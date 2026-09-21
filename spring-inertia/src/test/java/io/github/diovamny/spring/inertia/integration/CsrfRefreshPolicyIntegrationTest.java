package io.github.diovamny.spring.inertia.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=true",
    "inertia.csrf-refresh-policy=lazy",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class CsrfRefreshPolicyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void firstVisitStillIssuesTokenCookie() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void secondIdempotentVisitWithValidCookieSkipsReemission() throws Exception {
        var first = mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("XSRF-TOKEN"))
            .andReturn();
        var token = first.getResponse().getCookie("XSRF-TOKEN").getValue();
        var session = (MockHttpSession) first.getRequest().getSession(false);

        var second = mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .session(session)
                .cookie(new Cookie("XSRF-TOKEN", token)))
            .andExpect(status().isOk())
            .andReturn();
        var reissued = second.getResponse().getCookie("XSRF-TOKEN");
        org.assertj.core.api.Assertions.assertThat(reissued).isNull();
    }

    @Test
    void stateChangingRequestWithValidTokenStillSucceeds() throws Exception {
        var first = mockMvc.perform(get("/dashboard")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andReturn();
        var token = first.getResponse().getCookie("XSRF-TOKEN").getValue();
        var session = (MockHttpSession) first.getRequest().getSession(false);

        mockMvc.perform(post("/submit")
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", token)
                .session(session)
                .cookie(new Cookie("XSRF-TOKEN", token)))
            .andExpect(status().isSeeOther());
    }
}
