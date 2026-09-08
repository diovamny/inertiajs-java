package io.github.dg.spring.inertia.mvc;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.security.InertiaCsrfService;

/**
 * Synchronizes the {@code XSRF-TOKEN} cookie with the session token: the
 * frontend echoes the cookie value in the {@code X-XSRF-TOKEN} (or
 * {@code X-CSRF-TOKEN}) header of state-changing requests; mismatches are
 * rejected with 419 before the controller runs.
 *
 * <p>The cookie is emitted on every response so that the initial HTML page
 * load also receives it. CSRF validation only applies to state-changing
 * Inertia requests (POST/PUT/PATCH/DELETE with {@code X-Inertia: true}).
 * Non-Inertia and GET requests pass through without validation.</p>
 */
public class InertiaCsrfFilter extends OncePerRequestFilter {

    private final InertiaProperties properties;
    private final InertiaCsrfService csrfService;

    public InertiaCsrfFilter(InertiaProperties properties, InertiaCsrfService csrfService) {
        this.properties = properties;
        this.csrfService = csrfService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !properties.isCsrfEnabled();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        var method = request.getMethod();
        var stateChanging = "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
            || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
        var isInertia = isInertiaRequest(request);

        if (stateChanging && isInertia) {
            var submitted = request.getHeader("X-XSRF-TOKEN");
            if (submitted == null || submitted.isBlank()) {
                submitted = request.getHeader("X-CSRF-TOKEN");
            }
            if (!csrfService.validate(submitted)) {
                response.setStatus(419);
                response.setHeader("X-Inertia-Location", request.getRequestURI());
                return;
            }
            csrfService.setHandled(true);
        }

        var token = csrfService.token();
        csrfService.setToken(token);
        var cookie = new Cookie("XSRF-TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        filterChain.doFilter(request, response);
    }

    private boolean isInertiaRequest(HttpServletRequest request) {
        var value = request.getHeader("X-Inertia");
        return value != null && ("true".equalsIgnoreCase(value) || Boolean.parseBoolean(value));
    }
}
