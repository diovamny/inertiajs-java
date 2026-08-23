package com.example.pingcrm;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PingCrmTest {

    @BeforeAll
    static void configure() {
        RestAssured.config = RestAssured.config()
                .redirect(io.restassured.config.RedirectConfig.redirectConfig().followRedirects(false));
    }

    private static String session;
    private static long organizationId;
    private static long contactId;
    private static String photoUrl;

    private static final String BASE = "";

    private static RequestSpecification inertia() {
        return given().header("X-Inertia", "true");
    }

    private static RequestSpecification authed() {
        return given().cookie("vertx-web.session", session);
    }

    private static RequestSpecification authedInertia() {
        return authed().header("X-Inertia", "true");
    }

    private static void captureSession(ExtractableResponse<Response> res) {
        var cookie = res.detailedCookies().get("vertx-web.session");
        if (cookie != null) {
            session = cookie.getValue();
        }
    }

    // ──────────────────────────────────────────────
    // Authentication
    // ──────────────────────────────────────────────

    @Test
    @Order(1)
    void loginPageIsPublic() {
        given().when().get("/login")
            .then().statusCode(200)
            .and().contentType(ContentType.HTML)
            .and().body(containsString("Auth/Login"));
    }

    @Test
    @Order(2)
    void rootRequiresAuthentication() {
        given().when().get("/")
            .then().statusCode(302)
            .and().header("Location", equalTo("/login"));
    }

    @Test
    @Order(3)
    void inertiaPostWithoutSessionRedirectsToLogin() {
        inertia().when().delete("/organizations/1")
            .then().statusCode(303)
            .and().header("Location", equalTo("/login"))
            .and().header("X-Inertia-Location", equalTo("/login"));
    }

    @Test
    @Order(4)
    void loginWithBadCredentialsShowsFlashError() {
        var res = inertia()
            .header("Referer", BASE + "/login")
            .contentType(ContentType.JSON)
            .body("{\"email\":\"johndoe@example.com\",\"password\":\"wrong\"}")
            .when().post("/login")
            .then().statusCode(303)
            .and().header("Location", equalTo(BASE + "/login"))
            .extract();
        captureSession(res);

        authedInertia().when().get("/login")
            .then().statusCode(200)
            .and().contentType(ContentType.JSON)
            .and().body("component", equalTo("Auth/Login"))
            .and().body("props.errors.email",
                equalTo("These credentials do not match our records."));
    }

    @Test
    @Order(5)
    void loginWithValidCredentialsSucceeds() {
        var res = inertia()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"johndoe@example.com\",\"password\":\"secret\"}")
            .when().post("/login")
            .then().statusCode(303)
            .and().header("Location", equalTo("/"))
            .extract();
        captureSession(res);

        authedInertia().when().get("/")
            .then().statusCode(200)
            .and().contentType(ContentType.JSON)
            .and().body("component", equalTo("Dashboard/Index"))
            .and().body("props.auth.user.email", equalTo("johndoe@example.com"))
            .and().body("props.auth.user.first_name", equalTo("John"))
            .and().body("props.auth.user.owner", equalTo(true))
            .and().body("props.auth.user.account.name", equalTo("Acme Corporation"));
    }

    @Test
    @Order(6)
    void authenticatedUserCannotVisitLoginAgain() {
        authed().when().get("/login")
            .then().statusCode(302)
            .and().header("Location", equalTo("/"));
    }

    // ──────────────────────────────────────────────
    // Organizations
    // ──────────────────────────────────────────────

    @Test
    @Order(7)
    void organizationsArePaginated() {
        authedInertia().when().get("/organizations")
            .then().statusCode(200)
            .and().body("component", equalTo("Organizations/Index"))
            .and().body("props.organizations.data.size()", equalTo(10))
            .and().body("props.organizations.links.size()", greaterThanOrEqualTo(6))
            .and().body("props.organizations.links.find { it.active }.label", equalTo("1"))
            .and().body("props.organizations.data[0].name", notNullValue())
            .and().body("props.filters.search", nullValue());
    }

    @Test
    @Order(8)
    void organizationsSearchWithNoMatchesIsEmpty() {
        authedInertia().when().get("/organizations?search=zzzznotfound")
            .then().statusCode(200)
            .and().body("props.organizations.data.size()", equalTo(0))
            .and().body("props.filters.search", equalTo("zzzznotfound"));
    }

    @Test
    @Order(9)
    void createOrganizationRedirectsAndFlashesSuccess() {
        var res = authedInertia()
            .header("Referer", BASE + "/organizations/create")
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Inertia Test Org\",\"email\":\"org@test.dev\",\"city\":\"Ottawa\"}")
            .when().post("/organizations")
            .then().statusCode(303)
            .and().header("Location", equalTo("/organizations"))
            .extract();
        captureSession(res);

        var page = authedInertia().when().get("/organizations?search=Inertia")
            .then().statusCode(200)
            .and().body("props.success", equalTo("Organization created."))
            .and().body("props.organizations.data[0].name",
                equalTo("Inertia Test Org"))
            .extract().jsonPath();
        organizationId = page.getLong("props.organizations.data[0].id");
    }

    @Test
    @Order(10)
    void organizationValidationErrorsAreFlashed() {
        authedInertia()
            .header("Referer", BASE + "/organizations/create")
            .contentType(ContentType.JSON)
            .body("{\"email\":\"not-an-email\"}")
            .when().post("/organizations")
            .then().statusCode(303)
            .and().header("Location", equalTo(BASE + "/organizations/create"));

        authedInertia().when().get("/organizations/create")
            .then().statusCode(200)
            .and().body("props.errors.name", equalTo("required"))
            .and().body("props.errors.email", equalTo("invalid"));
    }

    @Test
    @Order(11)
    void updateOrganizationRedirectsBack() {
        authedInertia()
            .header("Referer", BASE + "/organizations/" + organizationId + "/edit")
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Inertia Test Org Renamed\",\"city\":\"Toronto\"}")
            .when().put("/organizations/" + organizationId)
            .then().statusCode(303)
            .and().header("Location", equalTo(BASE + "/organizations/" + organizationId + "/edit"));

        authedInertia().when().get("/organizations/" + organizationId + "/edit")
            .then().statusCode(200)
            .and().body("props.success", equalTo("Organization updated."))
            .and().body("props.organization.name", equalTo("Inertia Test Org Renamed"))
            .and().body("props.organization.city", equalTo("Toronto"));
    }

    @Test
    @Order(12)
    void deleteOrganizationMovesItToTrashAndCanRestore() {
        authedInertia()
            .header("Referer", BASE + "/organizations/" + organizationId + "/edit")
            .when().delete("/organizations/" + organizationId)
            .then().statusCode(303);

        authedInertia().when().get("/organizations?trashed=only")
            .then().statusCode(200)
            .and().body("props.organizations.data.size()", equalTo(1))
            .and().body("props.organizations.data[0].id", equalTo((int) organizationId))
            .and().body("props.organizations.data[0].deleted_at", notNullValue());

        authedInertia().when().get("/organizations")
            .then().statusCode(200)
            .and().body("props.organizations.data.id", not(hasItem((int) organizationId)));

        authedInertia()
            .header("Referer", BASE + "/organizations/" + organizationId + "/edit")
            .when().put("/organizations/" + organizationId + "/restore")
            .then().statusCode(303);

        authedInertia().when().get("/organizations?trashed=only")
            .then().statusCode(200)
            .and().body("props.organizations.data.size()", equalTo(0));
    }

    @Test
    @Order(13)
    void editMissingOrganizationRedirectsWithError() {
        authedInertia().when().get("/organizations/999999/edit")
            .then().statusCode(302)
            .and().header("Location", equalTo("/organizations"));
    }

    // ──────────────────────────────────────────────
    // Contacts
    // ──────────────────────────────────────────────

    @Test
    @Order(14)
    void contactsArePaginatedAndSearchable() {
        authedInertia().when().get("/contacts")
            .then().statusCode(200)
            .and().body("component", equalTo("Contacts/Index"))
            .and().body("props.contacts.data.size()", equalTo(10))
            .and().body("props.contacts.data[0].name", notNullValue());

        authedInertia().when().get("/contacts?search=zzzznotfound")
            .then().statusCode(200)
            .and().body("props.contacts.data.size()", equalTo(0));
    }

    @Test
    @Order(15)
    void createContactWithOrganization() {
        var res = authedInertia()
            .header("Referer", BASE + "/contacts/create")
            .contentType(ContentType.JSON)
            .body("{\"first_name\":\"Jane\",\"last_name\":\"Doe\",\"email\":\"jane@test.dev\","
                + "\"organization_id\":" + organizationId + "}")
            .when().post("/contacts")
            .then().statusCode(303)
            .and().header("Location", equalTo("/contacts"))
            .extract();
        captureSession(res);

        var page = authedInertia().when().get("/contacts?search=Doe")
            .then().statusCode(200)
            .and().body("props.success", equalTo("Contact created."))
            .and().body("props.contacts.data[0].name", equalTo("Jane Doe"))
            .and().body("props.contacts.data[0].organization.name",
                equalTo("Inertia Test Org Renamed"))
            .extract().jsonPath();
        contactId = page.getLong("props.contacts.data[0].id");

        authedInertia().when()
            .get("/organizations/" + organizationId + "/edit")
            .then().statusCode(200)
            .and().body("props.organization.contacts[0].name", equalTo("Jane Doe"));
    }

    @Test
    @Order(16)
    void contactWithInvalidOrganizationFails() {
        authedInertia()
            .header("Referer", BASE + "/contacts/create")
            .contentType(ContentType.JSON)
            .body("{\"first_name\":\"Bad\",\"last_name\":\"Org\",\"organization_id\":999999}")
            .when().post("/contacts")
            .then().statusCode(303);

        authedInertia().when().get("/contacts/create")
            .then().statusCode(200)
            .and().body("props.errors.organization_id",
                equalTo("The selected organization is invalid."));
    }

    @Test
    @Order(17)
    void updateAndDeleteContact() {
        authedInertia()
            .header("Referer", BASE + "/contacts/" + contactId + "/edit")
            .contentType(ContentType.JSON)
            .body("{\"first_name\":\"Jane\",\"last_name\":\"Doe\",\"city\":\"Vancouver\"}")
            .when().put("/contacts/" + contactId)
            .then().statusCode(303);

        authedInertia().when().get("/contacts/" + contactId + "/edit")
            .then().statusCode(200)
            .and().body("props.contact.city", equalTo("Vancouver"));

        authedInertia()
            .header("Referer", BASE + "/contacts/" + contactId + "/edit")
            .when().delete("/contacts/" + contactId)
            .then().statusCode(303);

        authedInertia().when().get("/contacts?trashed=only")
            .then().statusCode(200)
            .and().body("props.contacts.data[0].id", equalTo((int) contactId));

        authedInertia()
            .header("Referer", BASE + "/contacts/" + contactId + "/edit")
            .when().put("/contacts/" + contactId + "/restore")
            .then().statusCode(303);

        authedInertia().when().get("/contacts?trashed=only")
            .then().statusCode(200)
            .and().body("props.contacts.data.size()", equalTo(0));
    }

    // ──────────────────────────────────────────────
    // Users
    // ──────────────────────────────────────────────

    @Test
    @Order(18)
    void usersIndexListsDemoUser() {
        authedInertia().when().get("/users")
            .then().statusCode(200)
            .and().body("component", equalTo("Users/Index"))
            .and().body("props.users.size()", equalTo(1))
            .and().body("props.users[0].email", equalTo("johndoe@example.com"))
            .and().body("props.users[0].owner", equalTo(true))
            .and().body("props.users[0].photo", nullValue());
    }

    @Test
    @Order(19)
    void createUserWithPhotoUpload() throws Exception {
        var png = pngBytes();
        var res = authedInertia()
            .header("Referer", BASE + "/users/create")
            .multiPart("first_name", "Jane")
            .multiPart("last_name", "Roe")
            .multiPart("email", "jane.roe@example.com")
            .multiPart("password", "secret123")
            .multiPart("owner", "false")
            .multiPart("photo", "avatar.png", png, "image/png")
            .when().post("/users")
            .then().statusCode(303)
            .and().header("Location", equalTo("/users"))
            .extract();
        captureSession(res);

        var page = authedInertia().when().get("/users")
            .then().statusCode(200)
            .and().body("props.success", equalTo("User created."))
            .and().body("props.users.size()", equalTo(2))
            .and().body("props.users.find { it.email == 'jane.roe@example.com' }.photo",
                notNullValue())
            .extract().jsonPath();
        photoUrl = page.getString(
            "props.users.find { it.email == 'jane.roe@example.com' }.photo");

        authed().when().get(photoUrl)
            .then().statusCode(200)
            .and().contentType("image/png")
            .and().header("Cache-Control", containsString("max-age"));
    }

    @Test
    @Order(20)
    void duplicateUserEmailIsRejected() {
        authedInertia()
            .header("Referer", BASE + "/users/create")
            .multiPart("first_name", "Dup")
            .multiPart("last_name", "User")
            .multiPart("email", "jane.roe@example.com")
            .multiPart("owner", "false")
            .when().post("/users")
            .then().statusCode(303);

        authedInertia().when().get("/users/create")
            .then().statusCode(200)
            .and().body("props.errors.email",
                equalTo("The email has already been taken."));
    }

    @Test
    @Order(21)
    void demoUserCannotBeModifiedOrDeleted() {
        var demoId = authedInertia().when().get("/users")
            .then().statusCode(200)
            .extract().jsonPath().getLong("props.users[0].id");

        authedInertia()
            .header("Referer", BASE + "/users/" + demoId + "/edit")
            .multiPart("first_name", "Hacked")
            .multiPart("last_name", "User")
            .multiPart("email", "johndoe@example.com")
            .multiPart("owner", "true")
            .when().post("/users/" + demoId)
            .then().statusCode(303);

        authedInertia().when().get("/users/" + demoId + "/edit")
            .then().statusCode(200)
            .and().body("props.error",
                equalTo("Updating the demo user is not allowed."))
            .and().body("props.user.first_name", equalTo("John"));

        authedInertia()
            .header("Referer", BASE + "/users/" + demoId + "/edit")
            .when().delete("/users/" + demoId)
            .then().statusCode(303);

        authedInertia().when().get("/users/" + demoId + "/edit")
            .then().statusCode(200)
            .and().body("props.error",
                equalTo("Deleting the demo user is not allowed."));
    }

    @Test
    @Order(22)
    void userCanBeDeletedAndRestored() {
        var userId = authedInertia().when().get("/users")
            .then().statusCode(200)
            .extract().jsonPath()
            .getLong("props.users.find { it.email == 'jane.roe@example.com' }.id");

        authedInertia()
            .header("Referer", BASE + "/users/" + userId + "/edit")
            .when().delete("/users/" + userId)
            .then().statusCode(303);

        authedInertia().when().get("/users?trashed=only")
            .then().statusCode(200)
            .and().body("props.users.size()", equalTo(1))
            .and().body("props.users[0].id", equalTo((int) userId));

        authedInertia()
            .header("Referer", BASE + "/users/" + userId + "/edit")
            .when().put("/users/" + userId + "/restore")
            .then().statusCode(303);

        authedInertia().when().get("/users?trashed=only")
            .then().statusCode(200)
            .and().body("props.users.size()", equalTo(0));
    }

    // ──────────────────────────────────────────────
    // Reports, images, logout
    // ──────────────────────────────────────────────

    @Test
    @Order(23)
    void reportsPageRenders() {
        authedInertia().when().get("/reports")
            .then().statusCode(200)
            .and().body("component", equalTo("Reports/Index"));
    }

    @Test
    @Order(24)
    void imageRequestsAreServedOrRejectedSafely() {
        authed().when().get("/img/missing.png")
            .then().statusCode(404);

        authed().when().get("/img/../application.properties")
            .then().statusCode(404);

        authed().when().get(photoUrl)
            .then().statusCode(200)
            .and().contentType("image/png");
    }

    @Test
    @Order(25)
    void logoutClearsSession() {
        var res = authedInertia()
            .when().delete("/logout")
            .then().statusCode(303)
            .and().header("Location", equalTo("/login"))
            .extract();
        captureSession(res);

        authed().when().get("/")
            .then().statusCode(302)
            .and().header("Location", equalTo("/login"));
    }

    private static byte[] pngBytes() throws Exception {
        var image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawString("avatar", 5, 32);
        var output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(image, "png", output)).isTrue();
        return output.toByteArray();
    }
}
