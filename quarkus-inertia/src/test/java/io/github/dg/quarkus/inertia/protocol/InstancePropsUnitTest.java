package io.github.dg.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.config.InertiaConfig;
import io.github.dg.quarkus.inertia.spi.FlashStore;
import io.github.dg.quarkus.inertia.version.VersionProvider;

class InstancePropsUnitTest {

    public static class UserForm {
        private final long id = 42;
        private final String name = "John";

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSecret() {
            return null;
        }
    }

    private PageObjectBuilder newBuilder() {
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
        when(httpRequest.uri()).thenReturn("/form");
        var currentVertxRequest = mock(CurrentVertxRequest.class);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        return new PageObjectBuilder(
            new SharedDataRegistry(),
            versionProvider,
            new PartialReloadProcessor(),
            new OncePropRegistry(),
            new MergePropProcessor(),
            flashStore,
            currentVertxRequest,
            config);
    }

    @Test
    void shouldShareInstancePropsWhenNoManualProps() throws Exception {
        var vertx = Vertx.vertx();
        try {
            var builder = newBuilder();
            var future = new CompletableFuture<Map<String, Object>>();
            vertx.runOnContext(v -> {
                io.vertx.core.Vertx.currentContext().putLocal("inertia-instance-props", new UserForm());
                future.complete(builder.build("Home", Map.of()).await().indefinitely().props());
            });
            var props = future.get(5, TimeUnit.SECONDS);

            assertThat(props).containsEntry("id", 42L);
            assertThat(props).containsEntry("name", "John");
            assertThat(props).doesNotContainKey("secret");
            assertThat(props).doesNotContainKey("class");
        } finally {
            vertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void shouldSkipInstancePropsWhenManualPropsPassed() throws Exception {
        var vertx = Vertx.vertx();
        try {
            var builder = newBuilder();
            var future = new CompletableFuture<Map<String, Object>>();
            vertx.runOnContext(v -> {
                io.vertx.core.Vertx.currentContext().putLocal("inertia-instance-props", new UserForm());
                future.complete(builder.build("Home", Map.of("manual", "value")).await().indefinitely().props());
            });
            var props = future.get(5, TimeUnit.SECONDS);

            assertThat(props).containsEntry("manual", "value");
            assertThat(props).doesNotContainKey("id");
            assertThat(props).doesNotContainKey("name");
        } finally {
            vertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        }
    }
}
