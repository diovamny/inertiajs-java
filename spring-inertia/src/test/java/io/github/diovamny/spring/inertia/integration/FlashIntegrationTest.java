package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.diovamny.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class FlashIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void flashSurvivesRedirectIntoNextPage() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/submit")
                .session(session)
                .header("X-Inertia", "true"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/flash"));

        mockMvc.perform(get("/flash")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("message", equalTo("Saved")))
            .andExpect(inertia().flash("message", equalTo("Saved")));
    }

    @Test
    void flashIsOneTime() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/submit").session(session).header("X-Inertia", "true"))
            .andExpect(status().isSeeOther());
        mockMvc.perform(get("/flash").session(session)
                .header("X-Inertia", "true").header("X-Inertia-Version", "test-version"))
            .andExpect(inertia().prop("message", equalTo("Saved")))
            .andExpect(inertia().flash("message", equalTo("Saved")));
        mockMvc.perform(get("/flash").session(session)
                .header("X-Inertia", "true").header("X-Inertia-Version", "test-version"))
            .andExpect(inertia().prop("message", org.hamcrest.Matchers.nullValue()))
            .andExpect(inertia().flash("message", org.hamcrest.Matchers.nullValue()));
    }
}
