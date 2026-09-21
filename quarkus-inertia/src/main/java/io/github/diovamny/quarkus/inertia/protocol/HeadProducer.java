package io.github.diovamny.quarkus.inertia.protocol;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import io.github.diovamny.inertia.core.head.HeadBuilder;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * One {@link HeadBuilder} per request, preconfigured with the
 * {@code inertia.meta-title-template} setting.
 */
@ApplicationScoped
public class HeadProducer {

    @Inject
    InertiaConfig config;

    @Produces
    @RequestScoped
    public HeadBuilder headBuilder() {
        return new HeadBuilder().titleTemplate(config.metaTitleTemplate());
    }
}
