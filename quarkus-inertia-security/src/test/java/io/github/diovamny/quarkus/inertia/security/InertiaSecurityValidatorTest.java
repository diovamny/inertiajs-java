package io.github.diovamny.quarkus.inertia.security;

import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

class InertiaSecurityValidatorTest {

    @Test
    void frameworkWithVerificationDisabledFailsFast() {
        var config = mock(InertiaConfig.class);
        when(config.securityMode()).thenReturn(Optional.of("framework"));
        var raw = mock(Config.class);
        when(raw.getOptionalValue("quarkus.rest-csrf.verify-token", Boolean.class))
            .thenReturn(Optional.of(false));
        var validator = new InertiaSecurityValidator(config, raw);
        assertThrows(IllegalStateException.class, () -> validator.onStart(null));
    }

    @Test
    void frameworkWithFormOnlyCheckFailsFast() {
        var config = mock(InertiaConfig.class);
        when(config.securityMode()).thenReturn(Optional.of("framework"));
        var raw = mock(Config.class);
        when(raw.getOptionalValue("quarkus.rest-csrf.verify-token", Boolean.class))
            .thenReturn(Optional.empty());
        when(raw.getOptionalValue("quarkus.rest-csrf.require-form-url-encoded", Boolean.class))
            .thenReturn(Optional.of(true));
        var validator = new InertiaSecurityValidator(config, raw);
        assertThrows(IllegalStateException.class, () -> validator.onStart(null));
    }

    @Test
    void frameworkWithCorrectRestCsrfPasses() {
        var config = mock(InertiaConfig.class);
        when(config.securityMode()).thenReturn(Optional.of("framework"));
        var raw = mock(Config.class);
        when(raw.getOptionalValue("quarkus.rest-csrf.verify-token", Boolean.class))
            .thenReturn(Optional.empty());
        when(raw.getOptionalValue("quarkus.rest-csrf.require-form-url-encoded", Boolean.class))
            .thenReturn(Optional.of(false));
        // Inertia SPAs use plain double-submit: no signature key (a signed
        // cookie can never be echoed back by official clients).
        when(raw.getOptionalValue("quarkus.rest-csrf.token-signature-key", String.class))
            .thenReturn(Optional.empty());
        var validator = new InertiaSecurityValidator(config, raw);
        assertDoesNotThrow(() -> validator.onStart(null));
    }

    @Test
    void frameworkWithSignatureKeyWarnsButPasses() {
        var config = mock(InertiaConfig.class);
        when(config.securityMode()).thenReturn(Optional.of("framework"));
        var raw = mock(Config.class);
        when(raw.getOptionalValue("quarkus.rest-csrf.verify-token", Boolean.class))
            .thenReturn(Optional.empty());
        when(raw.getOptionalValue("quarkus.rest-csrf.require-form-url-encoded", Boolean.class))
            .thenReturn(Optional.of(false));
        when(raw.getOptionalValue("quarkus.rest-csrf.token-signature-key", String.class))
            .thenReturn(Optional.of("0123456789abcdef0123456789abcdef"));
        var validator = new InertiaSecurityValidator(config, raw);
        assertDoesNotThrow(() -> validator.onStart(null));
    }

    @Test
    void adapterModeSkipsValidation() {
        var config = mock(InertiaConfig.class);
        when(config.securityMode()).thenReturn(Optional.of("adapter"));
        var raw = mock(Config.class);
        var validator = new InertiaSecurityValidator(config, raw);
        assertDoesNotThrow(() -> validator.onStart(null));
    }
}
