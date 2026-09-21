package io.github.diovamny.quarkus.inertia.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.spi.InertiaSharedDataContributor;

/**
 * Opt-in shared-props contributor exposing an allowlisted identity summary
 * for presentation only ({@code auth.user} with {@code id}, {@code name} and
 * {@code roles} from the Quarkus {@code SecurityIdentity}).
 *
 * <p>Never exposes credentials, tokens or internal attributes. Authorization
 * decisions always stay server-side ({@code @RolesAllowed}, HTTP policies);
 * these props only show or hide UI controls. Active only when
 * {@code inertia.security.auth-props-enabled=true}.</p>
 */
@ApplicationScoped
public class InertiaSecurityIdentityProps implements InertiaSharedDataContributor {

    @Inject
    InertiaConfig config;

    @Inject
    io.quarkus.security.identity.SecurityIdentity identity;

    @Override
    public Map<String, Object> contribute(RoutingContext context) {
        if (!config.authPropsEnabled()) {
            // Disabled: contribute nothing so application-owned "auth" props
            // (e.g. a DB-backed contributor) are never clobbered.
            return Map.of();
        }
        var auth = new LinkedHashMap<String, Object>();
        if (identity == null || identity.isAnonymous()) {
            auth.put("user", null);
            return Map.of("auth", auth);
        }
        var user = new LinkedHashMap<String, Object>();
        var principal = identity.getPrincipal();
        var name = principal != null ? principal.getName() : null;
        user.put("id", name);
        user.put("name", name);
        user.put("roles", new ArrayList<>(identity.getRoles()));
        auth.put("user", user);
        return Map.of("auth", auth);
    }
}
