package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.diovamny.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class SharedPropsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sharedAndAlwaysPropsAreInjected() throws Exception {
        mockMvc.perform(get("/shared")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("SharedPage"))
            .andExpect(inertia().prop("appName", equalTo("MyApp")))
            .andExpect(inertia().prop("csrf", equalTo("token123")))
            .andExpect(inertia().prop("extra", equalTo("e")));
    }

    @Test
    void sharedPropsSurvivePartialReload() throws Exception {
        mockMvc.perform(get("/shared")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "SharedPage")
                .header("X-Inertia-Partial-Data", "appName"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("appName", equalTo("MyApp")))
            .andExpect(inertia().prop("extra", org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void unrequestedSharedPropsDoNotSurvivePartialReload() throws Exception {
        mockMvc.perform(get("/shared")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "SharedPage")
                .header("X-Inertia-Partial-Data", "extra"))
            .andExpect(status().isOk())
            .andExpect(inertia().prop("extra", equalTo("e")))
            .andExpect(inertia().prop("appName", org.hamcrest.Matchers.nullValue()))
            .andExpect(inertia().prop("csrf", equalTo("token123")));
    }
}
