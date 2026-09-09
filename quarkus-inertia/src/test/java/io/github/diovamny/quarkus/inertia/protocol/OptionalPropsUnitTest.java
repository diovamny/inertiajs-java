package io.github.diovamny.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.smallrye.mutiny.Uni;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.spi.FlashStore;
import io.github.diovamny.quarkus.inertia.version.VersionProvider;

class OptionalPropsUnitTest {

    private PageObjectBuilder builder;
    private SharedDataRegistry sharedData;
    private HttpServerRequest httpRequest;
    private AtomicInteger calls;

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
        when(config.alwaysIncludeErrors()).thenReturn(true);

        var routingContext = mock(RoutingContext.class);
        httpRequest = mock(HttpServerRequest.class);
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

        calls = new AtomicInteger(0);
        sharedData.addOptionalProp("expensive", () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("resolved-expensive");
        });
    }

    private void asPartial(String dataHeaders) {
        when(httpRequest.getHeader("X-Inertia-Partial-Component")).thenReturn("Component");
        when(httpRequest.getHeader("X-Inertia-Partial-Data")).thenReturn(dataHeaders);
    }

    @Test
    void shouldOmitOptionalPropOnFullLoad() {
        var page = builder.build("Component", Map.of(), false).await().indefinitely();

        assertThat(page.props()).doesNotContainKey("expensive");
        assertThat(calls.get()).isZero();
    }

    @Test
    void shouldResolveOptionalPropOnlyWhenExplicitlyRequested() {
        asPartial("expensive");

        var page = builder.build("Component", Map.of("base", "x"), true).await().indefinitely();

        assertThat(page.props()).containsOnlyKeys("expensive", "errors");
        assertThat(page.props()).containsEntry("expensive", "resolved-expensive");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void shouldNotResolveOptionalPropWhenOtherDataRequested() {
        asPartial("other");

        var page = builder.build("Component", Map.of(), true).await().indefinitely();

        assertThat(page.props()).doesNotContainKey("expensive");
        assertThat(calls.get()).isZero();
    }

    @Test
    void shouldResolveOptionalPropWhenNotExcludedByExcept() {
        when(httpRequest.getHeader("X-Inertia-Partial-Component")).thenReturn("Component");
        when(httpRequest.getHeader("X-Inertia-Partial-Except")).thenReturn("base");

        var page = builder.build("Component", Map.of("base", "x"), true).await().indefinitely();

        assertThat(page.props()).containsEntry("expensive", "resolved-expensive");
        assertThat(calls.get()).isEqualTo(1);
    }
}
