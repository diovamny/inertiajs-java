package io.github.diovamny.quarkus.inertia.support;

import java.time.Duration;
import java.util.Locale;

import io.smallrye.config.SmallRyeConfigBuilder;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * Builds {@link InertiaConfig} mappings in plain unit tests, outside a
 * Quarkus runtime. Quarkus' shorthand {@code Duration} converter ({@code 5s},
 * {@code 15m}) is not part of bare SmallRye Config, so it is registered here.
 */
public final class SmallRyeInertiaConfig {

    private SmallRyeInertiaConfig() {
    }

    /**
     * Mapping with every property at its default.
     */
    public static InertiaConfig defaults() {
        return build();
    }

    /**
     * Mapping with the given {@code key, value, key, value...} overrides.
     */
    public static InertiaConfig build(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("expected alternating key/value arguments");
        }
        var builder = new SmallRyeConfigBuilder()
            .withConverter(Duration.class, 200, SmallRyeInertiaConfig::parseDuration);
        for (var i = 0; i < keyValues.length; i += 2) {
            builder.withDefaultValue(keyValues[i], keyValues[i + 1]);
        }
        return builder
            .withMapping(InertiaConfig.class)
            .build()
            .getConfigMapping(InertiaConfig.class);
    }

    private static Duration parseDuration(String value) {
        var v = value.trim().toLowerCase(Locale.ROOT);
        if (v.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(v.substring(0, v.length() - 2)));
        }
        if (v.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(v.substring(0, v.length() - 1)));
        }
        if (v.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(v.substring(0, v.length() - 1)));
        }
        if (v.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(v.substring(0, v.length() - 1)));
        }
        return Duration.parse(value);
    }
}
