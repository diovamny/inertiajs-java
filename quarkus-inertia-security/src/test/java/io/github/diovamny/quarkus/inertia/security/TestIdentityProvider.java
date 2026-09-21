package io.github.diovamny.quarkus.inertia.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

/**
 * Test-only identity provider (plaintext passwords, constant-time compare):
 * backs HTTP Basic in the security integration tests with two users.
 */
@ApplicationScoped
public class TestIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    private static final Map<String, String> PASSWORDS = Map.of(
        "user", "userpass",
        "admin", "adminpass");

    private static final Map<String, String> ROLES = Map.of(
        "user", "user",
        "admin", "admin");

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
            AuthenticationRequestContext context) {
        var expected = PASSWORDS.get(request.getUsername());
        var supplied = request.getPassword() != null
            ? String.valueOf(request.getPassword().getPassword())
            : null;
        // Note: getPassword() returns char[]; String.valueOf(char[]) is used.
        if (expected == null || supplied == null
                || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    supplied.getBytes(StandardCharsets.UTF_8))) {
            return Uni.createFrom().failure(new AuthenticationFailedException());
        }
        var identity = QuarkusSecurityIdentity.builder()
            .setPrincipal(new java.security.Principal() {
                @Override
                public String getName() {
                    return request.getUsername();
                }
            })
            .addRole("user")
            .addRole(ROLES.get(request.getUsername()))
            .setAnonymous(false)
            .build();
        return Uni.createFrom().item(identity);
    }
}
