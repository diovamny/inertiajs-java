package com.example.pingcrm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import io.github.dg.spring.inertia.testing.InertiaPage;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PingCrmSpringTest {

    private static final String BASE = "http://localhost";

    @Autowired
    private MockMvc mockMvc;

    private static final MockHttpSession session = new MockHttpSession();

    private static long organizationId;
    private static long contactId;
    private static String photoUrl;

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
        mockMvc.perform(delete("/organizations/1").header("X-Inertia", "true"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/login"))
            .andExpect(header().string("X-Inertia-Location", "/login"));
    }

    @Test
    @Order(4)
    void loginWithBadCredentialsShowsFlashError() throws Exception {
        mockMvc.perform(post("/login")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/login")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"johndoe@example.com\",\"password\":\"wrong\"}")
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
                .content("{\"email\":\"johndoe@example.com\",\"password\":\"secret\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/"));

        var page = inertiaGet("/");
        assertThat(page.component()).isEqualTo("Dashboard/Index");
        assertThat(nav(page, "auth.user.email")).isEqualTo("johndoe@example.com");
        assertThat(nav(page, "auth.user.first_name")).isEqualTo("John");
        assertThat(nav(page, "auth.user.owner")).isEqualTo(true);
        assertThat(nav(page, "auth.user.account.name")).isEqualTo("Acme Corporation");
    }

    @Test
    @Order(6)
    void authenticatedUserCannotVisitLoginAgain() throws Exception {
        mockMvc.perform(get("/login").session(session))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/"));
    }

    // ──────────────────────────────────────────────
    // Organizations
    // ──────────────────────────────────────────────

    @Test
    @Order(7)
    void organizationsArePaginated() throws Exception {
        var page = inertiaGet("/organizations");
        assertThat(page.component()).isEqualTo("Organizations/Index");
        assertThat(list(page, "organizations.data")).hasSize(10);
        assertThat(list(page, "organizations.links")).hasSizeGreaterThanOrEqualTo(6);
        var filters = new java.util.HashMap<String, Object>();
        filters.put("search", null);
        filters.put("trashed", null);
        assertThat(page.props().get("filters")).isEqualTo(filters);
        assertThat(firstActiveLabel(list(page, "organizations.links"))).isEqualTo("1");
        assertThat(nav(page, "organizations.data[0].name")).isNotNull();
    }

    @Test
    @Order(8)
    void organizationsSearchWithNoMatchesIsEmpty() throws Exception {
        var page = inertiaGet("/organizations?search=zzzznotfound");
        assertThat(list(page, "organizations.data")).isEmpty();
        assertThat(nav(page, "filters.search")).isEqualTo("zzzznotfound");
    }

    @Test
    @Order(9)
    void createOrganizationRedirectsAndFlashesSuccess() throws Exception {
        mockMvc.perform(post("/organizations")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/organizations/create")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Inertia Test Org\",\"email\":\"org@test.dev\",\"city\":\"Ottawa\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/organizations"));

        var page = inertiaGet("/organizations?search=Inertia");
        assertThat(page.props().get("success")).isEqualTo("Organization created.");
        assertThat(nav(page, "organizations.data[0].name")).isEqualTo("Inertia Test Org");
        organizationId = (long) (int) nav(page, "organizations.data[0].id");
    }

    @Test
    @Order(10)
    void organizationValidationErrorsAreFlashed() throws Exception {
        mockMvc.perform(post("/organizations")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/organizations/create")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", BASE + "/organizations/create"));

        var page = inertiaGet("/organizations/create");
        assertThat(nav(page, "errors.name")).isEqualTo("required");
        assertThat(nav(page, "errors.email")).isEqualTo("invalid");
    }

    @Test
    @Order(11)
    void updateOrganizationRedirectsBack() throws Exception {
        mockMvc.perform(put("/organizations/" + organizationId)
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/organizations/" + organizationId + "/edit")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Inertia Test Org Renamed\",\"city\":\"Toronto\"}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", BASE + "/organizations/" + organizationId + "/edit"));

        var page = inertiaGet("/organizations/" + organizationId + "/edit");
        assertThat(page.props().get("success")).isEqualTo("Organization updated.");
        assertThat(nav(page, "organization.name")).isEqualTo("Inertia Test Org Renamed");
        assertThat(nav(page, "organization.city")).isEqualTo("Toronto");
    }

    @Test
    @Order(12)
    void deleteOrganizationMovesItToTrashAndCanRestore() throws Exception {
        mockMvc.perform(delete("/organizations/" + organizationId)
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/organizations/" + organizationId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var trashed = inertiaGet("/organizations?trashed=only");
        assertThat(list(trashed, "organizations.data")).hasSize(1);
        assertThat(nav(trashed, "organizations.data[0].id")).isEqualTo((int) organizationId);
        assertThat(nav(trashed, "organizations.data[0].deleted_at")).isNotNull();

        var all = inertiaGet("/organizations");
        assertThat(list(all, "organizations.data").stream()
            .map(item -> ((Number) ((Map<?, ?>) item).get("id")).longValue()).toList())
            .doesNotContain(organizationId);

        mockMvc.perform(put("/organizations/" + organizationId + "/restore")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/organizations/" + organizationId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var afterRestore = inertiaGet("/organizations?trashed=only");
        assertThat(list(afterRestore, "organizations.data")).isEmpty();
    }

    @Test
    @Order(13)
    void editMissingOrganizationRedirectsWithError() throws Exception {
        mockMvc.perform(get("/organizations/999999/edit").header("X-Inertia", "true").session(session))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/organizations"));
    }

    // ──────────────────────────────────────────────
    // Contacts
    // ──────────────────────────────────────────────

    @Test
    @Order(14)
    void contactsArePaginatedAndSearchable() throws Exception {
        var page = inertiaGet("/contacts");
        assertThat(page.component()).isEqualTo("Contacts/Index");
        assertThat(list(page, "contacts.data")).hasSize(10);
        assertThat(nav(page, "contacts.data[0].name")).isNotNull();

        var empty = inertiaGet("/contacts?search=zzzznotfound");
        assertThat(list(empty, "contacts.data")).isEmpty();
    }

    @Test
    @Order(15)
    void createContactWithOrganization() throws Exception {
        mockMvc.perform(post("/contacts")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/contacts/create")
                .contentType(APPLICATION_JSON)
                .content("{\"first_name\":\"Jane\",\"last_name\":\"Doe\",\"email\":\"jane@test.dev\","
                    + "\"organization_id\":" + organizationId + "}")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/contacts"));

        var page = inertiaGet("/contacts?search=Doe");
        assertThat(page.props().get("success")).isEqualTo("Contact created.");
        assertThat(nav(page, "contacts.data[0].name")).isEqualTo("Jane Doe");
        assertThat(nav(page, "contacts.data[0].organization.name")).isEqualTo("Inertia Test Org Renamed");
        contactId = (long) (int) nav(page, "contacts.data[0].id");

        var orgPage = inertiaGet("/organizations/" + organizationId + "/edit");
        assertThat(nav(orgPage, "organization.contacts[0].name")).isEqualTo("Jane Doe");
    }

    @Test
    @Order(16)
    void contactWithInvalidOrganizationFails() throws Exception {
        mockMvc.perform(post("/contacts")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/contacts/create")
                .contentType(APPLICATION_JSON)
                .content("{\"first_name\":\"Bad\",\"last_name\":\"Org\",\"organization_id\":999999}")
                .session(session))
            .andExpect(status().isSeeOther());

        var page = inertiaGet("/contacts/create");
        assertThat(nav(page, "errors.organization_id"))
            .isEqualTo("The selected organization is invalid.");
    }

    @Test
    @Order(17)
    void updateAndDeleteContact() throws Exception {
        mockMvc.perform(put("/contacts/" + contactId)
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/contacts/" + contactId + "/edit")
                .contentType(APPLICATION_JSON)
                .content("{\"first_name\":\"Jane\",\"last_name\":\"Doe\",\"city\":\"Vancouver\"}")
                .session(session))
            .andExpect(status().isSeeOther());

        var edit = inertiaGet("/contacts/" + contactId + "/edit");
        assertThat(nav(edit, "contact.city")).isEqualTo("Vancouver");

        mockMvc.perform(delete("/contacts/" + contactId)
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/contacts/" + contactId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var trashed = inertiaGet("/contacts?trashed=only");
        assertThat(nav(trashed, "contacts.data[0].id")).isEqualTo((int) contactId);

        mockMvc.perform(put("/contacts/" + contactId + "/restore")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/contacts/" + contactId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var afterRestore = inertiaGet("/contacts?trashed=only");
        assertThat(list(afterRestore, "contacts.data")).isEmpty();
    }

    // ──────────────────────────────────────────────
    // Users
    // ──────────────────────────────────────────────

    @Test
    @Order(18)
    void usersIndexListsDemoUser() throws Exception {
        var page = inertiaGet("/users");
        assertThat(page.component()).isEqualTo("Users/Index");
        assertThat(list(page, "users")).hasSize(1);
        assertThat(nav(page, "users[0].email")).isEqualTo("johndoe@example.com");
        assertThat(nav(page, "users[0].owner")).isEqualTo(true);
        assertThat(nav(page, "users[0].photo")).isNull();
    }

    @Test
    @Order(19)
    void createUserWithPhotoUpload() throws Exception {
        var png = pngBytes();
        mockMvc.perform(multipart("/users")
                .file(new MockMultipartFile("photo", "avatar.png", "image/png", png))
                .param("first_name", "Jane")
                .param("last_name", "Roe")
                .param("email", "jane.roe@example.com")
                .param("password", "secret123")
                .param("owner", "false")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/users/create")
                .session(session))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/users"));

        var page = inertiaGet("/users");
        assertThat(page.props().get("success")).isEqualTo("User created.");
        assertThat(list(page, "users")).hasSize(2);
        var jane = findByEmail(list(page, "users"), "jane.roe@example.com");
        assertThat(jane).isNotNull();
        photoUrl = (String) jane.get("photo");
        assertThat(photoUrl).isNotNull();

        mockMvc.perform(get(photoUrl).session(session))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andExpect(header().string("Cache-Control", Matchers.containsString("max-age")));
    }

    @Test
    @Order(20)
    void duplicateUserEmailIsRejected() throws Exception {
        mockMvc.perform(multipart("/users")
                .param("first_name", "Dup")
                .param("last_name", "User")
                .param("email", "jane.roe@example.com")
                .param("owner", "false")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/users/create")
                .session(session))
            .andExpect(status().isSeeOther());

        var page = inertiaGet("/users/create");
        assertThat(nav(page, "errors.email")).isEqualTo("The email has already been taken.");
    }

    @Test
    @Order(21)
    void demoUserCannotBeModifiedOrDeleted() throws Exception {
        var users = inertiaGet("/users");
        var demoId = (int) nav(users, "users[0].id");

        mockMvc.perform(multipart("/users/" + demoId)
                .param("first_name", "Hacked")
                .param("last_name", "User")
                .param("email", "johndoe@example.com")
                .param("owner", "true")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/users/" + demoId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var edit = inertiaGet("/users/" + demoId + "/edit");
        assertThat(edit.props().get("error"))
            .isEqualTo("Updating the demo user is not allowed.");
        assertThat(nav(edit, "user.first_name")).isEqualTo("John");

        mockMvc.perform(delete("/users/" + demoId)
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/users/" + demoId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var edit2 = inertiaGet("/users/" + demoId + "/edit");
        assertThat(edit2.props().get("error"))
            .isEqualTo("Deleting the demo user is not allowed.");
    }

    @Test
    @Order(22)
    void userCanBeDeletedAndRestored() throws Exception {
        var users = inertiaGet("/users");
        var jane = findByEmail(list(users, "users"), "jane.roe@example.com");
        var userId = (int) jane.get("id");

        mockMvc.perform(delete("/users/" + userId)
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/users/" + userId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var trashed = inertiaGet("/users?trashed=only");
        assertThat(list(trashed, "users")).hasSize(1);
        assertThat(nav(trashed, "users[0].id")).isEqualTo(userId);

        mockMvc.perform(put("/users/" + userId + "/restore")
                .header("X-Inertia", "true")
                .header("Referer", BASE + "/users/" + userId + "/edit")
                .session(session))
            .andExpect(status().isSeeOther());

        var afterRestore = inertiaGet("/users?trashed=only");
        assertThat(list(afterRestore, "users")).isEmpty();
    }

    // ──────────────────────────────────────────────
    // Reports, images, logout
    // ──────────────────────────────────────────────

    @Test
    @Order(23)
    void reportsPageRenders() throws Exception {
        var page = inertiaGet("/reports");
        assertThat(page.component()).isEqualTo("Reports/Index");
    }

    @Test
    @Order(24)
    void imageRequestsAreServedOrRejectedSafely() throws Exception {
        mockMvc.perform(get("/img/missing.png").session(session))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/img/../application.properties").session(session))
            .andExpect(status().isNotFound());

        mockMvc.perform(get(photoUrl).session(session))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"));
    }

    @Test
    @Order(25)
    void logoutClearsSession() throws Exception {
        mockMvc.perform(delete("/logout").header("X-Inertia", "true").session(session))
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

    private static Map<String, Object> findByEmail(List<Map<String, Object>> items, String email) {
        return items.stream()
            .filter(item -> email.equals(item.get("email")))
            .findFirst()
            .orElse(null);
    }

    private static String firstActiveLabel(List<Map<String, Object>> links) {
        return links.stream()
            .filter(link -> Boolean.TRUE.equals(link.get("active")))
            .map(link -> (String) link.get("label"))
            .findFirst()
            .orElse(null);
    }

    private static byte[] pngBytes() throws Exception {
        var image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawString("avatar", 5, 32);
        var output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(image, "png", output)).isTrue();
        return output.toByteArray();
    }
}
