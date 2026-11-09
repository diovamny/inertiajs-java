package io.github.diovamny.spring.inertia.internal;

import java.math.BigInteger;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

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
        addSerializer(Long.class, new BigIntValueSerializer());
        addSerializer(BigInteger.class, new BigIntegerValueSerializer());
    }

    private static final class BigIntValueSerializer extends ValueSerializer<Long> {

        @Override
        public void serialize(Long value, JsonGenerator gen, SerializationContext ctxt) {
            if (BigIntSerializer.isUnsafe(value)) {
                gen.writeStartObject();
                gen.writeStringProperty(BigIntSerializer.MARKER, Long.toString(value));
                gen.writeEndObject();
            } else {
                gen.writeNumber(value.longValue());
            }
        }
    }

    private static final class BigIntegerValueSerializer extends ValueSerializer<BigInteger> {

        @Override
        public void serialize(BigInteger value, JsonGenerator gen, SerializationContext ctxt) {
            if (BigIntSerializer.isUnsafe(value)) {
                gen.writeStartObject();
                gen.writeStringProperty(BigIntSerializer.MARKER, value.toString());
                gen.writeEndObject();
            } else {
                gen.writeNumber(value.longValue());
            }
        }
    }
}
