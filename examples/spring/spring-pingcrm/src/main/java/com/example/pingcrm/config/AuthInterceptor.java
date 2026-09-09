package com.example.pingcrm.config;

import java.util.List;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.pingcrm.service.AuthService;
import io.github.diovamny.spring.inertia.api.Inertia;

/**
 * Guards every route except public ones (login, logout, assets, images).
 * Unauthenticated requests are redirected to /login; Inertia non-GET requests
 * get a 303 with X-Inertia-Location so the client follows with a GET.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final List<String> PUBLIC_PREFIXES = List.of("assets", "img", "build");
    private static final List<String> PUBLIC_EXACT = List.of("login", "logout", "favicon.svg", "error");

    private final AuthService auth;
    private final Inertia inertia;

    public AuthInterceptor(AuthService auth, Inertia inertia) {
        this.auth = auth;
        this.inertia = inertia;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getDispatcherType() == DispatcherType.ERROR) {
            return true;
        }
        var path = request.getRequestURI().substring(request.getContextPath().length());
        var normalized = path.startsWith("/") ? path.substring(1) : path;

        var user = auth.currentUser();
        inertia.share("auth", auth.authProps());

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

    private boolean isPublic(String path) {
        if (PUBLIC_EXACT.contains(path)) return true;
        for (var prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
