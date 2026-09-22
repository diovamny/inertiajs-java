package com.example.pingcrm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.pingcrm.controller.AuthController;
import com.example.pingcrm.entity.Account;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.AccountRepository;
import com.example.pingcrm.repository.UserRepository;

/**
 * Security smoke suite for the React demo: Spring Security owns auth + CSRF
 * (mode {@code framework}), BCrypt verifies the seeded users, anonymous
 * Inertia visits get {@code 409} challenges and owner-only routes render the
 * {@code 403} Inertia page for other roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PingCrmReactSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void seed() {
        if (accounts.count() == 0) {
            var account = new Account();
            account.name = "Acme Corporation";
            accounts.save(account);
        }
        var accountId = accounts.findAll().get(0).id;
        users.findByEmail("owner@example.com").orElseGet(() -> {
            var owner = new User();
            owner.accountId = accountId;
            owner.firstName = "Olivia";
            owner.lastName = "Owner";
            owner.email = "owner@example.com";
            owner.password = encoder.encode("secret");
            owner.owner = true;
            return users.save(owner);
        });
        users.findByEmail("user@example.com").orElseGet(() -> {
            var user = new User();
            user.accountId = accountId;
            user.firstName = "Ursula";
            user.lastName = "User";
            user.email = "user@example.com";
            user.password = encoder.encode("secret");
            user.owner = false;
            return users.save(user);
        });
    }

    @Test
    void loginPageIsPublicWithXsrfCookie() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("XSRF-TOKEN"))
            .andExpect(content().string(Matchers.containsString("Auth\\/Login")));
    }

    @Test
    void anonymousInertiaVisitIs409Challenge() throws Exception {
        mockMvc.perform(get("/contacts").header("X-Inertia", "true"))
            .andExpect(status().isConflict())
            .andExpect(header().string("X-Inertia-Location", "/login"))
            .andExpect(header().doesNotExist("X-Inertia"));
    }

    @Test
    void loginWithValidCredentialsSucceeds() throws Exception {
        var session = loginAs("owner@example.com", "secret");
        mockMvc.perform(get("/contacts").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.auth.user.email").value("owner@example.com"));
    }

    @Test
    void loginWithWrongPasswordShowsErrors() throws Exception {
        var fresh = new MockHttpSession();
        var xsrf = xsrf();
        mockMvc.perform(post("/login").session(fresh)
                .cookie(xsrf.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"owner@example.com\",\"password\":\"wrong\"}")
                .header("X-XSRF-TOKEN", xsrf.token())
                .header("X-Inertia", "true")
                .header("Referer", "http://localhost/login"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    void postWithTamperedTokenIs303() throws Exception {
        var session = loginAs("owner@example.com", "secret");
        var state = xsrf();
        mockMvc.perform(post("/contacts").session(session)
                .cookie(state.cookies())
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", "tampered")
                .header("Referer", "http://localhost/contacts")
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isSeeOther());
    }

    @Test
    void nonOwnerIsForbiddenOnUserManagement() throws Exception {
        var session = loginAs("user@example.com", "secret");
        mockMvc.perform(get("/users").session(session).header("X-Inertia", "true"))
            .andExpect(status().isForbidden())
            .andExpect(header().string("X-Inertia", "true"))
            .andExpect(jsonPath("$.component").value("Errors/Forbidden"));
    }

    private record XsrfState(String token, jakarta.servlet.http.Cookie[] cookies) {
    }

    private MockHttpSession loginAs(String email, String password) throws Exception {
        var fresh = new MockHttpSession();
        var xsrf = xsrf();
        mockMvc.perform(post("/login").session(fresh)
                .cookie(xsrf.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
                .header("X-XSRF-TOKEN", xsrf.token()))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/"));
        return (MockHttpSession) AuthController.LAST_SESSION;
    }

    private XsrfState xsrf() throws Exception {
        var result = mockMvc.perform(get("/login").session(new MockHttpSession()))
            .andExpect(status().isOk())
            .andReturn();
        var response = result.getResponse();
        return new XsrfState(response.getCookie("XSRF-TOKEN").getValue(), response.getCookies());
    }
}
