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
 * <p>All model types are records: besides invocation they require
 * introspection categories, otherwise GraalVM reports
 * {@code Record components not available for record class ...} at runtime.</p>
 */
public class InertiaRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var categories = new MemberCategory[] {
            MemberCategory.PUBLIC_FIELDS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS,
            MemberCategory.INTROSPECT_DECLARED_METHODS
        };
        for (var type : new Class<?>[] {
            PageObject.class, AlwaysProp.class, DeferredProp.class,
            OnceProp.class, RawJson.class, ScrollProp.class
        }) {
            hints.reflection().registerType(type, categories);
        }
    }
}