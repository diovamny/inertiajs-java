package io.github.dg.spring.inertia.protocol;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MergePropProcessorTest {

    private final MergePropProcessor processor = new MergePropProcessor();
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
        processor.reset();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void mergesListOfMapsByMatchKey() {
        var previous = List.of(Map.of("id", 1, "name", "a"), Map.of("id", 2, "name", "b"));
        var current = List.of(Map.of("id", 2, "name", "B"), Map.of("id", 3, "name", "c"));
        processor.propagateProps(Map.of("users", previous));
        var merged = processor.mergeProps(Map.of("users", current), List.of("users"), List.of(), List.of(), List.of("id"));
        var users = merged.get("users");
        assertEquals(3, ((List<?>) users).size());
    }

    @Test
    void prependsListOfMaps() {
        var previous = List.of(Map.of("id", 1, "name", "a"));
        var current = List.of(Map.of("id", 2, "name", "b"));
        processor.propagateProps(Map.of("items", previous));
        var merged = processor.mergeProps(Map.of("items", current), List.of(), List.of("items"), List.of(), List.of("id"));
        var items = (List<?>) merged.get("items");
        assertEquals(2, items.size());
        assertEquals(1, ((Map<?, ?>) items.get(0)).get("id"));
        assertEquals(2, ((Map<?, ?>) items.get(1)).get("id"));
    }

    @Test
    void deepMergesMaps() {
        processor.propagateProps(Map.of("filters", Map.of("search", "a", "page", 1)));
        var merged = processor.mergeProps(Map.of("filters", Map.of("search", "b", "sort", "asc")),
            List.of(), List.of(), List.of("filters"), List.of());
        var filters = (Map<?, ?>) merged.get("filters");
        assertEquals("b", filters.get("search"));
        assertEquals(1, filters.get("page"));
        assertEquals("asc", filters.get("sort"));
    }

    @Test
    void resetClearsLastProps() {
        processor.propagateProps(Map.of("users", List.of(Map.of("id", 1))));
        processor.reset();
        var merged = processor.mergeProps(Map.of("users", List.of(Map.of("id", 2))),
            List.of("users"), List.of(), List.of(), List.of("id"));
        assertEquals(1, ((List<?>) merged.get("users")).size());
    }

    @Test
    void noLastPropsIsNoOp() {
        processor.reset();
        var props = Map.<String, Object>of("users", List.of(Map.of("id", 1)));
        assertEquals(props, processor.mergeProps(props, List.of("users"), List.of(), List.of(), List.of("id")));
    }
}