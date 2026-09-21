package io.github.diovamny.spring.inertia.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract matrix for the Spring Security integration: anonymous, roles,
 * valid/missing/tampered tokens, 409 challenges, 303 CSRF recovery, 403
 * pages, malicious referers and single CSRF ownership.
 */
@SpringBootTest(properties = {
    "inertia.security.mode=framework",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class InertiaSpringSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    void resetCounter() {
        SecureTestController.SUBMIT_INVOCATIONS.set(0);
    }

    @Test
    void adapterCsrfFilterIsNotRegisteredInFrameworkMode() {
        assertThat(context.containsBean("inertiaCsrfFilter")).isFalse();
        assertThat(context.getBean(InertiaAuthenticationEntryPoint.class)).isNotNull();
        assertThat(context.getBean(InertiaAccessDeniedHandler.class)).isNotNull();
    }

    @Test
    void anonymousHtmlVisitRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/secure"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/login"));
    }

    @Test
    void anonymousInertiaVisitIs409WithoutPageHeaders() throws Exception {
        mockMvc.perform(get("/secure").header("X-Inertia", "true"))
            .andExpect(status().isConflict())
            .andExpect(header().string("X-Inertia-Location", "/login"))
            .andExpect(header().doesNotExist("X-Inertia"));
    }

    @Test
    void publicPageIssuesXsrfCookie() throws Exception {
        mockMvc.perform(get("/public").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("XSRF-TOKEN"))
            .andExpect(header().string("Vary", org.hamcrest.Matchers.containsString("X-Inertia")));
    }

    @Test
    void userRoleSeesForbiddenInertiaPageOnAdmin() throws Exception {
        var session = loginAs("user@example.com", "password");
        mockMvc.perform(get("/admin").session(session).header("X-Inertia", "true"))
            .andExpect(status().isForbidden())
            .andExpect(header().string("X-Inertia", "true"))
            .andExpect(jsonPath("$.component").value("Errors/Forbidden"))
            .andExpect(jsonPath("$.props.status").value(403));
    }

    @Test
    void adminRoleAccessesAdmin() throws Exception {
        var session = loginAs("admin@example.com", "adminpass");
        mockMvc.perform(get("/admin").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.component").value("Admin"));
    }

    @Test
    void postWithValidTokenReachesControllerExactlyOnce() throws Exception {
        var session = loginAs("user@example.com", "password");
        var state = xsrf(session);
        mockMvc.perform(post("/submit").session(session)
                .cookie(state.cookies())
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", state.token()))
            .andExpect(status().isSeeOther());
        assertThat(SecureTestController.SUBMIT_INVOCATIONS.get()).isEqualTo(1);
    }

    @Test
    void postWithoutTokenIs303WithFlashAndSkipsController() throws Exception {
        var session = loginAs("user@example.com", "password");
        var state = xsrf(session);
        mockMvc.perform(post("/submit").session(session)
                .cookie(state.cookies())
                .header("X-Inertia", "true")
                .header("Referer", "http://localhost/secure"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "http://localhost/secure"));
        assertThat(SecureTestController.SUBMIT_INVOCATIONS.get()).isZero();

        mockMvc.perform(get("/secure").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.error").value("La página expiró. Vuelve a intentarlo."));
    }

    @Test
    void postWithTamperedTokenIs303() throws Exception {
        var session = loginAs("user@example.com", "password");
        var state = xsrf(session);
        mockMvc.perform(post("/submit").session(session)
                .cookie(state.cookies())
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", "tampered")
                .header("Referer", "http://localhost/secure"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "http://localhost/secure"));
        assertThat(SecureTestController.SUBMIT_INVOCATIONS.get()).isZero();
    }

    @Test
    void maliciousRefererFallsBackToSafePath() throws Exception {
        var session = loginAs("user@example.com", "password");
        var state = xsrf(session);
        mockMvc.perform(post("/submit").session(session)
                .cookie(state.cookies())
                .header("X-Inertia", "true")
                .header("Referer", "https://evil.example/phish"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/"));
    }

    @Test
    void nonInertiaCsrfFailureUsesDefault403() throws Exception {
        var session = loginAs("user@example.com", "password");
        var state = xsrf(session);
        mockMvc.perform(post("/submit").session(session)
                .cookie(state.cookies()))
            .andExpect(status().isForbidden());
    }

    @Test
    void sessionRenewalIssuesFreshToken() throws Exception {
        var session = loginAs("user@example.com", "password");
        var first = xsrf(session);
        mockMvc.perform(post("/logout").session(session)
                .cookie(first.cookies())
                .header("X-XSRF-TOKEN", first.token()))
            .andExpect(status().isFound());
        var renewed = new MockHttpSession();
        var loginState = xsrf(renewed);
        mockMvc.perform(post("/login-json").session(renewed)
                .cookie(loginState.cookies())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"password\":\"password\"}")
                .header("X-XSRF-TOKEN", loginState.token()))
            .andExpect(status().isSeeOther());
        var current = (MockHttpSession) SecureTestController.LAST_SESSION;
        var fresh = xsrf(current);
        mockMvc.perform(post("/submit").session(current)
                .cookie(fresh.cookies())
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", fresh.token()))
            .andExpect(status().isSeeOther());
        assertThat(SecureTestController.SUBMIT_INVOCATIONS.get()).isEqualTo(1);
    }

    @Test
    void loginWithWrongPasswordShowsErrors() throws Exception {
        var session = new MockHttpSession();
        var state = xsrf(session);
        mockMvc.perform(post("/login-json").session(session)
                .cookie(state.cookies())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"password\":\"wrong\"}")
                .header("X-XSRF-TOKEN", state.token())
                .header("X-Inertia", "true"))
            .andExpect(status().isSeeOther());
        mockMvc.perform(get("/public").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.errors.email").exists());
    }

    /** XSRF state: framework CSRF is stateless, the token lives in the cookie. */
    private record XsrfState(String token, jakarta.servlet.http.Cookie[] cookies) {
    }

    @Test
    void loginRotatesTheSessionId() throws Exception {
        var session = new MockHttpSession();
        var before = session.getId();
        var current = loginAs("user@example.com", "password");
        assertThat(current.getId()).isNotEqualTo(before);
    }

    private MockHttpSession loginAs(String email, String password) throws Exception {
        var session = new MockHttpSession();
        var xsrf = xsrf(session);
        mockMvc.perform(post("/login-json").session(session)
                .cookie(xsrf.cookies())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
                .header("X-XSRF-TOKEN", xsrf.token()))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/secure"));
        // Session fixation rotates the id: MockMvc still pins the stale
        // object, so continue with the server-side session.
        return (MockHttpSession) SecureTestController.LAST_SESSION;
    }

    private XsrfState xsrf(MockHttpSession session) throws Exception {
        var result = mockMvc.perform(get("/public").session(session))
            .andExpect(status().isOk())
            .andReturn();
        var response = result.getResponse();
        return new XsrfState(response.getCookie("XSRF-TOKEN").getValue(), response.getCookies());
    }
}
