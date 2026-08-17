package io.github.dg.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.config.InertiaConfig;
import io.github.dg.quarkus.inertia.spi.FlashStore;
import io.github.dg.quarkus.inertia.version.VersionProvider;

class PageObjectBuilderDotNotationUnitTest {

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

        var routingContext = mock(RoutingContext.class);
        var httpRequest = mock(HttpServerRequest.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(httpRequest.uri()).thenReturn("/users");
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
    void shouldExpandDotNotationKeysIntoNestedProps() {
        var page = builder.build("Users", Map.of(
            "auth.user.id", 1,
            "auth.user.name", "John",
            "auth.email", "john@test.com"
        ), false).await().indefinitely();

        assertThat(page.props().get("auth")).isEqualTo(Map.of(
            "user", Map.of("id", 1, "name", "John"),
            "email", "john@test.com"
        ));
    }

    @Test
    void shouldMergeDottedKeysWithExistingNestedMap() {
        var page = builder.build("Users", Map.of(
            "auth", Map.of("email", "john@test.com"),
            "auth.user.id", 1
        ), false).await().indefinitely();

        assertThat(page.props().get("auth")).isEqualTo(Map.of(
            "user", Map.of("id", 1),
            "email", "john@test.com"
        ));
    }

    @Test
    void shouldKeepErrorsAlwaysInNestedPartialReload() {
        sharedData.set("errors", Map.of("name", "Required"));

        var page = builder.build("Users", Map.of("auth", Map.of("user", Map.of("id", 1), "email", "x")), false)
            .await().indefinitely();

        assertThat(page.props()).containsKey("errors");
        assertThat(page.props().get("errors")).isEqualTo(Map.of("name", "Required"));
    }
}
