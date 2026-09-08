package io.github.dg.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.dg.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void precognitionAnswers422WithFieldErrors() throws Exception {
        mockMvc.perform(post("/form")
                .header("X-Inertia", "true")
                .header("X-Inertia-Precognition", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void failedFormFlowsErrorsIntoNextPage() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/form")
                .session(session)
                .header("X-Inertia", "true")
                .header("Referer", "/flash")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/flash"));

        mockMvc.perform(get("/flash")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("errors", hasKey("name")))
            .andExpect(inertia().flash("errors", org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void validFormRedirects() throws Exception {
        mockMvc.perform(post("/form")
                .header("X-Inertia", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ada\"}"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/flash"));
    }

    @Test
    void precognitionWithoutXInertiaReturns422WithPrecognitionHeader() throws Exception {
        mockMvc.perform(post("/form")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "name")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(header().string("Precognition", "true"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void precognitionOnlyReportsRequestedField() throws Exception {
        mockMvc.perform(post("/form")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "name")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(header().string("Precognition", "true"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").doesNotExist());
    }

    @Test
    void precognitionValidReturns204WithSuccessHeader() throws Exception {
        mockMvc.perform(post("/form")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "name")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ada\"}"))
            .andExpect(status().isNoContent())
            .andExpect(header().string("Precognition", "true"))
            .andExpect(header().string("Precognition-Success", "true"));
    }

    @Test
    void errorBagErrorsAreNestedUnderBagOnReload() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/form")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Error-Bag", "createContact")
                .header("Referer", "/flash")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/flash"));

        mockMvc.perform(get("/flash")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Error-Bag", "createContact"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.errors.createContact.name").exists());
    }
}
