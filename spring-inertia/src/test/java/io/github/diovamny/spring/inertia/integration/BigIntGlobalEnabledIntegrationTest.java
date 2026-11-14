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
 * Global {@code inertia.bigint-enabled=true}: pages opt out per response via
 * {@code inertia.preserveBigIntegers(false)} and fall back to the plain wire.
 */
@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false",
    "inertia.bigint-enabled=true"
})
@AutoConfigureMockMvc
class BigIntGlobalEnabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void globalFlagMarksUnsafeLongsByDefault() throws Exception {
        mockMvc.perform(get("/bigint-default")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"id\":{\"$bigint\":\"9223372036854775807\"}")))
            .andExpect(content().string(containsString("\"preserveBigIntegers\":true")))
            .andExpect(content().string(containsString("\"safe\":42")));
    }

    @Test
    void facadeOptOutRestoresPlainWireForTheResponse() throws Exception {
        mockMvc.perform(get("/bigint-optout")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"id\":9223372036854775807")))
            .andExpect(content().string(not(containsString("$bigint"))))
            .andExpect(content().string(not(containsString("preserveBigIntegers"))));
    }
}
