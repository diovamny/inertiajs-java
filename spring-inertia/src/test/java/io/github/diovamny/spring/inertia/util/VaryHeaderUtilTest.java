package io.github.diovamny.spring.inertia.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VaryHeaderUtilTest {

    @Test
    void mergesWithEmptyExisting() {
        assertEquals("X-Inertia", VaryHeaderUtil.merge(null, "X-Inertia"));
        assertEquals("X-Inertia", VaryHeaderUtil.merge("", "X-Inertia"));
    }

    @Test
    void preservesExistingTokens() {
        assertEquals("Accept-Encoding, X-Inertia",
            VaryHeaderUtil.merge("Accept-Encoding", "X-Inertia"));
    }

    @Test
    void deduplicatesCaseInsensitively() {
        assertEquals("X-Inertia",
            VaryHeaderUtil.merge("X-Inertia", "x-inertia", "X-INERTIA"));
    }

    @Test
    void mergesMultipleTokensPreservingOrder() {
        assertEquals("X-Inertia, X-Inertia-Version, X-Inertia-Partial-Component",
            VaryHeaderUtil.merge(null, "X-Inertia", "X-Inertia-Version",
                "X-Inertia-Partial-Component"));
    }

    @Test
    void addsToHttpHeadersWithoutDropping() {
        var headers = new HttpHeaders();
        headers.set("Vary", "Accept-Encoding");
        VaryHeaderUtil.addTo(headers, "X-Inertia");
        var vary = headers.getFirst("Vary");
        assertTrue(vary.contains("Accept-Encoding"));
        assertTrue(vary.contains("X-Inertia"));
    }

    @Test
    void ignoresBlankTokens() {
        assertEquals("X-Inertia", VaryHeaderUtil.merge("X-Inertia", null, "  ", "X-Inertia"));
    }
}
