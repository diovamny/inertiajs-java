package io.github.diovamny.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.diovamny.quarkus.inertia.api.ProvidesInertiaProperties;
import io.github.diovamny.quarkus.inertia.api.RenderContext;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.model.PageObject;
import io.github.diovamny.quarkus.inertia.spi.FlashStore;
import io.github.diovamny.quarkus.inertia.version.VersionProvider;

class ProvidesInertiaPropertiesUnitTest {

    private PageObjectBuilder builder;
    private SharedDataRegistry sharedData;
    private HttpServerRequest httpRequest;

    record UserProfileDto(String name, String email) implements ProvidesInertiaProperties {
        @Override
        public Map<String, Object> toInertiaProperties(RenderContext context) {
            Map<String, Object> props = new HashMap<>();
            props.put("name", name);
            props.put("email", email);
            if (context.isPropRequested("permissions")) {
                props.put("permissions", "ADMIN");
            }
            return props;
        }
    }

    record GlobalSettingsDto(String appName, String theme) implements ProvidesInertiaProperties {
        @Override
        public Map<String, Object> toInertiaProperties(RenderContext context) {
            return Map.of("appName", appName, "theme", theme);
        }
    }

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
            config
        );
    }

    @Test
    void renderContextEvaluatesPropertiesCorrectly() {
        var ctxFull = new RenderContext("Users/Index", "/users", false, Set.of(), Set.of());
        assertThat(ctxFull.isPartial()).isFalse();
        assertThat(ctxFull.isPropRequested("anyProp")).isTrue();

        var ctxPartial = new RenderContext("Users/Index", "/users", true, Set.of("user", "stats"), Set.of("stats"));
        assertThat(ctxPartial.isPartial()).isTrue();
        assertThat(ctxPartial.isPropRequested("user")).isTrue();
        assertThat(ctxPartial.isPropRequested("stats")).isFalse();
        assertThat(ctxPartial.isPropRequested("unknown")).isFalse();
    }

    @Test
    void directProviderResolvesInSyncPageObject() {
        UserProfileDto dto = new UserProfileDto("Alice", "alice@example.com");
        PageObject page = builder.buildSync("Users/Profile", dto.toInertiaProperties(
            new RenderContext("Users/Profile", "/users", true, Set.of("name", "email"), Set.of())
        ));

        assertThat(page.component()).isEqualTo("Users/Profile");
        assertThat(page.props()).containsEntry("name", "Alice");
        assertThat(page.props()).containsEntry("email", "alice@example.com");
        assertThat(page.props()).doesNotContainKey("permissions");
    }

    @Test
    void nestedProviderInsidePropsMapResolvesRecursivelySync() {
        UserProfileDto dto = new UserProfileDto("Bob", "bob@example.com");
        Map<String, Object> props = Map.of(
            "user", dto,
            "count", 10
        );

        PageObject page = builder.buildSync("Dashboard", props);

        assertThat(page.component()).isEqualTo("Dashboard");
        assertThat(page.props()).containsEntry("count", 10);

        @SuppressWarnings("unchecked")
        Map<String, Object> resolvedUser = (Map<String, Object>) page.props().get("user");
        assertThat(resolvedUser).isNotNull();
        assertThat(resolvedUser).containsEntry("name", "Bob");
        assertThat(resolvedUser).containsEntry("email", "bob@example.com");
    }

    @Test
    void nestedProviderInsidePropsMapResolvesRecursivelyReactive() {
        UserProfileDto dto = new UserProfileDto("Bob", "bob@example.com");
        Map<String, Object> props = Map.of(
            "user", dto,
            "count", 10
        );

        PageObject page = builder.build("Dashboard", props).await().indefinitely();

        assertThat(page.component()).isEqualTo("Dashboard");
        assertThat(page.props()).containsEntry("count", 10);

        @SuppressWarnings("unchecked")
        Map<String, Object> resolvedUser = (Map<String, Object>) page.props().get("user");
        assertThat(resolvedUser).isNotNull();
        assertThat(resolvedUser).containsEntry("name", "Bob");
        assertThat(resolvedUser).containsEntry("email", "bob@example.com");
    }

    @Test
    void sharedProviderIsResolvedInPageObject() {
        sharedData.addSharedProvider(new GlobalSettingsDto("QuarkusPlatform", "dark"));

        PageObject page = builder.buildSync("Home", Map.of("content", "Hello Quarkus"));

        assertThat(page.component()).isEqualTo("Home");
        assertThat(page.props()).containsEntry("content", "Hello Quarkus");
        assertThat(page.props()).containsEntry("appName", "QuarkusPlatform");
        assertThat(page.props()).containsEntry("theme", "dark");
    }

    @Test
    void partialReloadAffectsContextAwareProvider() {
        when(httpRequest.getHeader("X-Inertia-Partial-Component")).thenReturn("Users/Profile");
        when(httpRequest.getHeader("X-Inertia-Partial-Data")).thenReturn("user,permissions");

        UserProfileDto dto = new UserProfileDto("Charlie", "charlie@example.com");
        Map<String, Object> props = Map.of("user", dto);

        PageObject page = builder.buildSync("Users/Profile", props);

        @SuppressWarnings("unchecked")
        Map<String, Object> resolvedUser = (Map<String, Object>) page.props().get("user");
        assertThat(resolvedUser).isNotNull();
        assertThat(resolvedUser).containsEntry("permissions", "ADMIN");
    }
}
