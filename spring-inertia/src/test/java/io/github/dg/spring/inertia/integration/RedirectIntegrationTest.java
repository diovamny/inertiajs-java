package io.github.dg.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class RedirectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullPageRedirectOnInertiaVisitIs409() throws Exception {
        mockMvc.perform(get("/goto").header("X-Inertia", "true"))
            .andExpect(status().isConflict())
            .andExpect(header().string("X-Inertia-Location", "/flash"))
            .andExpect(header().string("Vary", "X-Inertia"));
    }

    @Test
    void fullPageRedirectOnPlainVisitIs302() throws Exception {
        mockMvc.perform(get("/goto"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/flash"));
    }

    @Test
    void stateChangingRedirectIs303() throws Exception {
        mockMvc.perform(post("/submit").header("X-Inertia", "true"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/flash"));
    }

    @Test
    void preserveFragmentRedirectStaysSameOriginAndRendersFlag() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(get("/preserve-redirect")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/flash"));

        mockMvc.perform(get("/flash")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.preserveFragment").value(true));
    }
}
