package com.example.pingcrm.config;

import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;

import com.example.pingcrm.service.AuthService;
import io.github.diovamny.quarkus.inertia.spi.InertiaSharedDataContributor;

/**
 * Shares the Security-backed identity on every page (replaces the former
 * hand-written filter; route protection itself is owned by Quarkus Security
 * HTTP policies).
 */
@ApplicationScoped
public class AuthPropsContributor implements InertiaSharedDataContributor {

    @Inject
    AuthService auth;

    @Override
    public Map<String, Object> contribute(RoutingContext context) {
        return Map.of("auth", auth.authProps());
    }
}
