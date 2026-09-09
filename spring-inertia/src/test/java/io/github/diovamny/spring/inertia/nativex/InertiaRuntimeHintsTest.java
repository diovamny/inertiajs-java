package io.github.diovamny.spring.inertia.nativex;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

import io.github.diovamny.spring.inertia.model.AlwaysProp;
import io.github.diovamny.spring.inertia.model.DeferredProp;
import io.github.diovamny.spring.inertia.model.OnceProp;
import io.github.diovamny.spring.inertia.model.PageObject;
import io.github.diovamny.spring.inertia.model.RawJson;
import io.github.diovamny.spring.inertia.model.ScrollProp;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the GraalVM reflection metadata: every model record must be
 * registered with access and invocation categories. Spring Framework 7 adds
 * method introspection (including record components) by default, so the
 * retired {@code INTROSPECT_*} constants must not be used.
 */
class InertiaRuntimeHintsTest {

    @Test
    void allModelRecordsAreRegisteredWithoutRetiredCategories() {
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
            assertTrue(categories.contains(MemberCategory.ACCESS_DECLARED_FIELDS),
                type.getName() + " must allow declared field access");
        }
    }
}
