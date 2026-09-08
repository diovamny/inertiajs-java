package io.github.dg.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.smallrye.mutiny.Uni;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.config.InertiaConfig;
import io.github.dg.quarkus.inertia.spi.FlashStore;
import io.github.dg.quarkus.inertia.version.VersionProvider;

class OnceLazyShareOnceUnitTest {

    private PageObjectBuilder builder;
    private SharedDataRegistry sharedData;
    private OncePropRegistry onceRegistry;
    private AtomicInteger calls;

    @BeforeEach
    void setUp() {
        sharedData = new SharedDataRegistry();
        onceRegistry = new OncePropRegistry();

        var versionProvider = mock(VersionProvider.class);
        when(versionProvider.getVersion()).thenReturn("1.0.0");

        var flashStore = mock(FlashStore.class);
        when(flashStore.hasData()).thenReturn(false);

        var config = mock(InertiaConfig.class);
        when(config.encryptHistory()).thenReturn(false);
        when(config.camelizeProps()).thenReturn(false);

        var routingContext = mock(RoutingContext.class);
        var httpRequest = mock(HttpServerRequest.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(httpRequest.uri()).thenReturn("/once");

        var currentVertxRequest = mock(CurrentVertxRequest.class);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        builder = new PageObjectBuilder(
            sharedData,
            versionProvider,
            new PartialReloadProcessor(),
            onceRegistry,
            new MergePropProcessor(),
            flashStore,
            currentVertxRequest,
            config);

        calls = new AtomicInteger(0);
    }

    @Test
    void shouldResolveLazyOncePropOnFullLoad() {
        onceRegistry.setLazy("countries", () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("latam");
        });

        var page = builder.build("Home", Map.of(), false).await().indefinitely();

        assertThat(page.props()).containsEntry("countries", "latam");
        assertThat(page.onceProps()).containsKey("countries");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void shouldResolveLazyOnceWithCustomKeyAndExpiry() {
        onceRegistry.setLazy("auth_user", ()
            -> Uni.createFrom().item(Map.of("id", 1)), "user", null);

        var page = builder.build("Home", Map.of(), false).await().indefinitely();

        assertThat(page.props()).containsEntry("auth_user", Map.of("id", 1));
        assertThat(page.onceProps()).containsOnlyKeys("user");
    }

    @Test
    void shouldIncludeSharedValueAsOncePropInSharedPropsMetadata() {
        sharedData.set("countries", "draft");
        onceRegistry.set("countries", "resolved");
        sharedData.addOncePropKey("countries", "countries");

        var page = builder.build("Home", Map.of(), false).await().indefinitely();

        assertThat(page.props()).containsEntry("countries", "resolved");
        assertThat(page.sharedProps()).contains("countries");
        assertThat(page.onceProps()).containsKey("countries");
    }
}
