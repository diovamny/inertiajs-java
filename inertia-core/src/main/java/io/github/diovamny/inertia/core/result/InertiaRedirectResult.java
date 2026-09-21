package io.github.diovamny.inertia.core.result;

/**
 * A typed redirect request.
 *
 * @param url      the target URL
 * @param fullPage when {@code true} and the request is an Inertia visit, the
 *                 adapters answer {@code 409} + {@code X-Inertia-Location}
 *                 instead of following the redirect in place
 */
public record InertiaRedirectResult(String url, boolean fullPage) implements InertiaResult {

    public InertiaRedirectResult {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must not be blank");
        }
    }

    /**
     * Create an in-place redirect.
     */
    public static InertiaRedirectResult to(String url) {
        return new InertiaRedirectResult(url, false);
    }

    /**
     * Create a full-page redirect.
     */
    public static InertiaRedirectResult fullPage(String url) {
        return new InertiaRedirectResult(url, true);
    }
}
