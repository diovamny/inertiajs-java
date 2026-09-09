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
class OptionalPropsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullVisitOmitsOptionalProps() throws Exception {
        mockMvc.perform(get("/optional")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("section1", nullValue()))
            .andExpect(inertia().prop("section2", nullValue()));
    }

    @Test
    void partialReloadResolvesOnlyRequestedOptional() throws Exception {
        mockMvc.perform(get("/optional")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "OptionalPage")
                .header("X-Inertia-Partial-Data", "section1"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("section1", equalTo("one")))
            .andExpect(inertia().prop("section2", nullValue()));
    }

    @Test
    void partialReloadForOtherKeyOmitsOptional() throws Exception {
        mockMvc.perform(get("/optional")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "OptionalPage")
                .header("X-Inertia-Partial-Data", "section2"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("section1", nullValue()))
            .andExpect(inertia().prop("section2", equalTo("two")));
    }

    @Test
    void partialReloadWithExceptResolvesNonExcludedOptionals() throws Exception {
        mockMvc.perform(get("/optional")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "OptionalPage")
                .header("X-Inertia-Partial-Except", "title"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("section1", equalTo("one")))
            .andExpect(inertia().prop("section2", equalTo("two")));
    }
}
