package io.github.diovamny.inertia.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ErrorWireTest {

    @Test
    void blankBagKeepsWireUnchanged() {
        var wire = Map.<String, Object>of("name", "required");
        assertThat(ErrorWire.wrapBag(wire, null)).isSameAs(wire);
        assertThat(ErrorWire.wrapBag(wire, "")).isSameAs(wire);
        assertThat(ErrorWire.wrapBag(wire, "  ")).isSameAs(wire);
        assertThat(ErrorWire.wrapBag(null, "bag")).isNull();
    }

    @Test
    void namedBagNamespacesWire() {
        var wire = Map.<String, Object>of("name", "required");
        assertThat(ErrorWire.wrapBag(wire, "contact"))
            .isEqualTo(Map.of("contact", Map.of("name", "required")));
    }
}
