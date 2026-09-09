package io.github.diovamny.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.spi.FlashStore;
import io.github.diovamny.quarkus.inertia.version.VersionProvider;

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

    @Test
    void shouldEmitOnlyActuallyRescuedProps() {
        sharedData.addRescuedProp("flaky");
        sharedData.addRescuedProp("ok");

        var page = builder.build("Users", Map.of(
            "flaky", (java.util.function.Supplier<io.smallrye.mutiny.Uni<Object>>) ()
                -> io.smallrye.mutiny.Uni.createFrom().failure(new IllegalStateException("boom")),
            "ok", (java.util.function.Supplier<io.smallrye.mutiny.Uni<Object>>) ()
                -> io.smallrye.mutiny.Uni.createFrom().item("fine")
        ), false).await().indefinitely();

        assertThat(page.rescuedProps()).containsExactly("flaky");
        assertThat(page.props()).doesNotContainKey("flaky");
        assertThat(page.props()).containsEntry("ok", "fine");
    }

    @Test
    void shouldNotEmitRescuedPropsWhenCandidatesSucceed() {
        sharedData.addRescuedProp("ok");

        var page = builder.build("Users", Map.of(
            "ok", (java.util.function.Supplier<io.smallrye.mutiny.Uni<Object>>) ()
                -> io.smallrye.mutiny.Uni.createFrom().item("fine")
        ), false).await().indefinitely();

        assertThat(page.rescuedProps()).isNullOrEmpty();
        assertThat(page.props()).containsEntry("ok", "fine");
    }

    @Test
    void shouldPruneDottedMergeKeysWhenParentIsReset() throws Exception {
        sharedData.merge("contacts.data", java.util.List.of(1), false, "id");

        var vertx = io.vertx.core.Vertx.vertx();
        var ctx = vertx.getOrCreateContext();
        ctx.putLocal("inertia-reset", "contacts");
        var latch = new CountDownLatch(1);
        var ref = new AtomicReference<io.github.diovamny.quarkus.inertia.model.PageObject>();
        ctx.runOnContext(v -> {
            try {
                ref.set(builder.build("Users", Map.of(), false).await().indefinitely());
            } finally {
                latch.countDown();
            }
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(ref.get().mergeProps()).isNullOrEmpty();
    }
}
