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
class DeferredPropsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullVisitExcludesDeferredValuesButSendsMetadata() throws Exception {
        mockMvc.perform(get("/deferred")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("DeferredPage"))
            .andExpect(inertia().hasProp("title"))
            .andExpect(inertia().prop("data", nullValue()));
    }

    @Test
    void partialReloadResolvesDeferredGroup() throws Exception {
        mockMvc.perform(get("/deferred")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "DeferredPage")
                .header("X-Inertia-Partial-Data", "data"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("data", equalTo("lazy-value")))
            .andExpect(inertia().prop("title", nullValue()));
    }
}
