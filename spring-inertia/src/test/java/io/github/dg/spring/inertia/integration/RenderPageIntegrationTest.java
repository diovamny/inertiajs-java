package io.github.dg.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.dg.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class RenderPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersJsonForInertiaVisit() throws Exception {
        mockMvc.perform(get("/dashboard").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string("X-Inertia", "true"))
            .andExpect(header().string("X-Inertia-Component", "Dashboard"))
            .andExpect(header().string("X-Inertia-Version", "test-version"))
            .andExpect(header().string("Vary", "X-Inertia"))
            .andExpect(inertia().component("Dashboard"))
            .andExpect(inertia().prop("title", equalTo("Home")))
            .andExpect(inertia().prop("errors", org.hamcrest.Matchers.anEmptyMap()))
            .andExpect(inertia().url("/dashboard"))
            .andExpect(inertia().version("test-version"));
    }

    @Test
    void rendersHtmlForPlainVisit() throws Exception {
        mockMvc.perform(get("/html"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(header().string("Vary", "X-Inertia"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("data-page=")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(
                "type=\"application/json\" data-page=\"app\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("\"component\":\"Dashboard\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Dashboard")));
    }

    @Test
    void responds409OnVersionMismatch() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "stale"))
            .andExpect(status().isConflict())
            .andExpect(header().string("X-Inertia-Location", "/dashboard"))
            .andExpect(header().string("X-Inertia-Version", "test-version"));
    }

    @Test
    void supportsLazyEtag() throws Exception {
        mockMvc.perform(get("/dashboard").header("X-Inertia", "true"))
            .andExpect(header().exists("ETag"));
    }

    @Test
    void missingRouteIsSilent404ForPlainVisits() throws Exception {
        mockMvc.perform(get("/.well-known/appspecific/com.chrome.devtools.json"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    void missingRouteIsErrorPage404ForInertiaVisits() throws Exception {
        mockMvc.perform(get("/no-such-page").header("X-Inertia", "true"))
            .andExpect(status().isNotFound())
            .andExpect(inertia().component("ErrorPage"))
            .andExpect(inertia().prop("status", equalTo(404)));
    }
}