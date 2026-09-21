package io.github.diovamny.spring.inertia.security;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import io.github.diovamny.spring.inertia.config.InertiaProperties;

/**
 * Inertia-aware authentication entry point for anonymous requests to
 * protected routes: regular visits keep Spring Security's login redirect,
 * while Inertia visits receive a protocol control response ({@code 409} +
 * {@code X-Inertia-Location}, without {@code X-Inertia}) so the client
 * performs a full navigation to the login/OIDC challenge.
 */
public class InertiaAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final InertiaProperties properties;
    private final LoginUrlAuthenticationEntryPoint delegate;

    public InertiaAuthenticationEntryPoint(InertiaProperties properties) {
        this.properties = properties;
        this.delegate = new LoginUrlAuthenticationEntryPoint(loginUrl(properties));
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        if (InertiaChallengeUrls.isInertiaVisit(request)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setHeader("X-Inertia-Location", InertiaChallengeUrls
                .validatedLoginUrl(properties.getSecurity().getLoginUrl(), request));
            return;
        }
        delegate.commence(request, response, authException);
    }

    private static String loginUrl(InertiaProperties properties) {
        var configured = properties.getSecurity().getLoginUrl();
        if (configured == null || configured.isBlank()) {
            return "/login";
        }
        return configured.trim();
    }
}
