package io.github.diovamny.spring.inertia.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SsrEndpointValidatorTest {

    private static InertiaProperties props(String url, boolean remote, List<String> hosts) {
        var properties = new InertiaProperties();
        properties.setSsrEnabled(true);
        properties.setSsrUrl(url);
        properties.setSsrRemoteEnabled(remote);
        properties.setSsrAllowedHosts(hosts);
        return properties;
    }

    @Test
    void localSidecarBootsWithoutExtraConfig() {
        assertDoesNotThrow(() -> new InertiaConfigValidator(
            props("http://localhost:13714/render", false, List.of())));
    }

    @Test
    void remoteWithoutAllowlistRefusesToBoot() {
        assertThrows(IllegalArgumentException.class, () -> new InertiaConfigValidator(
            props("https://ssr.example.com/render", true, List.of())));
        assertThrows(IllegalArgumentException.class, () -> new InertiaConfigValidator(
            props("https://ssr.example.com/render", false, List.of("ssr.example.com"))));
        assertThrows(IllegalArgumentException.class, () -> new InertiaConfigValidator(
            props("http://ssr.example.com/render", true, List.of("ssr.example.com"))));
        assertThrows(IllegalArgumentException.class, () -> new InertiaConfigValidator(
            props("http://user:pass@localhost:13714/render", false, List.of())));
    }

    @Test
    void secureRemoteWithAllowlistBoots() {
        assertDoesNotThrow(() -> new InertiaConfigValidator(
            props("https://ssr.example.com/render", true, List.of("ssr.example.com"))));
    }

    @Test
    void disabledSsrSkipsEndpointValidation() {
        var properties = new InertiaProperties();
        properties.setSsrEnabled(false);
        properties.setSsrUrl("not-a-url");
        assertDoesNotThrow(() -> new InertiaConfigValidator(properties));
    }
}
