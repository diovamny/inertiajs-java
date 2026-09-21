package com.example.kitchensink.config;

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
import io.github.diovamny.spring.inertia.api.Inertia;
import io.github.diovamny.spring.inertia.model.PageObject;
import io.github.diovamny.spring.inertia.version.VersionProvider;

/**
 * Shares the Security-backed identity on every visit and registers a
 * per-visit error mapper so HTTP/business exceptions are rendered as the
 * {@code ErrorPage} component with their semantic HTTP status (mirrors the
 * Quarkus adapter's exception mapping).
 *
 * <p>Route protection itself is owned by Spring Security (see
 * {@code DemoSecurityConfig}): anonymous HTML visits are redirected to the
 * login page while anonymous Inertia visits receive {@code 409} with
 * {@code X-Inertia-Location}.</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

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
        inertia.share("auth", auth.authProps());
        inertia.handleErrorUsing(error -> errorPage(error, request));
        return true;
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

}
