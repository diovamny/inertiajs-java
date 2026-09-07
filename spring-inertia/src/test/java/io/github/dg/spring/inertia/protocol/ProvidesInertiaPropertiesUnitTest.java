package io.github.dg.spring.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.dg.spring.inertia.api.ProvidesInertiaProperties;
import io.github.dg.spring.inertia.api.RenderContext;
import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.version.VersionProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProvidesInertiaPropertiesUnitTest {

    private MockHttpServletRequest request;
    private SharedDataRegistry sharedDataRegistry;
    private PageObjectBuilder pageObjectBuilder;

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
        request = new MockHttpServletRequest();
        request.setRequestURI("/test-url");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));

        sharedDataRegistry = new SharedDataRegistry();
        var partialReloadProcessor = new PartialReloadProcessor();
        var oncePropRegistry = new OncePropRegistry();
        var mergePropProcessor = new MergePropProcessor();

        pageObjectBuilder = new PageObjectBuilder(
            new InertiaProperties(),
            sharedDataRegistry,
            oncePropRegistry,
            partialReloadProcessor,
            mergePropProcessor,
            new io.github.dg.spring.inertia.internal.SpringFlashStore(),
            () -> "test-version",
            null,
            null
        );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void renderContextEvaluatesPropertiesCorrectly() {
        var ctxFull = new RenderContext("Users/Index", "/users", false, Set.of(), Set.of());
        assertFalse(ctxFull.isPartial());
        assertTrue(ctxFull.isPropRequested("anyProp"));

        var ctxPartial = new RenderContext("Users/Index", "/users", true, Set.of("user", "stats"), Set.of("stats"));
        assertTrue(ctxPartial.isPartial());
        assertTrue(ctxPartial.isPropRequested("user"));
        assertFalse(ctxPartial.isPropRequested("stats"));
        assertFalse(ctxPartial.isPropRequested("unknown"));
    }

    @Test
    void directProviderResolvesInPageObject() {
        UserProfileDto dto = new UserProfileDto("Alice", "alice@example.com");
        PageObject page = pageObjectBuilder.build("Users/Profile", dto.toInertiaProperties(
            new RenderContext("Users/Profile", "/test-url", true, Set.of("name", "email"), Set.of())
        ), false);

        assertEquals("Users/Profile", page.component());
        assertEquals("Alice", page.props().get("name"));
        assertEquals("alice@example.com", page.props().get("email"));
        assertFalse(page.props().containsKey("permissions"));
    }

    @Test
    void nestedProviderInsidePropsMapResolvesRecursively() {
        UserProfileDto dto = new UserProfileDto("Bob", "bob@example.com");
        Map<String, Object> props = Map.of(
            "user", dto,
            "count", 10
        );

        PageObject page = pageObjectBuilder.build("Dashboard", props, false);

        assertEquals("Dashboard", page.component());
        assertEquals(10, page.props().get("count"));

        @SuppressWarnings("unchecked")
        Map<String, Object> resolvedUser = (Map<String, Object>) page.props().get("user");
        assertNotNull(resolvedUser);
        assertEquals("Bob", resolvedUser.get("name"));
        assertEquals("bob@example.com", resolvedUser.get("email"));
    }

    @Test
    void sharedProviderIsResolvedInPageObject() {
        sharedDataRegistry.addSharedProvider(new GlobalSettingsDto("MyPlatform", "dark"));

        PageObject page = pageObjectBuilder.build("Home", Map.of("content", "Hello World"), false);

        assertEquals("Home", page.component());
        assertEquals("Hello World", page.props().get("content"));
        assertEquals("MyPlatform", page.props().get("appName"));
        assertEquals("dark", page.props().get("theme"));
    }

    @Test
    void partialReloadAffectsContextAwareProvider() {
        request.setAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT, "Users/Profile");
        request.setAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA, "user,permissions");

        UserProfileDto dto = new UserProfileDto("Charlie", "charlie@example.com");
        Map<String, Object> props = Map.of("user", dto);

        PageObject page = pageObjectBuilder.build("Users/Profile", props, false);

        @SuppressWarnings("unchecked")
        Map<String, Object> resolvedUser = (Map<String, Object>) page.props().get("user");
        assertNotNull(resolvedUser);
        assertEquals("ADMIN", resolvedUser.get("permissions"));
    }
}
