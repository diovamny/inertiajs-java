package io.github.dg.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.dg.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class PartialReloadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void partialDataFiltersProps() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "Dashboard")
                .header("X-Inertia-Partial-Data", "users"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Inertia-Partial-Component", "Dashboard"))
            .andExpect(inertia().hasProp("users"))
            .andExpect(inertia().prop("title", nullValue()));
    }

    @Test
    void partialExceptExcludesProps() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "Dashboard")
                .header("X-Inertia-Partial-Except", "title"))
            .andExpect(status().isOk())
            .andExpect(inertia().hasProp("users"))
            .andExpect(inertia().prop("title", nullValue()));
    }

    @Test
    void fullVisitSendsAllProps() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().hasProp("title"))
            .andExpect(inertia().hasProp("users"))
            .andExpect(inertia().prop("users", hasSize(2)));
    }

    @Test
    void otherComponentPartialIsIgnored() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "Other")
                .header("X-Inertia-Partial-Data", "users"))
            .andExpect(status().isOk())
            .andExpect(inertia().hasProp("title"))
            .andExpect(inertia().hasProp("users"));
    }

    @Test
    void resetKeyPrunesMergeMetadata() throws Exception {
        mockMvc.perform(get("/merge-match")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "MergePage")
                .header("X-Inertia-Partial-Data", "contacts"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .jsonPath("$.mergeProps[0]").value("contacts"));

        mockMvc.perform(get("/merge-match")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "MergePage")
                .header("X-Inertia-Partial-Data", "contacts")
                .header("X-Inertia-Reset", "contacts"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                .jsonPath("$.mergeProps").doesNotExist());
    }
}
