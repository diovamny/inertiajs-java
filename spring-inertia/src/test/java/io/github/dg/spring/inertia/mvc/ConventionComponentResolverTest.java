package io.github.dg.spring.inertia.mvc;

import org.junit.jupiter.api.Test;
import io.github.dg.spring.inertia.config.InertiaProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConventionComponentResolverTest {

    @Test
    void preservesExplicitComponent() {
        var properties = new InertiaProperties();
        var resolver = new ConventionComponentResolver(properties);

        assertEquals("Users/Edit", resolver.transform("Users/Edit"));
    }

    @Test
    void appliesPrefixWhenConfigured() {
        var properties = new InertiaProperties();
        properties.setConventionRoutingPrefix("Admin/");
        var resolver = new ConventionComponentResolver(properties);

        assertEquals("Admin/Users/Edit", resolver.transform("Users/Edit"));
        assertEquals("Admin/Users/Edit", resolver.transform("Admin/Users/Edit"));
    }

    @Test
    void fallsBackToDefaultWhenNoContext() {
        var properties = new InertiaProperties();
        var resolver = new ConventionComponentResolver(properties);

        assertEquals("Index", resolver.transform(null));
        assertEquals("Index", resolver.transform(""));
    }
}
