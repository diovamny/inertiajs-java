package io.github.diovamny.spring.inertia.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Applies the Inertia-compatible CSRF setup to an application-provided
 * {@link HttpSecurity} chain: Spring Security creates, exposes and validates
 * the token; the cookie name ({@code XSRF-TOKEN}) and header name
 * ({@code X-XSRF-TOKEN}) match what the official Inertia v3 client reads and
 * echoes automatically.
 *
 * <p>Uses the plain {@link CsrfTokenRequestAttributeHandler} (raw token echo).
 * {@code SpaCsrfTokenRequestHandler} was evaluated and rejected: it does not
 * exist in Spring Security 7.x, and the XOR-masking handlers require the
 * client to submit a masked token, which the official Inertia client does not
 * do (it echoes the raw cookie value).</p>
 *
 * <p>Typical use inside the application's own {@code SecurityFilterChain}
 * bean (the application keeps full control of routes and authorization):</p>
 * <pre>{@code
 * http.csrf(csrf -> InertiaCsrfConfigurer.configure(csrf, "/login"));
 * }</pre>
 */
public final class InertiaCsrfConfigurer {

    private InertiaCsrfConfigurer() {
    }

    /**
     * Build the Inertia-compatible token repository (cookie
     * {@code XSRF-TOKEN}, header {@code X-XSRF-TOKEN}, readable cookie).
     * Expose it as a bean so {@code InertiaCsrfTokenEmitFilter} can persist
     * the token on plain GET visits.
     *
     * @param cookiePath path attribute of the XSRF cookie
     * @return the configured repository
     */
    public static CookieCsrfTokenRepository xsrfTokenRepository(String cookiePath) {
        var repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setCookiePath(cookiePath == null || cookiePath.isBlank() ? "/" : cookiePath);
        return repository;
    }

    /**
     * Configure framework-owned CSRF with Inertia cookie/header names.
     *
     * @param csrf the CSRF configurer of the application chain
     * @param cookiePath path attribute of the XSRF cookie
     */
    public static void configure(CsrfConfigurer<HttpSecurity> csrf, String cookiePath) {
        configure(csrf, xsrfTokenRepository(cookiePath));
    }

    /**
     * Configure framework-owned CSRF with a shared repository instance.
     *
     * @param csrf the CSRF configurer of the application chain
     * @param repository the repository bean (also injected into the emit filter)
     */
    public static void configure(CsrfConfigurer<HttpSecurity> csrf,
            CookieCsrfTokenRepository repository) {
        csrf.csrfTokenRepository(repository)
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
    }
}
