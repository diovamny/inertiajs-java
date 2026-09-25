package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false",
    "inertia.validation.all-errors=true"
})
@AutoConfigureMockMvc
class ValidationAllErrorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void precognitionEmitsArraysWhenAllErrorsEnabled() throws Exception {
        mockMvc.perform(post("/form")
                .header("X-Inertia", "true")
                .header("X-Inertia-Precognition", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.errors.name").isArray());
    }

    @Test
    void flashedErrorsAreArraysWhenAllErrorsEnabled() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/form")
                .session(session)
                .header("X-Inertia", "true")
                .header("Referer", "/flash")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isSeeOther());

        mockMvc.perform(get("/flash")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.errors.name").isArray());
    }
}
