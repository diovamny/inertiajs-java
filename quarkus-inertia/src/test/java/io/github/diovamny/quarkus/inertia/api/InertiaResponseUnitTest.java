package io.github.diovamny.quarkus.inertia.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import io.github.diovamny.inertia.core.spi.FlashStore;

class InertiaResponseUnitTest {

    @Test
    void flashWithStorePutsAndReturnsSameInstance() {
        var flashStore = mock(FlashStore.class);
        var response = new InertiaResponse(Response.ok().build(), null, flashStore);
        assertThat(response.flash("success", "OK")).isSameAs(response);
        verify(flashStore).put("success", "OK");
        assertThat(response.flash(Map.of("a", "b"))).isSameAs(response);
        verify(flashStore).putAll(Map.of("a", "b"));
    }

    @Test
    void withErrorsWithStorePutsUnderErrorsKey() {
        var flashStore = mock(FlashStore.class);
        var response = new InertiaResponse(Response.ok().build(), null, flashStore);
        assertThat(response.withErrors(Map.of("email", "required"))).isSameAs(response);
        verify(flashStore).put("errors", Map.of("email", "required"));
    }

    @Test
    void flashWithoutStoreFailsFast() {
        var response = new InertiaResponse(Response.ok().build());
        assertThatThrownBy(() -> response.flash("success", "OK"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("FlashStore");
    }
}
