package io.github.dg.spring.inertia.integration;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.dg.spring.inertia.testing.InertiaResultMatchers.inertia;

/**
 * Covers the metadata features added for the kitchen-sink demo: scroll props,
 * rescued deferred props, deferred filtering, custom-key once props, fragment
 * preservation and merge match-on fields.
 */
@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class FeatureMetadataIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetCounters() {
        TestController.deferredHeavyResolved = 0;
    }

    @Test
    void deferredPartialReloadOnlyResolvesRequestedProp() throws Exception {
        mockMvc.perform(get("/deferred-filter")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "DeferredPage")
                .header("X-Inertia-Partial-Data", "slow"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("slow", equalTo("slow-value")))
            .andExpect(inertia().prop("heavy", nullValue()))
            .andExpect(inertia().prop("title", nullValue()));

        if (TestController.deferredHeavyResolved != 0) {
            throw new AssertionError("Non-requested deferred prop 'heavy' was resolved");
        }
    }

    @Test
    void rescuedDeferredFailureOmittedFromResponseAndMarked() throws Exception {
        mockMvc.perform(get("/deferred-rescue")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "DeferredPage")
                .header("X-Inertia-Partial-Data", "flakyReport"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("flakyReport", nullValue()));

        var page = page("/deferred-rescue",
            Map.of("X-Inertia-Partial-Component", "DeferredPage", "X-Inertia-Partial-Data", "flakyReport"));
        if (!List.of("flakyReport").equals(strings(page.path("rescuedProps")))) {
            throw new AssertionError("Expected rescuedProps to contain flakyReport but was "
                + page.path("rescuedProps"));
        }
    }

    @Test
    void rescuedDeferredSuccessDeliveredAndNotMarked() throws Exception {
        mockMvc.perform(get("/deferred-rescue")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "DeferredPage")
                .header("X-Inertia-Partial-Data", "flakyReport")
                .header("X-Force-Success", "true"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("flakyReport", equalTo("ok")));

        var page = page("/deferred-rescue",
            Map.of("X-Inertia-Partial-Component", "DeferredPage", "X-Inertia-Partial-Data", "flakyReport",
                "X-Force-Success", "true"));
        if (page.has("rescuedProps")) {
            throw new AssertionError("Expected no rescuedProps on success but was " + page.path("rescuedProps"));
        }
    }

    @Test
    void scrollPropEmitsValueMetadataAndMergePath() throws Exception {
        var page = page("/scroll", Map.of());
        if (!page.path("props.contacts.data").isArray() || page.path("props.contacts.data").size() != 1) {
            throw new AssertionError("Expected contacts value to be delivered in props, was " + page.path("props.contacts"));
        }
        var scrollProps = page.path("scrollProps.contacts");
if (!scrollProps.isObject()
                || !"cursor".equals(scrollProps.path("pageName").asText())
                || !"abc".equals(scrollProps.path("nextPage").asText())
                || scrollProps.path("currentPage").asInt() != 1
                || scrollProps.path("reset").asBoolean(false)) {
            throw new AssertionError("Unexpected scrollProps: " + scrollProps);
        }
        if (!List.of("contacts.data").equals(strings(page.path("mergeProps")))) {
            throw new AssertionError("Expected mergeProps [contacts.data] but was " + page.path("mergeProps"));
        }
        if (!List.of("contacts.data.id").equals(strings(page.path("matchPropsOn")))) {
            throw new AssertionError("Expected matchPropsOn [contacts.data.id] but was " + page.path("matchPropsOn"));
        }
    }

    @Test
    void oncePropWithCustomKeyTracksSharedHistory() throws Exception {
        var session = new MockHttpSession();

        var first = page("/once-custom", Map.of(), session);
        if (!"v".equals(first.path("props.aliased").asText())) {
            throw new AssertionError("Expected aliased once prop delivered on first visit");
        }
        if (!first.path("onceProps").has("shared-key")) {
            throw new AssertionError("Expected onceProps keyed by custom key, was " + first.path("onceProps"));
        }

        var second = page("/once-custom", Map.of(), session);
        if (second.has("props.aliased")) {
            throw new AssertionError("Expected aliased once prop omitted after shared-key was shown");
        }
    }

    @Test
    void preserveFragmentIsEmitted() throws Exception {
        var page = page("/preserve-fragment", Map.of());
        if (!page.path("preserveFragment").asBoolean(false)) {
            throw new AssertionError("Expected preserveFragment true in page metadata");
        }
    }

    @Test
    void mergeWithMatchOnEmitsMatchPropsOn() throws Exception {
        var page = page("/merge-match", Map.of());
        if (!List.of("contacts.id").equals(strings(page.path("matchPropsOn")))) {
            throw new AssertionError("Expected matchPropsOn [contacts.id] but was " + page.path("matchPropsOn"));
        }
    }

    private static List<String> strings(tools.jackson.databind.JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        var out = new java.util.ArrayList<String>();
        for (var item : node) {
            if (item.isValueNode()) {
                out.add(item.asText());
            }
        }
        return out;
    }

private PageJson page(String url, Map<String, String> extraHeaders) throws Exception {
        return page(url, extraHeaders, new MockHttpSession());
    }

    private PageJson page(String url, Map<String, String> extraHeaders, MockHttpSession session) throws Exception {
        var request = get(url).session(session)
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "test-version");
        for (var entry : extraHeaders.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        var body = new String(mockMvc.perform(request).andReturn().getResponse().getContentAsByteArray(),
            java.nio.charset.StandardCharsets.UTF_8);
        try {
            return new PageJson(JSON.readTree(body));
        } catch (Exception e) {
            throw new AssertionError("Response is not a page payload: " + body, e);
        }
    }

    private record PageJson(tools.jackson.databind.JsonNode node) {
        tools.jackson.databind.JsonNode path(String pointer) {
            return node.at("/" + pointer.replace(".", "/"));
        }

        boolean has(String pointer) {
            return path(pointer).isMissingNode() == false;
        }
    }
}
