package io.github.diovamny.quarkus.inertia.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SsrEndpointValidatorTest {

    private static InertiaConfig config(String url, boolean remote, Optional<List<String>> hosts,
            boolean ssrEnabled) {
        var config = mock(InertiaConfig.class);
        when(config.rootTemplate()).thenReturn("index.html");
        when(config.versionStrategy()).thenReturn("sha256");
        when(config.csrfRefreshPolicy()).thenReturn("always");
        when(config.maxPageBytes()).thenReturn(33554432L);
        when(config.securityMode()).thenReturn(Optional.empty());
        when(config.securityFailOnFallback()).thenReturn(false);
        when(config.securityAllowDisabledInProduction()).thenReturn(false);
        when(config.securityLoginUrl()).thenReturn("/login");
        when(config.securityForbiddenComponent()).thenReturn("Errors/Forbidden");
        when(config.securityCsrfFailurePath()).thenReturn("/");
        when(config.securityCsrfFlashKey()).thenReturn("error");
        when(config.securityCsrfFlashMessage()).thenReturn("expired");
        when(config.securityCookieSameSite()).thenReturn("Lax");
        when(config.securityCookieSecure()).thenReturn(false);
        when(config.securityCookiePath()).thenReturn("/");
        when(config.securityCookieDomain()).thenReturn(Optional.empty());
        when(config.securityReactiveCsrfPaths()).thenReturn(Optional.empty());
        when(config.authPropsEnabled()).thenReturn(false);
        when(config.csrfEnabled()).thenReturn(true);
        when(config.ssrEnabled()).thenReturn(ssrEnabled);
        when(config.ssrUrl()).thenReturn(url);
        when(config.ssrRemoteEnabled()).thenReturn(remote);
        when(config.ssrAllowedHosts()).thenReturn(hosts);
        return config;
    }

    @Test
    void localSidecarBootsWithoutExtraConfig() {
        assertDoesNotThrow(() -> new InertiaConfigValidator(
            config("http://localhost:13714", false, Optional.empty(), true)).onStart(null));
    }

    @Test
    void remoteWithoutAllowlistRefusesToBoot() {
        assertThrows(IllegalArgumentException.class, () -> new InertiaConfigValidator(
            config("https://ssr.example.com/render", true, Optional.empty(), true)).onStart(null));
        assertThrows(IllegalArgumentException.class, () -> new InertiaConfigValidator(
            config("https://ssr.example.com/render", false, Optional.of(List.of("ssr.example.com")), true))
            .onStart(null));
        assertThrows(IllegalArgumentException.class, () -> new InertiaConfigValidator(
            config("http://ssr.example.com/render", true, Optional.of(List.of("ssr.example.com")), true))
            .onStart(null));
    }

    @Test
    void secureRemoteWithAllowlistBoots() {
        assertDoesNotThrow(() -> new InertiaConfigValidator(
            config("https://ssr.example.com/render", true,
                Optional.of(List.of("ssr.example.com")), true)).onStart(null));
    }

    @Test
    void disabledSsrSkipsEndpointValidation() {
        assertDoesNotThrow(() -> new InertiaConfigValidator(
            config("not-a-url", false, Optional.empty(), false)).onStart(null));
    }
}
