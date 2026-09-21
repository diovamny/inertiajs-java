package io.github.diovamny.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServerRequest;
import org.junit.jupiter.api.Test;

class PrecognitionGuardTest {

    private PrecognitionGuard guard(String precognition, String validateOnly, String validateOnlyAlias) {
        var request = mock(HttpServerRequest.class);
        when(request.getHeader("Precognition")).thenReturn(precognition);
        when(request.getHeader("X-Inertia-Precognition")).thenReturn(null);
        when(request.getHeader("Precognition-Validate-Only")).thenReturn(validateOnly);
        when(request.getHeader("X-Inertia-Precognition-Validate-Fields")).thenReturn(validateOnlyAlias);
        var rc = mock(RoutingContext.class);
        when(rc.request()).thenReturn(request);
        var current = mock(CurrentVertxRequest.class);
        when(current.getCurrent()).thenReturn(rc);
        var guard = new PrecognitionGuard();
        guard.currentVertxRequest = current;
        return guard;
    }

    @Test
    void plainRequestAllowsMutations() {
        var guard = guard(null, null, null);
        assertThat(guard.isPrecognitionRequest()).isFalse();
        assertThat(guard.isValidateOnlyRequest()).isFalse();
        assertThatNoException().isThrownBy(guard::assertMutationsAllowed);
    }

    @Test
    void precognitionWithoutValidateOnlyAllowsMutations() {
        var guard = guard("true", null, null);
        assertThat(guard.isPrecognitionRequest()).isTrue();
        assertThat(guard.isValidateOnlyRequest()).isFalse();
        assertThatNoException().isThrownBy(guard::assertMutationsAllowed);
    }

    @Test
    void validateOnlyBlocksMutations() {
        var guard = guard("true", "name,email", null);
        assertThat(guard.isValidateOnlyRequest()).isTrue();
        assertThatThrownBy(guard::assertMutationsAllowed)
            .isInstanceOf(PrecognitionWriteBlockedException.class);
    }

    @Test
    void validateOnlyAliasBlocksMutations() {
        var guard = guard(null, null, "name");
        assertThat(guard.isValidateOnlyRequest()).isTrue();
        assertThatThrownBy(guard::assertMutationsAllowed)
            .isInstanceOf(PrecognitionWriteBlockedException.class);
    }

    @Test
    void missingRequestScopeAllowsMutations() {
        var current = mock(CurrentVertxRequest.class);
        when(current.getCurrent()).thenThrow(new IllegalStateException("no scope"));
        var guard = new PrecognitionGuard();
        guard.currentVertxRequest = current;
        assertThat(guard.isPrecognitionRequest()).isFalse();
        assertThatNoException().isThrownBy(guard::assertMutationsAllowed);
    }
}
