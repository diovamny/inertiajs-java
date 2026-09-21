package io.github.diovamny.spring.inertia.mvc;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.security.InertiaCsrfService;
import io.github.diovamny.spring.inertia.security.InertiaSecurityModes;
import io.github.diovamny.spring.inertia.spi.FlashStore;

/**
 * Adapter-owned CSRF protection (mode {@code adapter} only): synchronizes the
 * {@code XSRF-TOKEN} cookie with the session token, which the official Inertia
 * v3 client echoes in the {@code X-XSRF-TOKEN} header of state-changing
 * requests.
 *
 * <p>Failure contract: an Inertia visit with a missing/invalid token never
 * reaches the controller and is answered with {@code 303} back to the
 * same-origin referer (or the configured fallback path) carrying a generic
 * flash message, so the user can retry without a technical error. Non-Inertia
 * requests pass through untouched so other security configurations (or plain
 * HTML forms) handle them. In {@code framework} mode this filter is not
 * registered at all: Spring Security owns CSRF.</p>
 *
 * <p>The cookie is emitted on every response so the initial HTML page load
 * receives it. The session cookie itself stays {@code HttpOnly}; only the
 * XSRF cookie is readable by the client ({@code HttpOnly=false}).</p>
 */
public class InertiaCsrfFilter extends OncePerRequestFilter {

    private final InertiaProperties properties;
    private final InertiaCsrfService csrfService;
    private final FlashStore flashStore;

    public InertiaCsrfFilter(InertiaProperties properties, InertiaCsrfService csrfService,
            FlashStore flashStore) {
        this.properties = properties;
        this.csrfService = csrfService;
        this.flashStore = flashStore;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return InertiaSecurityModes.effectiveMode(properties)
            != InertiaSecurityModes.Mode.ADAPTER;
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
                rejectInertia(request, response);
                return;
            }
            csrfService.setHandled(true);
        }

        emitXsrfCookie(request, response);
        filterChain.doFilter(request, response);
    }

    private void rejectInertia(HttpServletRequest request, HttpServletResponse response) {
        var security = properties.getSecurity();
        flashStore.put(security.getCsrfFlashKey(), security.getCsrfFlashMessage());
        var target = InertiaSecurityModes.safeFailureTarget(request.getHeader("Referer"),
            request.getScheme(), request.getServerName(), request.getServerPort(),
            security.getCsrfFailurePath());
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", target);
    }

    private void emitXsrfCookie(HttpServletRequest request, HttpServletResponse response) {
        var token = csrfService.token();
        csrfService.setToken(token);
        var security = properties.getSecurity();
        var cookie = new Cookie("XSRF-TOKEN", token);
        var path = security.getCookiePath();
        cookie.setPath(path == null || path.isBlank() ? "/" : path);
        cookie.setHttpOnly(false);
        cookie.setSecure(security.isCookieSecure() || request.isSecure());
        var domain = security.getCookieDomain();
        if (domain != null && !domain.isBlank()) {
            cookie.setDomain(domain.trim());
        }
        cookie.setAttribute("SameSite", sameSite(security.getCookieSameSite()));
        response.addCookie(cookie);
    }

    private static String sameSite(String configured) {
        if (configured == null || configured.isBlank()) {
            return "Lax";
        }
        var normalized = configured.trim();
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1).toLowerCase();
    }

    private boolean isInertiaRequest(HttpServletRequest request) {
        var value = request.getHeader("X-Inertia");
        return value != null && ("true".equalsIgnoreCase(value) || Boolean.parseBoolean(value));
    }
}
