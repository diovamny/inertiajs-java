package com.example.kitchensink.config;

import java.util.List;
import java.util.Map;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.kitchensink.service.AuthService;
import io.github.dg.spring.inertia.api.Inertia;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.version.VersionProvider;

/**
 * Guards every route except public ones (login, logout, assets).
 * Unauthenticated requests are redirected to /login; Inertia non-GET requests
 * get a 303 with X-Inertia-Location so the client follows with a GET.
 *
 * <p>Also registers a per-visit error mapper so HTTP/business exceptions are
 * rendered as the {@code ErrorPage} component with their semantic HTTP
 * status (mirrors the Quarkus adapter's exception mapping).</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final List<String> PUBLIC_PREFIXES = List.of("assets");
    private static final List<String> PUBLIC_EXACT = List.of("login", "logout", "favicon.svg");

    private final AuthService auth;
    private final Inertia inertia;
    private final VersionProvider versionProvider;

    public AuthInterceptor(AuthService auth, Inertia inertia, VersionProvider versionProvider) {
        this.auth = auth;
        this.inertia = inertia;
        this.versionProvider = versionProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getDispatcherType() == DispatcherType.ERROR) {
            return true;
        }
        var path = request.getRequestURI().substring(request.getContextPath().length());
        var normalized = path.startsWith("/") ? path.substring(1) : path;
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        var user = auth.currentUser();
        inertia.share("auth", auth.authProps());
        inertia.handleErrorUsing(error -> errorPage(error, request));

        if (isPublic(normalized)) {
            return true;
        }

        if ("login".equals(normalized) && user != null) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "/");
            return false;
        }

        if (user != null) {
            return true;
        }

        var isInertia = "true".equalsIgnoreCase(request.getHeader("X-Inertia"));
        if (isInertia && !"GET".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.setHeader("Location", "/login");
            response.setHeader("X-Inertia-Location", "/login");
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "/login");
        }
        return false;
    }

    private ResponseEntity<?> errorPage(Throwable error, HttpServletRequest request) {
        if (error instanceof NoResourceFoundException) {
            return null;
        }
        var status = statusFor(error);
        var page = new PageObject("ErrorPage",
            Map.of("status", status, "message", messageFor(error)),
            request.getRequestURI(), versionProvider.version());
        return inertia.with(page, status);
    }

    private int statusFor(Throwable error) {
        var cause = error.getCause() != null && error.getCause() != error ? error.getCause() : error;
        if (cause instanceof ResponseStatusException rse) {
            return rse.getStatusCode().value();
        }
        if (cause instanceof IllegalArgumentException) return 400;
        if (cause instanceof SecurityException) return 403;
        if (cause instanceof IllegalStateException) return 409;
        if (cause instanceof jakarta.validation.ValidationException ve
                && !(ve instanceof jakarta.validation.ConstraintViolationException)) {
            return 422;
        }
        return 500;
    }

    private String messageFor(Throwable error) {
        var cause = error.getCause() != null && error.getCause() != error ? error.getCause() : error;
        if (cause instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : "Internal Server Error";
        }
        return cause.getMessage() != null ? cause.getMessage() : "Internal Server Error";
    }

    private boolean isPublic(String path) {
        if (PUBLIC_EXACT.contains(path)) return true;
        for (var prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}