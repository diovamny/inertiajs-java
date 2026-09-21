package io.github.diovamny.inertia.core.result;

/**
 * A typed external-location request ({@code 409} + {@code X-Inertia-Location}
 * for Inertia visits, plain redirect otherwise).
 *
 * @param url the external URL
 */
public record InertiaLocationResult(String url) implements InertiaResult {

    public InertiaLocationResult {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must not be blank");
        }
    }

    /**
     * Create an external location.
     */
    public static InertiaLocationResult to(String url) {
        return new InertiaLocationResult(url);
    }
}
