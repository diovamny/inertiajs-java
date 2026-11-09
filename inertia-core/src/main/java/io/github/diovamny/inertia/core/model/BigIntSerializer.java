package io.github.diovamny.inertia.core.model;

import java.math.BigInteger;
import java.util.Map;

/**
 * Wire-format helper for the {@code $bigint} protocol marker introduced in
 * Inertia 3.8.0.
 *
 * <p>JavaScript numbers cannot represent integers beyond
 * {@code 2^53 - 1} (9007199254740991) without precision loss. When the
 * adapter-level {@code bigint-enabled} flag is active, unsafe {@code long}
 * values are serialized as {@code {"$bigint":"<decimal>"}} and the page
 * carries {@code preserveBigIntegers: true} so the client revives them as
 * {@code BigInt}. When disabled (the default), values keep the plain
 * numeric wire.</p>
 */
public final class BigIntSerializer {

    /** JSON property carrying the decimal string of an unsafe integer. */
    public static final String MARKER = "$bigint";

    /** Largest integer exactly representable by an IEEE-754 double (2^53 - 1). */
    public static final long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    /** Largest safe integer as an arbitrary-precision value (2^53 - 1). */
    public static final BigInteger MAX_SAFE_BIG_INT =
        BigInteger.valueOf(MAX_SAFE_INTEGER);

    /** Smallest safe integer as an arbitrary-precision value (-(2^53 - 1)). */
    public static final BigInteger MIN_SAFE_BIG_INT = MAX_SAFE_BIG_INT.negate();

    private BigIntSerializer() {
    }

    /**
     * Whether a value cannot be represented exactly by JavaScript numbers.
     *
     * @param value the integer to check
     * @return {@code true} when {@code |value| > 2^53 - 1}
     */
    public static boolean isUnsafe(long value) {
        return value > MAX_SAFE_INTEGER || value < -MAX_SAFE_INTEGER;
    }

    /**
     * Encodes a value for the wire: unsafe integers become the
     * {@code {"$bigint":"..."}} marker map, safe integers pass through
     * unchanged.
     *
     * @param value the integer to encode
     * @return the marker map for unsafe values, the plain {@link Long}
     *         otherwise
     */
    public static Object encode(long value) {
        return isUnsafe(value) ? marker(value) : value;
    }

    /**
     * Builds the {@code {"$bigint":"..."}} marker map for an integer.
     *
     * @param value the integer to wrap
     * @return the marker map expected by the Inertia v3 client
     */
    public static Map<String, Object> marker(long value) {
        return Map.of(MARKER, Long.toString(value));
    }

    /**
     * Whether an arbitrary-precision integer cannot be represented exactly by
     * JavaScript numbers.
     *
     * @param value the integer to check, may be {@code null}
     * @return {@code true} when {@code |value| > 2^53 - 1}
     */
    public static boolean isUnsafe(BigInteger value) {
        if (value == null) {
            return false;
        }
        return value.compareTo(MAX_SAFE_BIG_INT) > 0
            || value.compareTo(MIN_SAFE_BIG_INT) < 0;
    }

    /**
     * Encodes an arbitrary-precision integer for the wire: unsafe values
     * become the {@code {"$bigint":"..."}} marker map, safe values pass
     * through unchanged.
     *
     * @param value the integer to encode, may be {@code null}
     * @return the marker map for unsafe values, the plain {@link BigInteger}
     *         otherwise, {@code null} for a {@code null} input
     */
    public static Object encode(BigInteger value) {
        if (value == null) {
            return null;
        }
        return isUnsafe(value) ? marker(value) : value;
    }

    /**
     * Builds the {@code {"$bigint":"..."}} marker map for an
     * arbitrary-precision integer.
     *
     * @param value the integer to wrap
     * @return the marker map expected by the Inertia v3 client
     */
    public static Map<String, Object> marker(BigInteger value) {
        return Map.of(MARKER, value.toString());
    }
}
