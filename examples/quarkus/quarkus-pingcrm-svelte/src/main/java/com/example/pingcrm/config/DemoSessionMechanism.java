package com.example.pingcrm.config;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.AnonymousAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import com.example.pingcrm.service.AuthService;

/**
 * Session authentication mechanism for the demo: requests carrying a
 * logged-in session (written by the JSON login controller after PBKDF2
 * verification) resolve to a real {@code SecurityIdentity}, so path policies
 * and {@code @RolesAllowed} enforce authorization instead of the former
 * hand-written filter. Session data lives server-side, so trusting it is
 * safe; no database access happens here (fully non-blocking).
 *
 * <p>Challenges preserve the Inertia contract: anonymous HTML visits are
 * redirected (302) to the login page while anonymous Inertia visits receive
 * {@code 409} with {@code X-Inertia-Location} for a full navigation.</p>
 */
@ApplicationScoped
public class DemoSessionMechanism implements HttpAuthenticationMechanism {

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
            IdentityProviderManager identityProviderManager) {
        var session = context.session();
        var email = session != null ? session.get(AuthService.SESSION_EMAIL_KEY) : null;
        if (!(email instanceof String userEmail) || userEmail.isBlank()) {
            return Uni.createFrom().optional(Optional.empty());
        }
        var builder = QuarkusSecurityIdentity.builder()
            .setPrincipal(() -> userEmail)
            .addRole("user")
            .setAnonymous(false);
        var owner = session.get(AuthService.SESSION_OWNER_KEY);
        if (Boolean.TRUE.equals(owner)) {
            builder.addRole("owner");
        }
        return Uni.createFrom().item(builder.build());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        var inertia = context.request().getHeader("X-Inertia");
        var isInertia = inertia != null
            && ("true".equalsIgnoreCase(inertia) || Boolean.parseBoolean(inertia));
        if (isInertia) {
            return Uni.createFrom()
                .item(new ChallengeData(409, "X-Inertia-Location", "/login"));
        }
        return Uni.createFrom().item(new ChallengeData(302, "Location", "/login"));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(AnonymousAuthenticationRequest.class);
    }
}
