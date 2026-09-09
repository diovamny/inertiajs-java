package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.diovamny.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=true",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class CsrfIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void visitSetsXsrfCookie() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("XSRF-TOKEN"))
            .andExpect(inertia().component("Dashboard"));
    }

    @Test
    void stateChangingRequestWithoutTokenIs419() throws Exception {
        mockMvc.perform(post("/submit").header("X-Inertia", "true"))
            .andExpect(status().is(419));
    }

    @Test
    void stateChangingRequestWithWrongTokenIs419() throws Exception {
        mockMvc.perform(post("/submit")
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", "wrong"))
            .andExpect(status().is(419));
    }

    @Test
    void stateChangingRequestWithTokenSucceeds() throws Exception {
        var session = new MockHttpSession();
        var token = fetchToken(session);
        mockMvc.perform(post("/submit")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", token))
            .andExpect(status().isSeeOther());
    }

    private String fetchToken(MockHttpSession session) throws Exception {
        var result = mockMvc.perform(get("/dashboard")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andReturn();
        return result.getResponse().getCookie("XSRF-TOKEN").getValue();
    }
}
