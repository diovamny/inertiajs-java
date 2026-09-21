package com.example.kitchensink.config;

import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AnonymousAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

/**
 * Resolves anonymous requests to the anonymous identity, as required to pair
 * with {@link DemoSessionMechanism} (a mechanism must have at least one
 * provider supporting one of its credential types).
 */
@ApplicationScoped
public class AnonymousIdentityProvider implements IdentityProvider<AnonymousAuthenticationRequest> {

    @Override
    public Class<AnonymousAuthenticationRequest> getRequestType() {
        return AnonymousAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(AnonymousAuthenticationRequest request,
            AuthenticationRequestContext context) {
        return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
            .setAnonymous(true)
            .setPrincipal(() -> "anonymous")
            .build());
    }
}
