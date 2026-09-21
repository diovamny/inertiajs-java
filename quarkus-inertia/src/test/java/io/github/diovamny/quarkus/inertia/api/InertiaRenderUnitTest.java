package io.github.diovamny.quarkus.inertia.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import io.github.diovamny.inertia.core.spi.FlashStore;

class InertiaRenderUnitTest {

    private InertiaRender render(FlashStore flashStore) {
        return new InertiaRender(Uni.createFrom().item("page"), flashStore);
    }

    @Test
    void flashStoresSingleEntryAndReturnsSameInstance() {
        var flashStore = mock(FlashStore.class);
        var result = render(flashStore);
        assertThat(result.flash("success", "Guardado!")).isSameAs(result);
        verify(flashStore).put("success", "Guardado!");
    }

    @Test
    void flashStoresMapAndReturnsSameInstance() {
        var flashStore = mock(FlashStore.class);
        var result = render(flashStore);
        assertThat(result.flash(Map.of("success", "OK"))).isSameAs(result);
        verify(flashStore).putAll(Map.of("success", "OK"));
    }

    @Test
    void withIsAliasOfFlash() {
        var flashStore = mock(FlashStore.class);
        var result = render(flashStore);
        assertThat(result.with("success", "OK")).isSameAs(result);
        verify(flashStore).put("success", "OK");
        assertThat(result.with(Map.of("a", "b"))).isSameAs(result);
        verify(flashStore).putAll(Map.of("a", "b"));
    }

    @Test
    void withErrorsStoresUnderErrorsKey() {
        var flashStore = mock(FlashStore.class);
        var result = render(flashStore);
        assertThat(result.withErrors(Map.of("email", "required"))).isSameAs(result);
        verify(flashStore).put("errors", Map.of("email", "required"));
    }

    @Test
    void stillResolvesAsUni() {
        var result = render(mock(FlashStore.class));
        assertThat(result.await().indefinitely()).isEqualTo("page");
    }

    @Test
    void enableSsrCacheSetsPolicyTtl() {
        var policy = new io.github.diovamny.quarkus.inertia.renderer.SsrCachePolicy();
        var result = new InertiaRender(Uni.createFrom().item("page"), mock(FlashStore.class), policy);
        assertThat(result.enableSsrCache(java.time.Duration.ofMinutes(15))).isSameAs(result);
        assertThat(policy.ttlMillis()).isEqualTo(15 * 60 * 1000L);
    }
}
