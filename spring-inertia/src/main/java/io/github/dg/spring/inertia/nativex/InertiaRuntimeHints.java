package io.github.dg.spring.inertia.nativex;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import io.github.dg.spring.inertia.model.AlwaysProp;
import io.github.dg.spring.inertia.model.DeferredProp;
import io.github.dg.spring.inertia.model.OnceProp;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.model.RawJson;
import io.github.dg.spring.inertia.model.ScrollProp;

/**
 * Registers the reflection metadata needed by the Jackson serializer when
 * the application is compiled to a GraalVM native image.
 *
 * <p>All model types are records. Spring Framework 7 adds method
 * introspection (including record components) by default to every reflected
 * type, so only access and invocation categories are declared; the retired
 * {@code INTROSPECT_*} constants must not be used.</p>
 */
public final class InertiaRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        var categories = new MemberCategory[] {
            MemberCategory.ACCESS_PUBLIC_FIELDS,
            MemberCategory.ACCESS_DECLARED_FIELDS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_METHODS
        };
        for (var type : new Class<?>[] {
            PageObject.class, AlwaysProp.class, DeferredProp.class,
            OnceProp.class, RawJson.class, ScrollProp.class
        }) {
            hints.reflection().registerType(type, categories);
        }
    }
}
