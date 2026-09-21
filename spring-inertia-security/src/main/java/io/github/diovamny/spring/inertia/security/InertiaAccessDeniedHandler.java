package io.github.diovamny.spring.inertia.security;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.model.PageObject;
import io.github.diovamny.spring.inertia.security.InertiaSecurityModes;
import io.github.diovamny.spring.inertia.spi.FlashStore;
import io.github.diovamny.spring.inertia.spi.JsonProvider;
import io.github.diovamny.spring.inertia.version.VersionProvider;

/**
 * Inertia-aware access-denied handler:
 *
 * <ul>
 * <li>CSRF failures ({@code Missing/InvalidCsrfTokenException}) on Inertia
 * visits: generic flash message plus {@code 303} back to the same-origin
 * referer (or the configured fallback), so the user can retry. The next GET
 * issues a fresh token.</li>
 * <li>Authorization failures on Inertia visits: a valid {@code 403} Inertia
 * page ({@code X-Inertia: true}, {@code Vary: X-Inertia}) with the configured
 * component (default {@code Errors/Forbidden}).</li>
 * <li>Anything else (including non-Inertia requests): Spring Security's
 * default {@code 403} handling.</li>
 * </ul>
 */
public class InertiaAccessDeniedHandler implements AccessDeniedHandler {

    private final InertiaProperties properties;
    private final JsonProvider jsonProvider;
    private final VersionProvider versionProvider;
    private final FlashStore flashStore;
    private final AccessDeniedHandler delegate = new AccessDeniedHandlerImpl();

    public InertiaAccessDeniedHandler(InertiaProperties properties, JsonProvider jsonProvider,
            VersionProvider versionProvider, FlashStore flashStore) {
        this.properties = properties;
        this.jsonProvider = jsonProvider;
        this.versionProvider = versionProvider;
        this.flashStore = flashStore;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        var csrfFailure = accessDeniedException instanceof MissingCsrfTokenException
            || accessDeniedException instanceof InvalidCsrfTokenException;
        if (csrfFailure && InertiaChallengeUrls.isInertiaVisit(request)) {
            var security = properties.getSecurity();
            flashStore.put(security.getCsrfFlashKey(), security.getCsrfFlashMessage());
            var target = InertiaSecurityModes.safeFailureTarget(request.getHeader("Referer"),
                request.getScheme(), request.getServerName(), request.getServerPort(),
                security.getCsrfFailurePath());
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.setHeader("Location", target);
            return;
        }
        if (!csrfFailure && InertiaChallengeUrls.isInertiaVisit(request)) {
            renderForbiddenPage(request, response);
            return;
        }
        delegate.handle(request, response, accessDeniedException);
    }

    private void renderForbiddenPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        var security = properties.getSecurity();
        var page = new PageObject(security.getForbiddenComponent(),
            Map.of("status", 403, "message", "Forbidden"),
            request.getRequestURI(), versionProvider.version());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-Inertia", "true");
        response.setHeader("Vary", "X-Inertia");
        response.getWriter().write(jsonProvider.toJson(page));
    }
}
