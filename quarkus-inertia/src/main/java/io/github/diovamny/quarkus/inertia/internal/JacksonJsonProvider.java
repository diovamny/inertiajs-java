package io.github.diovamny.quarkus.inertia.internal;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.spi.JsonProvider;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals;

/**
 * Jackson-backed {@link JsonProvider}, selected with priority 1 when the
 * application also registers {@link JsonbJsonProvider}; uses the request's
 * {@link ObjectMapper} and unwraps {@link RawJson} values beforehand.
 *
 * <p>Unsafe 64-bit integers (and {@link java.math.BigInteger} values beyond
 * JavaScript precision) travel as {@code {"$bigint":"..."}} markers when BigInt
 * preservation is active. Precedence: request-scoped override
 * ({@code preserveBigIntegers()}) &gt; page flag
 * ({@code PageObject.preserveBigIntegers}) &gt; global
 * {@code inertia.bigint-enabled}. Serialized pages carry
 * {@code preserveBigIntegers: true} only when the effective value is true.</p>
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class JacksonJsonProvider implements JsonProvider {

    /** Top-level page flag telling the client to revive {@code $bigint} markers. */
    static final String PRESERVE_BIG_INTEGERS = "preserveBigIntegers";

    private final ObjectMapper mapper;
    private final ObjectMapper bigintMapper;
    private final boolean bigintEnabled;

    @Inject
    public JacksonJsonProvider(ObjectMapper mapper, InertiaConfig config) {
        this.bigintEnabled = config.bigintEnabled();
        this.mapper = mapper;
        this.bigintMapper = mapper.copy().registerModule(new JacksonBigIntModule());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toJson(Object value) {
        try {
            var target = RawJsonUnwrapper.containsRawJson(value)
                ? RawJsonUnwrapper.unwrap(value)
                : value;
            boolean effective = effectiveBigInt(target);
            var selected = effective ? bigintMapper : mapper;
            if (target instanceof PageObject page) {
                Map<String, Object> map = selected.convertValue(page, Map.class);
                if (effective) {
                    map.put(PRESERVE_BIG_INTEGERS, Boolean.TRUE);
                } else {
                    map.remove(PRESERVE_BIG_INTEGERS);
                }
                return selected.writeValueAsString(map);
            }
            return selected.writeValueAsString(target);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    private boolean effectiveBigInt(Object target) {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var override = InertiaContextLocals.get(ctx, "inertia-preserve-bigint");
            if (override instanceof Boolean flag) {
                return flag;
            }
        }
        if (target instanceof PageObject page && page.preserveBigIntegers() != null) {
            return page.preserveBigIntegers();
        }
        return bigintEnabled;
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
}
