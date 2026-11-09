package io.github.diovamny.quarkus.inertia.internal;

import java.io.IOException;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.github.diovamny.inertia.core.model.BigIntSerializer;

/**
 * Jackson module serializing unsafe {@code long} and {@link BigInteger}
 * values as the Inertia v3 {@code {"$bigint":"..."}} marker. Registered on
 * the adapter mapper only when {@code inertia.bigint-enabled=true} or when a
 * response opts in with {@code preserveBigIntegers()}, so the default wire
 * stays unchanged.
 */
public class JacksonBigIntModule extends SimpleModule {

    public JacksonBigIntModule() {
        super("inertia-bigint");
        addSerializer(Long.class, new LongBigIntSerializer());
        addSerializer(BigInteger.class, new BigIntegerBigIntSerializer());
    }

    private static final class LongBigIntSerializer extends StdSerializer<Long> {

        LongBigIntSerializer() {
            super(Long.class);
        }

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
    }

    private static final class BigIntegerBigIntSerializer extends StdSerializer<BigInteger> {

        BigIntegerBigIntSerializer() {
            super(BigInteger.class);
        }

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
    }
}
