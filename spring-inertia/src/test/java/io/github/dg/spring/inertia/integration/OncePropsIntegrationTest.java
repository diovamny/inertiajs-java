package io.github.dg.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.dg.spring.inertia.testing.InertiaResultMatchers.inertia;

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
        var session = new MockHttpSession();

        mockMvc.perform(get("/once")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("notice", equalTo("Hello")));

        mockMvc.perform(get("/once")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("notice", nullValue()));
    }

    @Test
    void oncePropIsDeliveredInDifferentSessions() throws Exception {
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