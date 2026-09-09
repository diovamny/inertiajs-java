package io.github.dg.spring.inertia.testing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import io.github.dg.spring.inertia.integration.TestApplication;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class InertiaPageDslIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void canPerformActivePartialReloadAndDeferredLoading() {
        // 1. Initial request through InertiaPage factory
        InertiaPage initialPage = InertiaPage.from(mockMvc, "/deferred");

        initialPage.assertComponent("DeferredPage")
                   .assertProp("title", "t")
                   .assertMissing("data")
                   .assertDeferredProps("data");

        // 2. Active loadDeferredProps with callback
        InertiaPage reloaded = initialPage.loadDeferredProps("slow", page -> {
            page.assertComponent("DeferredPage")
                .assertProp("data", "lazy-value")
                .assertMissing("title");
        });

        assertNotNull(reloaded);
        assertEquals("lazy-value", reloaded.prop("data"));

        // 3. Active reloadOnly
        InertiaPage onlyTitle = initialPage.reloadOnly("title");
        onlyTitle.assertPropExists("title")
                 .assertMissing("data");

        // 4. Active reloadExcept
        InertiaPage exceptTitle = initialPage.reloadExcept("title");
        exceptTitle.assertMissing("title");
    }

    @Test
    void assertionHelpersSupportCollectionsAndNestedMaps() {
        InertiaPage page = new InertiaPage(
            "Test/Page",
            Map.of(
                "users", List.of("Alice", "Bob", "Charlie"),
                "settings", Map.of("theme", "dark", "notifications", true)
            ),
            "/test",
            "v1",
            null
        );

        page.assertComponent("Test/Page")
            .assertPropCount("users", 3)
            .assertMissing("unknownProp")
            .assertPropMap("settings", map -> {
                assertEquals("dark", map.get("theme"));
                assertEquals(true, map.get("notifications"));
            });
    }
}
