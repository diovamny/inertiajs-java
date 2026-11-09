package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Per-response BigInt opt-in with the global {@code inertia.bigint-enabled}
 * flag disabled: unsafe longs must travel as {@code {"$bigint":"..."}}
 * markers and the page must carry {@code preserveBigIntegers: true}.
 */
@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class BigIntPerResponseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fluentOptInMarksUnsafeLongsAndStampsPageFlag() throws Exception {
        mockMvc.perform(get("/bigint-fluent")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"id\":{\"$bigint\":\"9223372036854775807\"}")))
            .andExpect(content().string(containsString("\"preserveBigIntegers\":true")))
            .andExpect(content().string(containsString("\"safe\":42")));
    }

    @Test
    void facadeOptInMarksUnsafeLongsAndStampsPageFlag() throws Exception {
        mockMvc.perform(get("/bigint-facade")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"id\":{\"$bigint\":\"9223372036854775807\"}")))
            .andExpect(content().string(containsString("\"preserveBigIntegers\":true")));
    }

    @Test
    void defaultStaysPlainWhenGlobalDisabled() throws Exception {
        mockMvc.perform(get("/bigint-default")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"id\":9223372036854775807")))
            .andExpect(content().string(not(containsString("$bigint"))))
            .andExpect(content().string(not(containsString("preserveBigIntegers"))));
    }
}
