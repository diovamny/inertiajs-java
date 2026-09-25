package io.github.diovamny.inertia.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RedirectClassifierTest {

    @Test
    void getRedirectIsFound() {
        var decision = RedirectClassifier.classify(true, true, false, "/dash", false, false);
        assertThat(decision.status()).isEqualTo(302);
        assertThat(decision.kind()).isEqualTo(RedirectClassifier.Kind.FOUND_302);
        assertThat(decision.location()).isEqualTo("/dash");
    }

    @Test
    void nonGetRedirectIsSeeOther() {
        var decision = RedirectClassifier.classify(false, true, false, "/dash", false, false);
        assertThat(decision.status()).isEqualTo(303);
        assertThat(decision.kind()).isEqualTo(RedirectClassifier.Kind.SEE_OTHER_303);
    }

    @Test
    void fragmentTargetBecomesConflictRedirect() {
        var decision = RedirectClassifier.classify(true, true, false, "/page#section", false, false);
        assertThat(decision.status()).isEqualTo(409);
        assertThat(decision.kind()).isEqualTo(RedirectClassifier.Kind.CONFLICT_REDIRECT);
        assertThat(decision.location()).isEqualTo("/page#section");
    }

    @Test
    void prefetchFragmentStaysStandardRedirect() {
        var decision = RedirectClassifier.classify(true, true, true, "/page#section", false, false);
        assertThat(decision.status()).isEqualTo(302);
    }

    @Test
    void forcedFullPageWinsOverFragment() {
        var decision = RedirectClassifier.classify(true, true, false, "/page#section", true, false);
        assertThat(decision.status()).isEqualTo(409);
        assertThat(decision.kind()).isEqualTo(RedirectClassifier.Kind.CONFLICT_LOCATION);
    }

    @Test
    void nonInertiaFragmentStaysStandardRedirect() {
        var decision = RedirectClassifier.classify(true, false, false, "/page#section", false, false);
        assertThat(decision.status()).isEqualTo(302);
    }

    @Test
    void forcedFullPageOnInertiaIsConflictLocation() {
        var decision = RedirectClassifier.classify(true, true, false, "/dash", true, false);
        assertThat(decision.status()).isEqualTo(409);
        assertThat(decision.kind()).isEqualTo(RedirectClassifier.Kind.CONFLICT_LOCATION);
    }

    @Test
    void forcedFullPageOffInertiaFollowsMethodRule() {
        assertThat(RedirectClassifier.classify(true, false, false, "/dash", true, false).status())
            .isEqualTo(302);
        assertThat(RedirectClassifier.classify(false, false, false, "/dash", true, false).status())
            .isEqualTo(303);
    }

    @Test
    void externalGetOnInertiaIsConflictLocation() {
        var decision = RedirectClassifier.classify(true, true, false, "https://other.test/x", false, true);
        assertThat(decision.status()).isEqualTo(409);
        assertThat(decision.kind()).isEqualTo(RedirectClassifier.Kind.CONFLICT_LOCATION);
    }

    @Test
    void refererOrPrefersReferer() {
        assertThat(RedirectClassifier.refererOr("http://app/back", "/")).isEqualTo("http://app/back");
        assertThat(RedirectClassifier.refererOr(null, "/")).isEqualTo("/");
        assertThat(RedirectClassifier.refererOr("  ", "/fallback")).isEqualTo("/fallback");
    }
}
