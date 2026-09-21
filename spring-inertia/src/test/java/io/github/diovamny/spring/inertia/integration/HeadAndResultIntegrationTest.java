package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.diovamny.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false",
    "inertia.server-head=true",
    "inertia.meta-title-template=%s | App"
})
@AutoConfigureMockMvc
class HeadAndResultIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void serverHeadTagsArePublishedAsHeadProp() throws Exception {
        mockMvc.perform(get("/headed")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("HeadPage"))
            .andExpect(jsonPath("$.props.head", hasSize(3)))
            .andExpect(jsonPath("$.props.head[0].key", equalTo("title")))
            .andExpect(jsonPath("$.props.head[0].attributes.text", equalTo("Hi | App")))
            .andExpect(jsonPath("$.props.head[1].key", equalTo("meta-description")))
            .andExpect(jsonPath("$.props.head[2].key", equalTo("link-canonical")));
    }

    @Test
    void typedPageResultIsServed() throws Exception {
        mockMvc.perform(get("/typed-page")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("TypedPage"))
            .andExpect(inertia().prop("a", equalTo(1)));
    }

    @Test
    void typedRedirectResultIsServed() throws Exception {
        mockMvc.perform(get("/typed-redirect")
                .header("X-Inertia", "true"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/flash"));
    }

    @Test
    void typedLocationResultIsConflict() throws Exception {
        mockMvc.perform(get("/typed-location")
                .header("X-Inertia", "true"))
            .andExpect(status().isConflict())
            .andExpect(header().string("X-Inertia-Location", "https://example.com"));
    }
}
