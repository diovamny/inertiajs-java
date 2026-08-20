package com.example.kitchensink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import io.github.dg.spring.inertia.testing.InertiaPage;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KitchenSinkSpringTest {

    private static final String BASE = "http://localhost";

    @Autowired
    private MockMvc mockMvc;

    private static final MockHttpSession session = new MockHttpSession();

    // ──────────────────────────────────────────────
    // Authentication
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
    void rootRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/login"));
    }

    @Test
    @Order(3)
    void inertiaPostWithoutSessionRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/features/state/flash-data").header("X-Inertia", "true"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/login"))
            .andExpect(header().string("X-Inertia-Location", "/login"));
    }

    @Test
    @Order(4)
    void loginWithUnknownEmailShowsFlashError() throws Exception {
        mockMvc.perform(post("/login")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/login")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"whatever\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", BASE + "/login"));

        var page = inertiaGet("/login");
        assertThat(page.component()).isEqualTo("Auth/Login");
        assertThat(page.props().get("errors"))
            .isEqualTo(Map.of("email", "These credentials do not match our records."));
    }

    @Test
    @Order(5)
    void loginWithValidCredentialsSucceeds() throws Exception {
        mockMvc.perform(post("/login")
                .header("X-Inertia", "true")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/dashboard"));

        var page = inertiaGet("/dashboard");
        assertThat(page.component()).isEqualTo("Crm/Dashboard");
        assertThat(nav(page, "auth.user.email")).isEqualTo("test@example.com");
    }

    @Test
    @Order(6)
    void loginPageIsPublicEvenWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/login").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Auth/Login")));
    }

    // ──────────────────────────────────────────────
    // Precognition (port of PrecognitionQuarkusTest)
    // ──────────────────────────────────────────────

    @Test
    @Order(7)
    void precognitionSuccessIs204() throws Exception {
        mockMvc.perform(post("/features/forms/precognition")
                .header("Precognition", "true")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"email\":\"jane@example.com\","
                    + "\"password\":\"secret123\",\"password_confirmation\":\"secret123\"}")
                .session(session))
            .andExpect(status().isNoContent())
            .andExpect(header().string("Precognition-Success", "true"))
            .andExpect(header().string("Precognition", "true"));
    }

    @Test
    @Order(8)
    void precognitionWithErrorsIs422() throws Exception {
        mockMvc.perform(post("/features/forms/precognition")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "username,email")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"a\",\"email\":\"not-an-email\"}")
                .session(session))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Precognition", "true"))
            .andExpect(header().string("Vary", Matchers.containsString("Precognition")))
            .andExpect(jsonPath("$.errors.username").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @Order(9)
    void precognitionPasswordMismatchIs422() throws Exception {
        mockMvc.perform(post("/features/forms/precognition")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "password,password_confirmation")
                .contentType(APPLICATION_JSON)
                .content("{\"password\":\"secret123\",\"password_confirmation\":\"different\"}")
                .session(session))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors.password_confirmation")
                .value("The password confirmation does not match."));
    }

    @Test
    @Order(10)
    void withoutPrecognitionRendersPageAndValidates() throws Exception {
        mockMvc.perform(post("/features/forms/precognition")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/features/forms/precognition")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"a\",\"email\":\"bad\",\"password\":\"x\",\"password_confirmation\":\"x\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", BASE + "/features/forms/precognition"));

        var page = inertiaGet("/features/forms/precognition");
        assertThat(page.props().get("errors")).isNotNull();
    }

    // ──────────────────────────────────────────────
    // Smoke: CRM pages
    // ──────────────────────────────────────────────

    @Test
    @Order(11)
    void contactsArePaginatedAndSearchable() throws Exception {
        var page = inertiaGet("/contacts");
        assertThat(page.component()).isEqualTo("Contacts/Index");
        assertThat(list(page, "contacts.data")).hasSize(15);
        assertThat(nav(page, "contacts.data[0].first_name")).isNotNull();

        var empty = inertiaGet("/contacts?search=zzzznotfound");
        assertThat(list(empty, "contacts.data")).isEmpty();
    }

    @Test
    @Order(12)
    void organizationsArePaginated() throws Exception {
        var page = inertiaGet("/organizations");
        assertThat(page.component()).isEqualTo("Organizations/Index");
        assertThat(list(page, "organizations.data")).hasSize(15);
    }

    // ──────────────────────────────────────────────
    // Smoke: feature pages
    // ──────────────────────────────────────────────

    @Test
    @Order(13)
    void deferredPropsSkipDeferredSectionsOnFullVisit() throws Exception {
        var page = inertiaGet("/features/data-loading/deferred-props");
        assertThat(page.component()).isEqualTo("Features/DataLoading/DeferredProps");
        assertThat(nav(page, "quickStat")).isEqualTo("Loaded instantly");
        assertThat(page.hasProp("slowStats")).isFalse();
        assertThat(page.hasProp("heavyData")).isFalse();
    }

    @Test
    @Order(14)
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
    @Order(15)
    void propMergingMergesInitialProps() throws Exception {
        var page = inertiaGet("/features/data-loading/prop-merging");
        assertThat(page.component()).isEqualTo("Features/DataLoading/PropMerging");
        assertThat(list(page, "contacts")).hasSize(1);
        assertThat(list(page, "notifications")).hasSize(1);
        assertThat(list(page, "activities")).hasSize(1);
    }

    @Test
    @Order(16)
    void oncePropsRender() throws Exception {
        var page = inertiaGet("/features/data-loading/once-props");
        assertThat(page.component()).isEqualTo("Features/DataLoading/OnceProps");
        assertThat(nav(page, "staticData.randomId")).isNotNull();
        assertThat(nav(page, "freshData.value")).isNotNull();
        assertThat(nav(page, "dynamicData.timestamp")).isNotNull();
    }

    @Test
    @Order(17)
    void navigationLinksRender() throws Exception {
        var page = inertiaGet("/features/navigation/links");
        assertThat(page.component()).isEqualTo("Features/Navigation/Links");
    }

    @Test
    @Order(18)
    void fileUploadsAcceptMultipleFiles() throws Exception {
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
                .session(session))
            .andExpect(status().isSeeOther());

        var page = inertiaGet("/features/forms/file-uploads");
        assertThat(page.props().get("message")).isEqualTo("Uploaded 3 file(s) successfully!");
    }

    @Test
    @Order(19)
    void fileUploadsRejectMoreThanFiveFiles() throws Exception {
        var request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .multipart("/features/forms/file-uploads")
            .header("X-Inertia", "true")
            .header("Referer", BASE + "/features/forms/file-uploads")
            .session(session);
        for (int i = 0; i < 6; i++) {
            request.file(new org.springframework.mock.web.MockMultipartFile(
                "files", "f" + i + ".txt", "text/plain", "x".getBytes()));
        }
        mockMvc.perform(request).andExpect(status().isSeeOther());

        var page = inertiaGet("/features/forms/file-uploads");
        assertThat(nav(page, "errors.files"))
            .isEqualTo("The files field must not have more than 5 items.");
    }

    @Test
    @Order(20)
    void httpApiEndpointReturnsJson() throws Exception {
        mockMvc.perform(post("/features/http/use-http/api")
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
    @Order(21)
    void httpExceptionsRenderErrorPage() throws Exception {
        var forbidden = inertiaErrorGet("/features/errors/http-exceptions/403");
        assertThat(forbidden.component()).isEqualTo("ErrorPage");
        assertThat(forbidden.props().get("status")).isEqualTo(403);

        var notFound = inertiaErrorGet("/features/errors/http-exceptions/404");
        assertThat(notFound.component()).isEqualTo("ErrorPage");
        assertThat(notFound.props().get("status")).isEqualTo(404);

        var serverError = inertiaErrorGet("/features/errors/http-exceptions/500");
        assertThat(serverError.component()).isEqualTo("ErrorPage");
        assertThat(serverError.props().get("status")).isEqualTo(500);
    }

    @Test
    @Order(22)
    void javaExceptionsMapToSemanticStatuses() throws Exception {
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/400").props().get("status"))
            .isEqualTo(400);
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/403").props().get("status"))
            .isEqualTo(403);
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/409").props().get("status"))
            .isEqualTo(409);
        assertThat(inertiaErrorGet("/features/errors/java-exceptions/422").props().get("status"))
            .isEqualTo(422);
    }

    @Test
    @Order(23)
    void useFormContextPrecognitionOnlyReportsRequestedField() throws Exception {
        mockMvc.perform(post("/features/forms/form-component")
                .header("X-Inertia", "true")
                .header("Precognition", "true")
                .header("Precognition-Validate-Only", "name")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"not-an-email\",\"bio\":\"\",\"role\":\"developer\"}")
                .session(session))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Precognition", "true"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").doesNotExist())
            .andExpect(jsonPath("$.errors.bio").doesNotExist());
    }

    @Test
    @Order(24)
    void logoutClearsSession() throws Exception {
        mockMvc.perform(post("/logout").header("X-Inertia", "true").session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/login"));

        mockMvc.perform(get("/").session(session))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/login"));
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private InertiaPage inertiaGet(String path) throws Exception {
        return page(mockMvc.perform(get(path).header("X-Inertia", "true").session(session))
            .andExpect(status().isOk())
            .andReturn());
    }

    private InertiaPage inertiaErrorGet(String path) throws Exception {
        return page(mockMvc.perform(get(path).header("X-Inertia", "true").session(session))
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