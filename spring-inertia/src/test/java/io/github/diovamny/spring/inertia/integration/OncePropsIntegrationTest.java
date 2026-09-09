package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.diovamny.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class OncePropsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void oncePropIsDeliveredThenOmitted() throws Exception {
        // First request: once prop is delivered
        mockMvc.perform(get("/once")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("notice", equalTo("Hello")));

        // Second request: client sends X-Inertia-Except-Once-Props with the tracking key
        mockMvc.perform(get("/once")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Except-Once-Props", "notice"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("notice", nullValue()));
    }

    @Test
    void oncePropIsDeliveredInDifferentSessions() throws Exception {
        // Without X-Inertia-Except-Once-Props, once prop is delivered every time
        mockMvc.perform(get("/once")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(inertia().prop("notice", equalTo("Hello")));

        mockMvc.perform(get("/once")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(inertia().prop("notice", equalTo("Hello")));
    }
}
