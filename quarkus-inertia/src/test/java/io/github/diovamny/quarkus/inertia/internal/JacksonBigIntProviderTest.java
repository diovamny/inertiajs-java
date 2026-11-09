package io.github.diovamny.quarkus.inertia.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.support.SmallRyeInertiaConfig;

class JacksonBigIntProviderTest {

    private static InertiaConfig config(boolean bigintEnabled) {
        return SmallRyeInertiaConfig.build(
            "inertia.bigint-enabled", Boolean.toString(bigintEnabled));
    }

    private static JacksonJsonProvider provider(boolean bigintEnabled) {
        return new JacksonJsonProvider(new ObjectMapper(), config(bigintEnabled));
    }

    @Test
    void disabledByDefaultLeavesLongsPlain() {
        var page = new PageObject("Contacts/Index", Map.of("id", Long.MAX_VALUE),
            "/contacts", "v1");
        assertThat(provider(false).toJson(page))
            .contains("\"id\":9223372036854775807")
            .doesNotContain("$bigint")
            .doesNotContain("preserveBigIntegers");
    }

    @Test
    void enabledMarksUnsafeLongsAndSetsPageFlag() {
        var page = new PageObject("Contacts/Index", Map.of("id", Long.MAX_VALUE),
            "/contacts", "v1");
        assertThat(provider(true).toJson(page))
            .contains("\"id\":{\"$bigint\":\"9223372036854775807\"}")
            .contains("\"preserveBigIntegers\":true");
    }

    @Test
    void enabledKeepsSafeValuesPlain() {
        assertThat(provider(true).toJson(Map.of("n", 42L))).isEqualTo("{\"n\":42}");
    }

    @Test
    void enabledDoesNotMutateSharedMapper() throws Exception {
        var mapper = new ObjectMapper();
        provider(true);
        assertThat(mapper.writeValueAsString(Map.of("n", Long.MAX_VALUE)))
            .isEqualTo("{\"n\":9223372036854775807}");
    }

    @Test
    void pageFlagOptsInWhenGlobalDisabled() {
        var page = new PageObject("Contacts/Index", Map.of("id", Long.MAX_VALUE),
            "/contacts", "v1").withPreserveBigIntegers(Boolean.TRUE);
        assertThat(provider(false).toJson(page))
            .contains("\"id\":{\"$bigint\":\"9223372036854775807\"}")
            .contains("\"preserveBigIntegers\":true");
    }

    @Test
    void pageFlagFalseOverridesGlobalEnabled() {
        var page = new PageObject("Contacts/Index", Map.of("id", Long.MAX_VALUE),
            "/contacts", "v1").withPreserveBigIntegers(Boolean.FALSE);
        assertThat(provider(true).toJson(page))
            .contains("\"id\":9223372036854775807")
            .doesNotContain("$bigint")
            .doesNotContain("preserveBigIntegers");
    }

    @Test
    void unsafeBigIntegersTravelAsMarkersWhenOptedIn() {
        var huge = new BigInteger("123456789012345678901234567890");
        var page = new PageObject("Orders", Map.of("id", huge), "/", "v1")
            .withPreserveBigIntegers(Boolean.TRUE);
        assertThat(provider(false).toJson(page))
            .contains("\"id\":{\"$bigint\":\"123456789012345678901234567890\"}");

        var safe = new PageObject("Orders", Map.of("id", BigInteger.valueOf(7)), "/", "v1")
            .withPreserveBigIntegers(Boolean.TRUE);
        assertThat(provider(false).toJson(safe))
            .contains("\"id\":7")
            .doesNotContain("$bigint");
    }
}
