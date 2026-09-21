package com.example.kitchensink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import io.github.diovamny.spring.inertia.testing.InertiaPage;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KitchenSinkSpringTest {

    private static final String BASE = "http://localhost";

    @Autowired
    private MockMvc mockMvc;

    private static MockHttpSession session = new MockHttpSession();

    // ──────────────────────────────────────────────
    // Authentication (Spring Security owns auth + CSRF)
    // ──────────────────────────────────────────────

    @Test
    @Order(1)
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(TEXT_HTML))
            .andExpect(content().string(Matchers.containsString("Auth/Login")));
    }

    @Test
    @Order(2)
    void initialVisitIssuesXsrfCookie() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    @Order(3)
    void rootRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/login"));
    }

    @Test
    @Order(4)
    void anonymousInertiaVisitIs409Challenge() throws Exception {
        mockMvc.perform(get("/").header("X-Inertia", "true"))
            .andExpect(status().isConflict())
            .andExpect(header().string("X-Inertia-Location", "/login"))
            .andExpect(header().doesNotExist("X-Inertia"));
    }

    @Test
    @Order(5)
    void inertiaPostWithoutSessionIs409Challenge() throws Exception {
        // Anonymous visits carry the XSRF cookie from the login page; with a
        // valid token the request reaches authentication and gets the 409
        // challenge (without a token the CSRF layer answers 303 first).
        var anonymous = new MockHttpSession();
        var xsrf = xsrf(anonymous);
        mockMvc.perform(post("/features/state/flash-data").session(anonymous)
                .cookie(xsrf.cookies())
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", xsrf.token()))
            .andExpect(status().isConflict())
            .andExpect(header().string("X-Inertia-Location", "/login"));
    }

    @Test
    @Order(6)
    void loginWithUnknownEmailShowsFlashError() throws Exception {
        var fresh = new MockHttpSession();
        var xsrf = xsrf(fresh);
        mockMvc.perform(post("/login")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/login")
                .header("X-XSRF-TOKEN", xsrf.token())
                .cookie(xsrf.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"whatever\"}")
                .session(fresh))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", BASE + "/login"));

        var page = inertiaGet("/login", fresh);
        assertThat(page.component()).isEqualTo("Auth/Login");
        assertThat(page.props().get("errors"))
            .isEqualTo(Map.of("email", "These credentials do not match our records."));
    }

    @Test
    @Order(7)
    void loginWithValidCredentialsSucceeds() throws Exception {
        session = loginAs("test@example.com", "password");

        var page = inertiaGet("/dashboard", session);
        assertThat(page.component()).isEqualTo("Crm/Dashboard");
        assertThat(nav(page, "auth.user.email")).isEqualTo("test@example.com");
    }

    @Test
    @Order(8)
    void loginPageRendersWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/login").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Auth/Login")));
    }

    @Test
    @Order(9)
    void postWithTamperedTokenIs303WithFlash() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/state/flash-data").session(session)
                .cookie(state.cookies())
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", "tampered")
                .header("Referer", BASE + "/dashboard"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", BASE + "/dashboard"));
    }

    @Test
    @Order(10)
    void maliciousRefererFallsBackToSafePath() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/state/flash-data").session(session)
                .cookie(state.cookies())
                .header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", "tampered")
                .header("Referer", "https://evil.example/phish"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/"));
    }

    // ──────────────────────────────────────────────
    // Precognition (port of PrecognitionQuarkusTest)
    // ──────────────────────────────────────────────

    @Test
    @Order(11)
    void precognitionSuccessIs204() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/forms/precognition")
                .header("Precognition", "true")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"email\":\"jane@example.com\","
                    + "\"password\":\"secret123\",\"password_confirmation\":\"secret123\"}")
                .session(session))
            .andExpect(status().isNoContent())
            .andExpect(header().string("Precognition-Success", "true"))
            .andExpect(header().string("Precognition", "true"));
    }

    @Test
    @Order(12)
    void precognitionWithErrorsIs422() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/forms/precognition")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "username,email")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"a\",\"email\":\"not-an-email\"}")
                .session(session))
            .andExpect(status().isUnprocessableContent())
            .andExpect(header().string("Precognition", "true"))
            .andExpect(header().string("Vary", Matchers.containsString("Precognition")))
            .andExpect(jsonPath("$.errors.username").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @Order(13)
    void precognitionPasswordMismatchIs422() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/forms/precognition")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "password,password_confirmation")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"password\":\"secret123\",\"password_confirmation\":\"different\"}")
                .session(session))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.errors.password_confirmation")
                .value("The password confirmation does not match."));
    }

    @Test
    @Order(14)
    void withoutPrecognitionRendersPageAndValidates() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/forms/precognition")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/features/forms/precognition")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"a\",\"email\":\"bad\",\"password\":\"x\",\"password_confirmation\":\"x\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", BASE + "/features/forms/precognition"));

        var page = inertiaGet("/features/forms/precognition", session);
        assertThat(page.props().get("errors")).isNotNull();
    }

    // ──────────────────────────────────────────────
    // Smoke: CRM pages
    // ──────────────────────────────────────────────

    @Test
    @Order(15)
    void contactsArePaginatedAndSearchable() throws Exception {
        var page = inertiaGet("/contacts", session);
        assertThat(page.component()).isEqualTo("Contacts/Index");
        assertThat(list(page, "contacts.data")).hasSize(15);
        assertThat(nav(page, "contacts.data[0].first_name")).isNotNull();

        var empty = inertiaGet("/contacts?search=zzzznotfound", session);
        assertThat(list(empty, "contacts.data")).isEmpty();
    }

    @Test
    @Order(16)
    void organizationsArePaginated() throws Exception {
        var page = inertiaGet("/organizations", session);
        assertThat(page.component()).isEqualTo("Organizations/Index");
        assertThat(list(page, "organizations.data")).hasSize(15);
    }

    // ──────────────────────────────────────────────
    // Smoke: feature pages
    // ──────────────────────────────────────────────

    @Test
    @Order(17)
    void deferredPropsSkipDeferredSectionsOnFullVisit() throws Exception {
        var page = inertiaGet("/features/data-loading/deferred-props", session);
        assertThat(page.component()).isEqualTo("Features/DataLoading/DeferredProps");
        assertThat(nav(page, "quickStat")).isEqualTo("Loaded instantly");
        assertThat(page.hasProp("slowStats")).isFalse();
        assertThat(page.hasProp("heavyData")).isFalse();
    }

    @Test
    @Order(18)
    void partialReloadResolvesDeferredProps() throws Exception {
        var result = mockMvc.perform(get("/features/data-loading/deferred-props")
                .header("X-Inertia", "true")
                .header("X-Inertia-Partial-Component", "Features/DataLoading/DeferredProps")
                .header("X-Inertia-Partial-Data", "slowStats")
                .session(session))
            .andExpect(status().isOk())
            .andReturn();
        var page = page(result);
        assertThat(nav(page, "slowStats.totalContacts")).isNotNull();
        assertThat(nav(page, "slowStats.totalFavorites")).isNotNull();
    }

    @Test
    @Order(19)
    void propMergingMergesInitialProps() throws Exception {
        var page = inertiaGet("/features/data-loading/prop-merging", session);
        assertThat(page.component()).isEqualTo("Features/DataLoading/PropMerging");
        assertThat(list(page, "contacts")).hasSize(1);
        assertThat(list(page, "notifications")).hasSize(1);
        assertThat(list(page, "activities")).hasSize(1);
    }

    @Test
    @Order(20)
    void oncePropsRender() throws Exception {
        var page = inertiaGet("/features/data-loading/once-props", session);
        assertThat(page.component()).isEqualTo("Features/DataLoading/OnceProps");
        assertThat(nav(page, "staticData.randomId")).isNotNull();
        assertThat(nav(page, "freshData.value")).isNotNull();
        assertThat(nav(page, "dynamicData.timestamp")).isNotNull();
    }

    @Test
    @Order(21)
    void navigationLinksRender() throws Exception {
        var page = inertiaGet("/features/navigation/links", session);
        assertThat(page.component()).isEqualTo("Features/Navigation/Links");
    }

    @Test
    @Order(22)
    void fileUploadsAcceptMultipleFiles() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .multipart("/features/forms/file-uploads")
                .file(new org.springframework.mock.web.MockMultipartFile(
                    "files", "a.txt", "text/plain", "a".getBytes()))
                .file(new org.springframework.mock.web.MockMultipartFile(
                    "files", "b.txt", "text/plain", "b".getBytes()))
                .file(new org.springframework.mock.web.MockMultipartFile(
                    "photo", "photo.png", "image/png", new byte[] { 1, 2, 3 }))
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/features/forms/file-uploads")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .session(session))
            .andExpect(status().isSeeOther());

        var page = inertiaGet("/features/forms/file-uploads", session);
        assertThat(page.props().get("message")).isEqualTo("Uploaded 3 file(s) successfully!");
    }

    @Test
    @Order(23)
    void fileUploadsRejectMoreThanFiveFiles() throws Exception {
        var state = xsrf(session);
        var request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .multipart("/features/forms/file-uploads")
            .header("X-Inertia", "true")
            .header("Referer", BASE + "/features/forms/file-uploads")
            .header("X-XSRF-TOKEN", state.token())
            .cookie(state.cookies())
            .session(session);
        for (int i = 0; i < 6; i++) {
            request.file(new org.springframework.mock.web.MockMultipartFile(
                "files", "f" + i + ".txt", "text/plain", "x".getBytes()));
        }
        mockMvc.perform(request).andExpect(status().isSeeOther());

        var page = inertiaGet("/features/forms/file-uploads", session);
        assertThat(nav(page, "errors.files"))
            .isEqualTo("The files field must not have more than 5 items.");
    }

    @Test
    @Order(24)
    void httpApiEndpointReturnsJson() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/http/use-http/api")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Inertia\"}")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Hello, Inertia!"));
    }

    // ──────────────────────────────────────────────
    // Smoke: error pages (ErrorPage component)
    // ──────────────────────────────────────────────

    @Test
    @Order(25)
    void httpExceptionsRenderErrorPage() throws Exception {
        var forbidden = inertiaErrorGet("/features/errors/http-exceptions/403", session);
        assertThat(forbidden.component()).isEqualTo("ErrorPage");
        assertThat(forbidden.props().get("status")).isEqualTo(403);

        var notFound = inertiaErrorGet("/features/errors/http-exceptions/404", session);
        assertThat(notFound.component()).isEqualTo("ErrorPage");
        assertThat(notFound.props().get("status")).isEqualTo(404);

        var serverError = inertiaErrorGet("/features/errors/http-exceptions/500", session);
        assertThat(serverError.component()).isEqualTo("ErrorPage");
        assertThat(serverError.props().get("status")).isEqualTo(500);
    }

    @Test
    @Order(26)
    void javaExceptionsMapToSemanticStatuses() throws Exception {
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/400", session).props().get("status"))
            .isEqualTo(400);
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/403", session).props().get("status"))
            .isEqualTo(403);
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/409", session).props().get("status"))
            .isEqualTo(409);
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/422", session).props().get("status"))
            .isEqualTo(422);
    }

    @Test
    @Order(27)
    void useFormContextPrecognitionOnlyReportsRequestedField() throws Exception {
        var state = xsrf(session);
        mockMvc.perform(post("/features/forms/form-component")
                .header("X-Inertia", "true")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "name")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"not-an-email\",\"bio\":\"\",\"role\":\"developer\"}")
                .session(session))
            .andExpect(status().isUnprocessableContent())
            .andExpect(header().string("Precognition", "true"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").doesNotExist())
            .andExpect(jsonPath("$.errors.bio").doesNotExist());
    }

    @Test
    @Order(28)
    void logoutClearsSession() throws Exception {
        var state = xsrf(session);
        // POST /logout is handled by Spring Security itself (session
        // invalidated, 302 to the login page).
        mockMvc.perform(post("/logout").header("X-Inertia", "true")
                .header("X-XSRF-TOKEN", state.token())
                .cookie(state.cookies())
                .session(session))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/login?logout"));

        mockMvc.perform(get("/").session(session))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/login"));
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private record XsrfState(String token, jakarta.servlet.http.Cookie[] cookies) {
    }

    private MockHttpSession loginAs(String email, String password) throws Exception {
        var fresh = new MockHttpSession();
        var xsrf = xsrf(fresh);
        mockMvc.perform(post("/login").session(fresh)
                .cookie(xsrf.cookies())
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
                .header("X-XSRF-TOKEN", xsrf.token()))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/dashboard"));
        // Session fixation rotates the id on login; MockMvc still pins the
        // stale object, so continue with the server-side session.
        return (MockHttpSession) com.example.kitchensink.controller.AuthController.LAST_SESSION;
    }

    private XsrfState xsrf(MockHttpSession target) throws Exception {
        var result = mockMvc.perform(get("/login").session(target))
            .andExpect(status().isOk())
            .andReturn();
        var response = result.getResponse();
        return new XsrfState(response.getCookie("XSRF-TOKEN").getValue(), response.getCookies());
    }

    private InertiaPage inertiaGet(String path, MockHttpSession current) throws Exception {
        return page(mockMvc.perform(get(path).header("X-Inertia", "true").session(current))
            .andExpect(status().isOk())
            .andReturn());
    }

    private InertiaPage inertiaErrorGet(String path, MockHttpSession current) throws Exception {
        return page(mockMvc.perform(get(path).header("X-Inertia", "true").session(current))
            .andReturn());
    }

    private static InertiaPage page(MvcResult result) throws Exception {
        return InertiaPage.fromJson(result.getResponse().getContentAsString());
    }

    private static Object nav(InertiaPage page, String path) {
        return nav(page.props(), path);
    }

    @SuppressWarnings("unchecked")
    private static Object nav(Object node, String path) {
        for (var part : path.split("\\.")) {
            var bracket = part.indexOf('[');
            if (bracket >= 0) {
                var name = part.substring(0, bracket);
                var index = Integer.parseInt(part.substring(bracket + 1, part.indexOf(']')));
                node = ((Map<String, Object>) node).get(name);
                node = ((List<Object>) node).get(index);
            } else {
                node = ((Map<String, Object>) node).get(part);
            }
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(InertiaPage page, String path) {
        return (List<Map<String, Object>>) nav(page, path);
    }
}
