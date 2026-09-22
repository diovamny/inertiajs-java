package io.github.diovamny.inertia.core.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * §3.17 scenarios 3, 4, 11 at the guard level: redirect targets and page
 * size limits fail closed before any header or body is emitted.
 */
class SecurityGuardsTest {

    @Test
    void crlfTargetsAreRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("/ok\r\nSet-Cookie: x=1"));
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("/ok\nX: 1"));
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("/ok\tx"));
    }

    @Test
    void dangerousSchemesAreRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("javascript:alert(1)"));
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("  JaVaScRiPt:alert(1)"));
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("data:text/html,<h1>x</h1>"));
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("vbscript:msgbox(1)"));
        assertThrows(IllegalArgumentException.class,
            () -> RedirectTargets.check("file:///etc/passwd"));
    }

    @Test
    void legitimateTargetsPassThrough() {
        assertEquals("/contacts", RedirectTargets.check("/contacts"));
        assertEquals("https://example.com/x", RedirectTargets.check("https://example.com/x"));
        assertEquals("/paged#section", RedirectTargets.check("/paged#section"));
        assertNull(RedirectTargets.check(null));
    }

    @Test
    void oversizedPagesThrowWithSizes() {
        var failure = assertThrows(PageTooLargeException.class,
            () -> PageSizeGuard.check("x".repeat(2048), 1024));
        assertEquals(2048, failure.bytes());
        assertEquals(1024, failure.maxBytes());
        assertTrue(failure.getMessage().contains("2048"));
    }

    @Test
    void limitsPassOrDisable() {
        assertEquals("{}", PageSizeGuard.check("{}", 1024));
        assertEquals("{}", PageSizeGuard.check("{}", -1));
        assertNull(PageSizeGuard.check(null, 1024));
    }
}
