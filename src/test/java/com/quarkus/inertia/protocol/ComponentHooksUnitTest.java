package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.enterprise.inject.Instance;
import io.smallrye.mutiny.Uni;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.spi.ComponentTransformer;
import com.quarkus.inertia.spi.FlashStore;
import com.quarkus.inertia.spi.UrlResolver;
import com.quarkus.inertia.version.VersionProvider;

class ComponentHooksUnitTest {

    private SharedDataRegistry sharedData;
    private InertiaConfig config;

    @BeforeEach
    void setUp() {
        sharedData = new SharedDataRegistry();

        var versionProvider = mock(VersionProvider.class);
        when(versionProvider.getVersion()).thenReturn("1.0.0");

        var flashStore = mock(FlashStore.class);
        when(flashStore.hasData()).thenReturn(false);

        config = mock(InertiaConfig.class);
        when(config.encryptHistory()).thenReturn(false);
        when(config.camelizeProps()).thenReturn(false);
    }

    @SuppressWarnings("unchecked")
    private PageObjectBuilder builderWith(Instance<ComponentTransformer> transformers,
            Instance<UrlResolver> resolvers) {
        var routingContext = mock(RoutingContext.class);
        var httpRequest = mock(HttpServerRequest.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(httpRequest.uri()).thenReturn("/hooks");

        var currentVertxRequest = mock(CurrentVertxRequest.class);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        var versionProvider = mock(VersionProvider.class);
        when(versionProvider.getVersion()).thenReturn("1.0.0");

        var flashStore = mock(FlashStore.class);
        when(flashStore.hasData()).thenReturn(false);

        return new PageObjectBuilder(
            sharedData,
            versionProvider,
            new PartialReloadProcessor(),
            new OncePropRegistry(),
            new MergePropProcessor(),
            flashStore,
            currentVertxRequest,
            config,
            transformers,
            resolvers);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldTransformComponentViaHook() {
        var transformer = mock(ComponentTransformer.class);
        when(transformer.transform("home")).thenReturn("Home/Index");
        var instance = mock(Instance.class);
        when(instance.isResolvable()).thenReturn(true);
        when(instance.get()).thenReturn(transformer);

        var builder = builderWith(instance, mock(Instance.class));
        var page = builder.build("home", java.util.Map.of(), false).await().indefinitely();

        assertThat(page.component()).isEqualTo("Home/Index");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldResolveUrlViaHook() {
        var resolver = mock(UrlResolver.class);
        when(resolver.resolve("/hooks")).thenReturn("/hooks?lang=es");
        var instance = mock(Instance.class);
        when(instance.isResolvable()).thenReturn(true);
        when(instance.get()).thenReturn(resolver);

        var builder = builderWith(mock(Instance.class), instance);
        var page = builder.build("home", java.util.Map.of(), false).await().indefinitely();

        assertThat(page.url()).isEqualTo("/hooks?lang=es");
    }
}
