package io.github.diovamny.inertia.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.junit.jupiter.api.Test;

import io.github.diovamny.inertia.core.model.BigIntSerializer;
import io.github.diovamny.inertia.core.model.PageObject;

/**
 * Normative wire contract for 64-bit integers (Inertia v3.8.0):
 *
 * <ul>
 *   <li>{@code PROTO-BigInt-01} - disabled (default): unsafe longs travel
 *       as plain JSON numbers.</li>
 *   <li>{@code PROTO-BigInt-02} - enabled: unsafe longs travel as
 *       {@code {"$bigint":"..."}} and safe values stay plain.</li>
 *   <li>{@code PROTO-BigInt-03} - per-response opt-in with the global flag
 *       disabled: markers travel and the page carries
 *       {@code preserveBigIntegers: true}.</li>
 *   <li>{@code PROTO-BigInt-04} - per-response opt-out with the global flag
 *       enabled: values stay plain and the page omits
 *       {@code preserveBigIntegers}.</li>
 *   <li>{@code PROTO-BigInt-05} - {@link BigInteger} values beyond 2^53 - 1
 *       travel as {@code {"$bigint":"..."}}; safe values stay plain.</li>
 * </ul>
 */
class BigIntSerializerTest {

    private static final ObjectMapper PLAIN = new ObjectMapper();

    private static ObjectMapper bigintMapper() {
        var module = new SimpleModule("inertia-bigint");
        module.addSerializer(Long.class, new StdSerializer<>(Long.class) {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                if (BigIntSerializer.isUnsafe(value)) {
                    gen.writeStartObject();
                    gen.writeStringField(BigIntSerializer.MARKER, Long.toString(value));
                    gen.writeEndObject();
                } else {
                    gen.writeNumber(value.longValue());
                }
            }
        });
        module.addSerializer(BigInteger.class, new StdSerializer<>(BigInteger.class) {
            @Override
            public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                if (BigIntSerializer.isUnsafe(value)) {
                    gen.writeStartObject();
                    gen.writeStringField(BigIntSerializer.MARKER, value.toString());
                    gen.writeEndObject();
                } else {
                    gen.writeNumber(value.longValue());
                }
            }
        });
        return new ObjectMapper().registerModule(module);
    }

    @Test
    void protoBigInt01DisabledEmitsPlainNumber() throws IOException {
        var page = Map.of("props", Map.of("id", Long.MAX_VALUE));
        assertThat(PLAIN.writeValueAsString(page))
            .isEqualTo("{\"props\":{\"id\":9223372036854775807}}");
        assertThat(BigIntSerializer.encode(7L)).isEqualTo(7L);
    }

    @Test
    void protoBigInt02EnabledEmitsMarker() throws IOException {
        var page = Map.of("props", Map.of("id", Long.MAX_VALUE));
        assertThat(bigintMapper().writeValueAsString(page))
            .isEqualTo("{\"props\":{\"id\":{\"$bigint\":\"9223372036854775807\"}}}");
    }

    @Test
    void protoBigInt02SafeValuesStayPlain() throws IOException {
        assertThat(bigintMapper().writeValueAsString(Map.of("id", 42L)))
            .isEqualTo("{\"id\":42}");
        assertThat(bigintMapper().writeValueAsString(Map.of("id", -42L)))
            .isEqualTo("{\"id\":-42}");
    }

    @Test
    void protoBigInt02NegativeUnsafeEmitsMarker() throws IOException {
        assertThat(bigintMapper().writeValueAsString(Map.of("id", Long.MIN_VALUE)))
            .isEqualTo("{\"id\":{\"$bigint\":\"-9223372036854775808\"}}");
    }

    @Test
    void protoBigInt03PerResponseOptInEmitsMarkerAndPageFlag() throws IOException {
        var page = new PageObject("Orders/Show", Map.of("id", Long.MAX_VALUE),
            "/orders/1", "v1").withPreserveBigIntegers(Boolean.TRUE);

        var json = bigintMapper().writeValueAsString(page);
        assertThat(json)
            .contains("\"id\":{\"$bigint\":\"9223372036854775807\"}")
            .contains("\"preserveBigIntegers\":true");
    }

    @Test
    void protoBigInt04PerResponseOptOutOmitsFlagAndKeepsNumbersPlain() throws IOException {
        var page = new PageObject("Orders/Show", Map.of("id", Long.MAX_VALUE),
            "/orders/1", "v1").withPreserveBigIntegers(null);

        var json = PLAIN.writeValueAsString(page);
        assertThat(json)
            .contains("\"id\":9223372036854775807")
            .doesNotContain("$bigint")
            .doesNotContain("preserveBigIntegers");
    }

    @Test
    void protoBigInt05BigIntegerBeyondSafeRangeEmitsMarker() throws IOException {
        var huge = new BigInteger("123456789012345678901234567890");
        assertThat(bigintMapper().writeValueAsString(Map.of("id", huge)))
            .isEqualTo("{\"id\":{\"$bigint\":\"123456789012345678901234567890\"}}");

        assertThat(bigintMapper().writeValueAsString(
            Map.of("id", BigIntSerializer.MAX_SAFE_BIG_INT.add(BigInteger.ONE))))
            .isEqualTo("{\"id\":{\"$bigint\":\"9007199254740992\"}}");

        assertThat(bigintMapper().writeValueAsString(Map.of("id", BigInteger.valueOf(42))))
            .isEqualTo("{\"id\":42}");
        assertThat(bigintMapper().writeValueAsString(
            Map.of("id", BigIntSerializer.MIN_SAFE_BIG_INT)))
            .isEqualTo("{\"id\":-9007199254740991}");
    }
}
