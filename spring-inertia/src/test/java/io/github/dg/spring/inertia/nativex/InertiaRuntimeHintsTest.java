package io.github.dg.spring.inertia.nativex;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

import io.github.dg.spring.inertia.model.AlwaysProp;
import io.github.dg.spring.inertia.model.DeferredProp;
import io.github.dg.spring.inertia.model.OnceProp;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.model.RawJson;
import io.github.dg.spring.inertia.model.ScrollProp;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the GraalVM reflection metadata: every model record must be
 * registered with invocation <em>and</em> introspection categories,
 * otherwise Jackson fails in native images with
 * {@code Record components not available for record class ...}.
 */
class InertiaRuntimeHintsTest {

    @Test
    void allModelRecordsAreRegisteredWithIntrospection() {
        var hints = new RuntimeHints();
        new InertiaRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (var type : new Class<?>[] {
            PageObject.class, AlwaysProp.class, DeferredProp.class,
            OnceProp.class, RawJson.class, ScrollProp.class
        }) {
            var hint = hints.reflection().getTypeHint(type);
            assertTrue(hint != null, "missing reflection hint for " + type.getName());
            var categories = hint.getMemberCategories();
            assertTrue(categories.contains(MemberCategory.INVOKE_DECLARED_METHODS),
                type.getName() + " must allow declared method invocation");
            assertTrue(categories.contains(MemberCategory.INTROSPECT_DECLARED_METHODS),
                type.getName() + " must allow declared method introspection (records)");
        }
    }
}
