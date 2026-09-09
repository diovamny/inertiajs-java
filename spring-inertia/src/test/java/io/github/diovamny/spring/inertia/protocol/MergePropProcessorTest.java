package io.github.diovamny.spring.inertia.protocol;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MergePropProcessorTest {

    private final MergePropProcessor processor = new MergePropProcessor();

    @Test
    void mergePropsIsNoOp() {
        Map<String, Object> props = Map.of("users", (List<?>) List.of(Map.of("id", 1)));
        var result = processor.mergeProps(props, List.of("users"), List.of(), List.of(), List.of("id"));
        assertSame(props, result, "mergeProps should return the same map (no-op)");
    }

    @Test
    void propagatePropsIsNoOp() {
        processor.propagateProps(Map.of("users", (List<?>) List.of(Map.of("id", 1))));
        // No exception, just verifies no-op
    }

    @Test
    void resetIsNoOp() {
        processor.reset();
        // No exception, just verifies no-op
    }

    @Test
    void mergePropsReturnsUnmodifiedMap() {
        Map<String, Object> props = Map.of("filters", Map.of("search", "a"));
        var result = processor.mergeProps(props, List.of(), List.of(), List.of("filters"), List.of());
        assertEquals(props, result);
    }
}
