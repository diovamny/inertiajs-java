package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.smallrye.mutiny.Uni;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.spi.FlashStore;
import com.quarkus.inertia.version.VersionProvider;

class FlashDataUnitTest {

    private PageObjectBuilder builder;
    private SharedDataRegistry sharedData;
    private FlashStore flashStore;
    private InertiaConfig config;

    @BeforeEach
    void setUp() {
        sharedData = new SharedDataRegistry();

        var versionProvider = mock(VersionProvider.class);
        when(versionProvider.getVersion()).thenReturn("1.0.0");

        flashStore = mock(FlashStore.class);
        when(flashStore.hasData()).thenReturn(false);

        config = mock(InertiaConfig.class);
        when(config.encryptHistory()).thenReturn(false);
        when(config.camelizeProps()).thenReturn(false);
        when(config.alwaysIncludeErrors()).thenReturn(false);

        var routingContext = mock(RoutingContext.class);
        var httpRequest = mock(HttpServerRequest.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(httpRequest.uri()).thenReturn("/flash");

        var currentVertxRequest = mock(CurrentVertxRequest.class);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        builder = new PageObjectBuilder(
            sharedData,
            versionProvider,
            new PartialReloadProcessor(),
            new OncePropRegistry(),
            new MergePropProcessor(),
            flashStore,
            currentVertxRequest,
            config);
    }

    @Test
    void shouldIncludeAllFlashKeysByDefault() {
        when(flashStore.hasData()).thenReturn(true);
        when(flashStore.drain()).thenReturn(Map.of("success", "creado", "warning", "cuidado"));

        var page = builder.build("Home", Map.of(), false).await().indefinitely();

        assertThat(page.props())
            .containsEntry("success", "creado")
            .containsEntry("warning", "cuidado");
    }

    @Test
    void shouldFilterFlashKeysByAllowlist() {
        when(flashStore.hasData()).thenReturn(true);
        when(flashStore.drain()).thenReturn(Map.of("success", "creado", "warning", "cuidado"));
        when(config.flashKeys()).thenReturn(Optional.of(List.of("success")));

        var page = builder.build("Home", Map.of(), false).await().indefinitely();

        assertThat(page.props())
            .containsEntry("success", "creado")
            .doesNotContainKey("warning");
    }

    @Test
    void shouldNotIncludeErrorsByDefaultWhenNone() {
        var page = builder.build("Home", Map.of(), false).await().indefinitely();

        assertThat(page.props()).doesNotContainKey("errors");
    }

    @Test
    void shouldAlwaysIncludeEmptyErrorsWhenConfigured() {
        when(config.alwaysIncludeErrors()).thenReturn(true);

        var page = builder.build("Home", Map.of(), false).await().indefinitely();

        assertThat(page.props()).containsKey("errors");
        assertThat(page.props().get("errors")).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) page.props().get("errors")).isEmpty();
    }
}
