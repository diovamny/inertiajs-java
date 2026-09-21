package io.github.diovamny.spring.inertia.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures the XSRF cookie is issued on the initial HTML visit.
 *
 * <p>Spring Security 7.x resolves the CSRF token lazily: on a plain GET that
 * never touches the {@code _csrf} request attribute nothing triggers
 * generation and no cookie is emitted. The official Inertia v3 client needs
 * that cookie before its first mutation, so this filter (running after the
 * security chain) loads or generates the token through the same repository
 * the chain validates against and persists it.</p>
 */
public class InertiaCsrfTokenEmitFilter extends OncePerRequestFilter {

    private final CsrfTokenRepository repository;

    public InertiaCsrfTokenEmitFilter(CsrfTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        var method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            var token = repository.loadToken(request);
            if (token == null) {
                token = repository.generateToken(request);
            }
            repository.saveToken(token, request, response);
        }
        filterChain.doFilter(request, response);
    }
}
