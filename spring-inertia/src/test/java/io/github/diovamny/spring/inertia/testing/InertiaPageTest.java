package io.github.diovamny.spring.inertia.testing;

import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InertiaPageTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private static InertiaPage nestedPage() {
        var json = """
            {"component":"Users/Index",
             "props":{"users":{"total":50,"data":[{"name":"Alice"},{"name":"Bob"}]},
                      "role":"admin","secret":"x"},
             "url":"/users","version":"1.0.0"}
            """;
        return InertiaPage.fromJson(json, MAPPER);
    }

    @Test
    void whereAssertsDeepPaths() {
        var page = nestedPage();
        page.where("users.total", 50);
        page.where("users.data[0].name", "Alice");
        page.where("users.data[1].name", "Bob");
        assertThrows(AssertionError.class, () -> page.where("users.data[0].name", "Mallory"));
        assertThrows(AssertionError.class, () -> page.where("users.missing", "x"));
    }

    @Test
    void hasAssertsDeepCounts() {
        var page = nestedPage();
        page.has("users.data", 2);
        page.has("users", 2);
        assertThrows(AssertionError.class, () -> page.has("users.data", 3));
        assertThrows(AssertionError.class, () -> page.has("role", 1));
    }

    @Test
    void missingAssertsDeepAbsence() {
        var page = nestedPage();
        page.missing("users.data[0].age");
        page.missing("nope.nothing");
        assertThrows(AssertionError.class, () -> page.missing("role"));
        assertThrows(AssertionError.class, () -> page.missing("users.data[0].name"));
    }

    @Test
    void dumpDiffPassesWhenEqualAndFailsWithDiff() {
        var page = nestedPage();
        page.dumpDiff(nestedPage());
        var other = InertiaPage.fromJson("""
            {"component":"Users/Index","props":{"users":{"total":51},"role":"admin"},
             "url":"/users","version":"1.0.0"}
            """, MAPPER);
        var error = assertThrows(AssertionError.class, () -> page.dumpDiff(other));
        assertTrue(error.getMessage().contains("users.total"));
    }

    @Test
    void parsesPagePayload() {
        var json = """
            {"component":"Dashboard","props":{"title":"Home","count":3},"url":"/dashboard","version":"v1"}
            """;
        var page = InertiaPage.fromJson(json, MAPPER);
        assertEquals("Dashboard", page.component());
        assertEquals("/dashboard", page.url());
        assertEquals("v1", page.version());
        assertEquals("Home", page.prop("title"));
        assertEquals(3, page.prop("count"));
        assertTrue(page.hasProp("title"));
        assertFalse(page.hasProp("missing"));
    }

    @Test
    void fluentAssertionsPassWhenValid() {
        var json = """
            {
              "component": "Users/Index",
              "props": { "users": ["Alice", "Bob"], "role": "admin" },
              "url": "/users",
              "version": "1.0.0",
              "flash": { "success": "Saved" },
              "deferredProps": { "default": ["metrics"] },
              "mergeProps": ["users"],
              "prependProps": ["alerts"],
              "deepMergeProps": ["settings"],
              "matchPropsOn": ["users.id"],
              "onceProps": { "token": { "prop": "token", "expiresAt": 123456 } },
              "scrollProps": { "feed": { "merge": true } },
              "rescuedProps": ["failedProp"],
              "meta": { "title": "User List" },
              "encryptHistory": true,
              "clearHistory": false,
              "preserveFragment": true
            }
            """;
        var page = InertiaPage.fromJson(json, MAPPER);

        page.assertComponent("Users/Index")
            .assertUrl("/users")
            .assertVersion("1.0.0")
            .assertProp("role", "admin")
            .assertHasProps("users", "role")
            .assertHasProps(Map.of("role", "admin"))
            .assertNoProp("nonExistent")
            .assertDeferredProps("metrics")
            .assertDeferredPropsInGroup("default", "metrics")
            .assertMergeProps("users")
            .assertPrependProps("alerts")
            .assertDeepMergeProps("settings")
            .assertMatchPropsOn("users.id")
            .assertOnceProps("token")
            .assertScrollProps("feed")
            .assertRescuedProps("failedProp")
            .assertMeta("title", "User List")
            .assertEncryptHistory(true)
            .assertClearHistory(false)
            .assertPreserveFragment(true)
            .assertFlash("success", "Saved");
    }

    @Test
    void fluentAssertionsFailWhenInvalid() {
        var json = """
            {"component":"Dashboard","props":{"count":5},"url":"/dashboard"}
            """;
        var page = InertiaPage.fromJson(json, MAPPER);

        assertThrows(AssertionError.class, () -> page.assertComponent("Other"));
        assertThrows(AssertionError.class, () -> page.assertUrl("/other"));
        assertThrows(AssertionError.class, () -> page.assertProp("count", 10));
        assertThrows(AssertionError.class, () -> page.assertNoProp("count"));
        assertThrows(AssertionError.class, () -> page.assertDeferredProps("missingDeferred"));
    }

    @Test
    void toleratesMissingVersion() {
        var page = InertiaPage.fromJson("{\"component\":\"C\",\"props\":{},\"url\":\"/\"}", MAPPER);
        assertEquals(null, page.version());
    }

    @Test
    void rejectsInvalidPayload() {
        assertThrows(IllegalArgumentException.class,
            () -> InertiaPage.fromJson("not json", MAPPER));
    }
}
