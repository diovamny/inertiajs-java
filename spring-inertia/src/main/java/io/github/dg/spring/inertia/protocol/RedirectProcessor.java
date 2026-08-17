package io.github.dg.spring.inertia.protocol;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.annotation.RequestScope;

import io.github.dg.spring.inertia.api.InertiaRedirect;
import io.github.dg.spring.inertia.internal.InertiaRequestContext;
import io.github.dg.spring.inertia.internal.SpringFlashStore;
import io.github.dg.spring.inertia.spi.FlashStore;

/**
 * Builds the redirect responses of the visit:
 *
 * <ul>
 *   <li>302 for GET, 303 for state-changing requests</li>
 *   <li>full-page redirects on Inertia visits become 409 +
 *       {@code X-Inertia-Location} so the client performs the navigation
 *       itself</li>
 *   <li>fragment redirects on Inertia visits become 409 +
 *       {@code X-Inertia-Redirect}</li>
 *   <li>{@code location()} forces a full page load</li>
 * </ul>
 */
@RequestScope
public class RedirectProcessor {

    private final FlashStore flashStore;

    public RedirectProcessor(FlashStore flashStore) {
        this.flashStore = flashStore;
    }

    /**
     * Redirect to a URL; on state-changing visits a 303 is issued.
     *
     * @param url      the target URL
     * @param fullPage force a full page load
     * @return the redirect response
     */
    public InertiaRedirect redirect(String url, boolean fullPage) {
        var status = isGet() ? 302 : 303;
        if (!fullPage && isInertiaRequest() && url != null && url.contains("#")) {
            var headers = new HttpHeaders();
            headers.set("X-Inertia-Redirect", url);
            return new InertiaRedirect(HttpStatus.CONFLICT, headers, null, flashStore);
        }
        return back(status, Map.of(), url, fullPage);
    }

    /**
     * Redirect back to the referer, defaulting to {@code /}.
     *
     * @return the redirect response
     */
    public InertiaRedirect back() {
        return back(302, Map.of());
    }

    /**
     * Redirect back to the referer with a fallback URL.
     *
     * @param fallback used when no referer is present
     * @return the redirect response
     */
    public InertiaRedirect back(String fallback) {
        return back(302, Map.of(), fallback, true);
    }

    /**
     * Redirect back to the referer with the given status and custom headers.
     *
     * @param status  the HTTP status
     * @param headers extra response headers
     * @return the redirect response
     */
    public InertiaRedirect back(int status, Map<String, String> headers) {
        return back(status, headers, refererOrDefault("/"), false);
    }

    /**
     * Redirect back to the referer (or fallback) with the given status and
     * custom headers.
     *
     * @param status   the HTTP status
     * @param headers  extra response headers
     * @param fallback used when no referer is present
     * @return the redirect response
     */
    public InertiaRedirect back(int status, Map<String, String> headers, String fallback) {
        return back(status, headers, refererOrDefault(fallback), true);
    }

    /**
     * Force a full page load to the given URL.
     *
     * @param url the target URL
     * @return the redirect response
     */
    public InertiaRedirect location(String url) {
        var headers = new HttpHeaders();
        headers.set("X-Inertia-Location", url);
        return isInertiaRequest()
            ? new InertiaRedirect(HttpStatus.CONFLICT, headers, null, flashStore)
            : new InertiaRedirect(HttpStatus.FOUND, headers, null, flashStore);
    }

    private InertiaRedirect back(int status, Map<String, String> headers, String url, boolean fullPage) {
        if (url == null || url.isBlank()) {
            url = "/";
        }
        var httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::set);
        if (fullPage) {
            httpHeaders.set("X-Inertia-Location", url);
            if (isInertiaRequest()) {
                return new InertiaRedirect(HttpStatus.CONFLICT, httpHeaders, null, flashStore);
            }
            httpHeaders.set("Location", url);
            return new InertiaRedirect(HttpStatusCode.valueOf(status), httpHeaders, null, flashStore);
        }
        httpHeaders.set("Location", url);
        return new InertiaRedirect(HttpStatusCode.valueOf(status), httpHeaders, null, flashStore);
    }

    private static String refererOrDefault(String fallback) {
        var referer = InertiaRequestContext.header("Referer");
        return referer != null && !referer.isBlank() ? referer : fallback;
    }

    private static boolean isGet() {
        return "GET".equalsIgnoreCase(InertiaRequestContext.method());
    }

    private static boolean isInertiaRequest() {
        return InertiaRequestContext.isInertiaRequest();
    }
}