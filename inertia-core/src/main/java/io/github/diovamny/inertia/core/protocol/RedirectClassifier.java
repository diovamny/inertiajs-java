package io.github.diovamny.inertia.core.protocol;

/**
 * Redirect classification for Inertia visits (protocol: redirects).
 *
 * <p>Rule order (shared by both adapters): a fragment-bearing target on a
 * non-prefetch, non-forced Inertia visit becomes {@code 409 +
 * X-Inertia-Redirect} (fresh Inertia {@code GET}; a forced full page wins
 * because the browser reload preserves the fragment natively); a forced
 * full-page or external Inertia visit becomes {@code 409 +
 * X-Inertia-Location} (window reload); otherwise standard {@code 302}/
 * {@code 303} by request method. Adapters keep their own
 * method-to-{@code GET} and URL-identity ({@code isExternal}) rules — whose
 * divergences (case sensitivity, default ports) are pinned by adapter
 * tests — and pass the verdicts in as booleans.</p>
 */
public final class RedirectClassifier {

    private RedirectClassifier() {
    }

    /** Wire shape of a classified redirect. */
    public enum Kind {
        FOUND_302,
        SEE_OTHER_303,
        CONFLICT_LOCATION,
        CONFLICT_REDIRECT
    }

    /**
     * A classified redirect: HTTP status plus the single location value for
     * the matching header ({@code Location}, {@code X-Inertia-Location} or
     * {@code X-Inertia-Redirect}).
     *
     * @param status HTTP status code
     * @param kind wire shape
     * @param location redirect target
     */
    public record Decision(int status, Kind kind, String location) {
    }

    /**
     * Classify a redirect target.
     *
     * @param getRequest whether the original request counts as GET (adapter rule)
     * @param inertia whether this is an Inertia visit
     * @param prefetch whether this is a prefetch visit
     * @param url redirect target, may be relative
     * @param fullPage whether a full-page visit was forced
     * @param external whether the target is outside the app (adapter rule)
     * @return the decision (status, shape and target)
     */
    public static Decision classify(boolean getRequest, boolean inertia, boolean prefetch,
            String url, boolean fullPage, boolean external) {
        if (inertia && !prefetch && !fullPage && url != null && url.contains("#")) {
            return new Decision(409, Kind.CONFLICT_REDIRECT, url);
        }
        if (fullPage && inertia) {
            return new Decision(409, Kind.CONFLICT_LOCATION, url);
        }
        if (inertia && external && getRequest) {
            return new Decision(409, Kind.CONFLICT_LOCATION, url);
        }
        if (!getRequest) {
            return new Decision(303, Kind.SEE_OTHER_303, url);
        }
        return new Decision(302, Kind.FOUND_302, url);
    }

    /**
     * Resolve the back-navigation target, preferring the referer.
     *
     * @param referer the {@code Referer} header, if any
     * @param fallback used when the referer is missing or blank
     * @return the referer, or the fallback
     */
    public static String refererOr(String referer, String fallback) {
        return referer != null && !referer.isBlank() ? referer : fallback;
    }
}
