package io.github.diovamny.quarkus.inertia.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.credential.PasswordCredential;

class TestIdentityProviderTest {

    private final TestIdentityProvider provider = new TestIdentityProvider();

    @Test
    void requestTypeIsUsernamePassword() {
        assertEquals(UsernamePasswordAuthenticationRequest.class, provider.getRequestType());
    }

    @Test
    void validCredentialsAuthenticate() {
        var request = new UsernamePasswordAuthenticationRequest("user", new PasswordCredential("userpass".toCharArray()));
        var identity = provider.authenticate(request, null).await().indefinitely();
        assertEquals("user", identity.getPrincipal().getName());
        assertTrue(identity.getRoles().contains("user"));
    }

    @Test
    void wrongPasswordFails() {
        var request = new UsernamePasswordAuthenticationRequest("user", new PasswordCredential("nope".toCharArray()));
        assertThrows(AuthenticationFailedException.class,
            () -> provider.authenticate(request, null).await().indefinitely());
    }

    @Test
    void unknownUserFails() {
        var request = new UsernamePasswordAuthenticationRequest("ghost", new PasswordCredential("x".toCharArray()));
        assertThrows(AuthenticationFailedException.class,
            () -> provider.authenticate(request, null).await().indefinitely());
    }
}
