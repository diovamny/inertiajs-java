package io.github.diovamny.quarkus.inertia.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Tests verifying the Interface Segregation Principle (ISP) refactoring.
 * Ensures each specialized role interface can be injected independently via CDI
 * as well as the unified Inertia facade.
 */
@QuarkusTest
class InertiaIspInjectionTest {

    @Inject
    Inertia fullFacade;

    @Inject
    InertiaRenderer renderer;

    @Inject
    InertiaRedirector redirector;

    @Inject
    InertiaProps props;

    @Inject
    InertiaFlash flash;

    @Inject
    InertiaMetadata metadata;

    @Test
    void eachSegregatedInterfaceCanBeInjectedIndependently() {
        assertNotNull(fullFacade, "Unified Inertia facade should be injectable");
        assertNotNull(renderer, "InertiaRenderer role interface should be injectable");
        assertNotNull(redirector, "InertiaRedirector role interface should be injectable");
        assertNotNull(props, "InertiaProps role interface should be injectable");
        assertNotNull(flash, "InertiaFlash role interface should be injectable");
        assertNotNull(metadata, "InertiaMetadata role interface should be injectable");
    }

    @Test
    void roleInterfacesCanPerformSpecializedOperations() {
        // Test InertiaProps
        props.share("user", "Alice");
        assertEquals("Alice", props.getShared("user", null));

        // Test InertiaMetadata
        metadata.meta("description", "Inertia Quarkus App");
        assertNotNull(metadata.getVersion());
        assertFalse(metadata.getVersion().isBlank());

        // Test InertiaFlash
        flash.flash("notice", "Success!");
        assertEquals("fallback", flash.getFlash("missing", "fallback"));

        // Test InertiaRedirector
        assertNotNull(redirector.redirect("/dashboard"));

        // Test InertiaRenderer
        assertNotNull(renderer.render("Dashboard", Map.of("title", "Test")));

        // Clean up
        props.flushShared();
    }
}
