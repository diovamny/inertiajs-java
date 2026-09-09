package io.github.diovamny.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.spi.FlashStore;
import io.github.diovamny.quarkus.inertia.version.VersionProvider;

class MergePropsBuilderUnitTest {

    private PageObjectBuilder builder;
    private SharedDataRegistry sharedData;

    @BeforeEach
    void setUp() {
        sharedData = new SharedDataRegistry();

        var versionProvider = mock(VersionProvider.class);
        when(versionProvider.getVersion()).thenReturn("1.0.0");

        var flashStore = mock(FlashStore.class);
        when(flashStore.hasData()).thenReturn(false);

        var config = mock(InertiaConfig.class);
        when(config.encryptHistory()).thenReturn(false);
        when(config.camelizeProps()).thenReturn(false);

        var httpRequest = mock(HttpServerRequest.class);
        var routingContext = mock(RoutingContext.class);
        when(routingContext.request()).thenReturn(httpRequest);

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
    void mergeWithMatchOnEmitsMergeAndMatchMetadata() {
        sharedData.merge("items", java.util.List.of(1), true, "id");

        var page = builder.build("Component", Map.of(), false).await().indefinitely();

        assertThat(page.mergeProps()).containsExactly("items");
        assertThat(page.deepMergeProps()).containsExactly("items");
        assertThat(page.matchPropsOn()).containsExactly("items.id");
        assertThat(page.props()).containsEntry("items", java.util.List.of(1));
    }

    @Test
    void mergeWithoutMatchOnOmitsMatchMetadata() {
        sharedData.merge("items", java.util.List.of(1), false);

        var page = builder.build("Component", Map.of(), false).await().indefinitely();

        assertThat(page.mergeProps()).containsExactly("items");
        assertThat(page.matchPropsOn()).isNull();
    }
}
