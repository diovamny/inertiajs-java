package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class BigIntSerializerTest {

    @Test
    void unsafeValuesAreBeyondJavaScriptPrecision() {
        assertThat(BigIntSerializer.isUnsafe(Long.MAX_VALUE)).isTrue();
        assertThat(BigIntSerializer.isUnsafe(Long.MIN_VALUE)).isTrue();
        assertThat(BigIntSerializer.isUnsafe(BigIntSerializer.MAX_SAFE_INTEGER + 1)).isTrue();
        assertThat(BigIntSerializer.isUnsafe(-BigIntSerializer.MAX_SAFE_INTEGER - 1)).isTrue();
    }

    @Test
    void safeValuesPassThrough() {
        assertThat(BigIntSerializer.isUnsafe(0L)).isFalse();
        assertThat(BigIntSerializer.isUnsafe(BigIntSerializer.MAX_SAFE_INTEGER)).isFalse();
        assertThat(BigIntSerializer.isUnsafe(-BigIntSerializer.MAX_SAFE_INTEGER)).isFalse();
        assertThat(BigIntSerializer.encode(42L)).isEqualTo(42L);
    }

    @Test
    void unsafeValuesEncodeAsMarkerMap() {
        assertThat(BigIntSerializer.encode(Long.MAX_VALUE))
            .isEqualTo(BigIntSerializer.marker(Long.MAX_VALUE));
        assertThat(BigIntSerializer.marker(9_007_199_254_740_993L))
            .containsEntry("$bigint", "9007199254740993");
    }

    @Test
    void unsafeBigIntegersAreBeyondJavaScriptPrecision() {
        assertThat(BigIntSerializer.isUnsafe((BigInteger) null)).isFalse();
        assertThat(BigIntSerializer.isUnsafe(BigIntSerializer.MAX_SAFE_BIG_INT)).isFalse();
        assertThat(BigIntSerializer.isUnsafe(BigIntSerializer.MIN_SAFE_BIG_INT)).isFalse();
        assertThat(BigIntSerializer.isUnsafe(BigIntSerializer.MAX_SAFE_BIG_INT.add(BigInteger.ONE)))
            .isTrue();
        assertThat(BigIntSerializer.isUnsafe(BigIntSerializer.MIN_SAFE_BIG_INT.subtract(BigInteger.ONE)))
            .isTrue();
        assertThat(BigIntSerializer.isUnsafe(new BigInteger("100000000000000000000000000000000")))
            .isTrue();
    }

    @Test
    void bigIntegersEncodeAsMarkerMapWhenUnsafe() {
        var huge = new BigInteger("123456789012345678901234567890");
        assertThat(BigIntSerializer.encode(huge))
            .isEqualTo(BigIntSerializer.marker(huge));
        assertThat(BigIntSerializer.marker(huge))
            .containsEntry("$bigint", "123456789012345678901234567890");
        assertThat(BigIntSerializer.encode(BigInteger.valueOf(42)))
            .isEqualTo(BigInteger.valueOf(42));
        assertThat(BigIntSerializer.encode((BigInteger) null)).isNull();
    }
}
