package io.github.dg.spring.inertia.mvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.internal.InertiaRequestContext;
import io.github.dg.spring.inertia.protocol.PageObjectBuilder;

/**
 * Post-processes every response of the visit:
 *
 * <ul>
 *   <li>adds {@code Vary: X-Inertia}</li>
 *   <li>applies custom headers registered via the request context</li>
 *   <li>computes the lazy ETag and answers 304 on cache hits</li>
 *   <li>redirects empty 200 responses back to the referer</li>
 *   <li>normalizes 302 redirects of state-changing requests to 303</li>
 *   <li>converts external 302 redirects on Inertia GET visits into 409 +
 *       {@code X-Inertia-Location}</li>
 * </ul>
 */
public class InertiaFilter extends OncePerRequestFilter {

    private final InertiaProperties properties;

    public InertiaFilter(InertiaProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        var wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        applyVaryHeader(wrapper);
        applyCustomHeaders(wrapper);
        applyRedirectNormalization(request, wrapper);
        applyEmptyResponseRedirect(request, wrapper);
        applyEtag(request, wrapper);
        wrapper.copyBodyToResponse();
    }

    private void applyVaryHeader(ContentCachingResponseWrapper response) {
        if (response.getHeader("Vary") == null) {
            response.setHeader("Vary", "X-Inertia");
        }
    }

    @SuppressWarnings("unchecked")
    private void applyCustomHeaders(ContentCachingResponseWrapper response) {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_CUSTOM_HEADERS);
        if (stored instanceof Map<?, ?> map) {
            for (var entry : ((Map<String, String>) map).entrySet()) {
                response.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private void applyRedirectNormalization(HttpServletRequest request, ContentCachingResponseWrapper response) {
        var location = response.getHeader("Location");
        if (location == null) {
            return;
        }
        var method = request.getMethod();
        if (response.getStatus() == 302 && !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            response.setStatus(303);
        }
        if (response.getStatus() == 302 && InertiaRequestContext.isInertiaRequest()
                && "GET".equalsIgnoreCase(method)) {
            response.setStatus(409);
            response.setHeader("X-Inertia-Location", location);
            response.setHeader("Vary", "X-Inertia");
        }
    }

    private void applyEmptyResponseRedirect(HttpServletRequest request, ContentCachingResponseWrapper response) {
        if (response.getStatus() == 200 && response.getContentAsByteArray().length == 0
                && !"GET".equalsIgnoreCase(request.getMethod())) {
            var referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank()) {
                response.setStatus(302);
                response.setHeader("Location", referer);
            }
        }
    }

    private void applyEtag(HttpServletRequest request, ContentCachingResponseWrapper response) {
        if (!properties.isLazyEtagEnabled()) {
            return;
        }
        if (response.getStatus() != 200 || response.getHeader("X-Inertia") == null) {
            return;
        }
        var body = response.getContentAsByteArray();
        var etag = "\"" + sha256(body) + "\"";
        response.setHeader("ETag", etag);
        var ifNoneMatch = request.getHeader("If-None-Match");
        if (ifNoneMatch != null && matches(ifNoneMatch, etag)) {
            response.setStatus(304);
            response.setContentLength(0);
        }
    }

    private static boolean matches(String ifNoneMatch, String etag) {
        for (var candidate : ifNoneMatch.split(",")) {
            var trimmed = candidate.trim();
            if (trimmed.equals("*") || trimmed.equals(etag) || trimmed.equals("W/" + etag)) {
                return true;
            }
        }
        return false;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}