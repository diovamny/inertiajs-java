package com.example.pingcrm.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.pingcrm.service.AuthService;
import io.github.diovamny.spring.inertia.api.Inertia;

/**
 * Shares the Security-backed identity on every visit.
 *
 * <p>Route protection itself is owned by Spring Security (see
 * {@code PingCrmSvelteApplication}): anonymous HTML visits are redirected to
 * the login page while anonymous Inertia visits receive {@code 409} with
 * {@code X-Inertia-Location}.</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

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
        inertia.share("auth", auth.authProps());
        return true;
    }
}
