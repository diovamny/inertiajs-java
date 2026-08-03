package com.quarkus.inertia.protocol;

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

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.spi.FlashStore;
import com.quarkus.inertia.version.VersionProvider;

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
    void shouldNotResolveOptionalPropOnExceptOnlyPartial() {
        when(httpRequest.getHeader("X-Inertia-Partial-Component")).thenReturn("Component");
        when(httpRequest.getHeader("X-Inertia-Partial-Except")).thenReturn("base");

        var page = builder.build("Component", Map.of("base", "x"), true).await().indefinitely();

        assertThat(page.props()).doesNotContainKey("expensive");
        assertThat(calls.get()).isZero();
    }
}