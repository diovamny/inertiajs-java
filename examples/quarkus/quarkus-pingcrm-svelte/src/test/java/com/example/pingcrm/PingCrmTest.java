package com.example.pingcrm;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PingCrmTest {

    @BeforeAll
    static void configure() {
        RestAssured.config = RestAssured.config()
                .redirect(RedirectConfig.redirectConfig().followRedirects(false));
    }

    private static String sessionCookie;

    private static RequestSpecification inertia() {
        return given()
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0");
    }

    private static RequestSpecification authed() {
        return given()
                .cookie("vertx-web.session", sessionCookie);
    }

    private static RequestSpecification authedInertia() {
        return authed()
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0");
    }

    private static void captureSession(ExtractableResponse<Response> res) {
        var cookie = res.detailedCookies().get("vertx-web.session");
        if (cookie != null) {
            sessionCookie = cookie.getValue();
        }
    }

    @Test
    @Order(1)
    void loginPage() {
        inertia()
        .when()
            .get("/login")
        .then()
            .statusCode(200)
            .body(containsString("Auth\\/Login"));
    }

    @Test
    @Order(2)
    void loginBadCredentials() {
        inertia()
            .contentType("application/json")
            .body("{\"email\":\"bad@example.com\",\"password\":\"wrong\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(303)
            .header("Location", "/login");
    }

    @Test
    @Order(3)
    void loginSuccess() {
        var res = inertia()
            .contentType("application/json")
            .body("{\"email\":\"johndoe@example.com\",\"password\":\"secret\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(303)
            .header("Location", "/")
            .extract();

        captureSession(res);
        assertThat(sessionCookie).isNotBlank();
    }

    @Test
    @Order(4)
    void unauthorizedRedirect() {
        inertia()
        .when()
            .get("/")
        .then()
            .statusCode(302)
            .header("Location", "/login");
    }

    @Test
    @Order(5)
    void unauthorizedInertia303() {
        inertia()
        .when()
            .delete("/logout")
        .then()
            .statusCode(303)
            .header("Location", "/login");
    }

    @Test
    @Order(6)
    void dashboardPage() {
        authedInertia()
        .when()
            .get("/")
        .then()
            .statusCode(200)
            .body(containsString("Dashboard/Index"));
    }

    @Test
    @Order(7)
    void organizationsIndex() {
        authedInertia()
        .when()
            .get("/organizations")
        .then()
            .statusCode(200)
            .body(containsString("Organizations/Index"));
    }

    @Test
    @Order(8)
    void contactsIndex() {
        authedInertia()
        .when()
            .get("/contacts")
        .then()
            .statusCode(200)
            .body(containsString("Contacts/Index"));
    }

    @Test
    @Order(9)
    void usersIndex() {
        authedInertia()
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body(containsString("Users/Index"));
    }

    @Test
    @Order(10)
    void reportsIndex() {
        authedInertia()
        .when()
            .get("/reports")
        .then()
            .statusCode(200)
            .body(containsString("Reports/Index"));
    }

    @Test
    @Order(11)
    void logout() {
        authedInertia()
        .when()
            .delete("/logout")
        .then()
            .statusCode(303)
            .header("Location", "/login");
    }

    @Test
    @Order(12)
    void createOrganization() {
        loginAgain();

        authedInertia()
            .contentType("application/json")
            .body("{\"name\":\"Test Org\",\"email\":\"test@org.com\",\"phone\":\"555-0100\"}")
        .when()
            .post("/organizations")
        .then()
            .statusCode(303)
            .header("Location", "/organizations");
    }

    @Test
    @Order(13)
    void editOrganization() {
        loginAgain();

        var id = authedInertia()
        .when()
            .get("/organizations")
        .then()
            .statusCode(200)
            .extract().path("props.organizations.data[0].id");

        authedInertia()
            .contentType("application/json")
            .body("{\"name\":\"Updated Org\",\"email\":\"updated@org.com\"}")
        .when()
            .put("/organizations/" + id)
        .then()
            .statusCode(303);
    }

    @Test
    @Order(14)
    void deleteOrganization() {
        loginAgain();

        var id = authedInertia()
        .when()
            .get("/organizations")
        .then()
            .statusCode(200)
            .extract().path("props.organizations.data[0].id");

        authedInertia()
        .when()
            .delete("/organizations/" + id)
        .then()
            .statusCode(303);
    }

    @Test
    @Order(15)
    void createContact() {
        loginAgain();

        authedInertia()
            .contentType("application/json")
            .body("{\"first_name\":\"Jane\",\"last_name\":\"Doe\",\"email\":\"jane@test.com\"}")
        .when()
            .post("/contacts")
        .then()
            .statusCode(303)
            .header("Location", "/contacts");
    }

    @Test
    @Order(16)
    void createUser() {
        loginAgain();

        authedInertia()
            .contentType("application/json")
            .body("{\"first_name\":\"Bob\",\"last_name\":\"Smith\",\"email\":\"bob@test.com\",\"password\":\"password123\"}")
        .when()
            .post("/users")
        .then()
            .statusCode(303)
            .header("Location", "/users");
    }

    @Test
    @Order(17)
    void duplicateEmail() {
        loginAgain();

        authedInertia()
            .contentType("application/json")
            .body("{\"first_name\":\"Another\",\"last_name\":\"Bob\",\"email\":\"bob@test.com\",\"password\":\"pass\"}")
        .when()
            .post("/users")
        .then()
            .statusCode(303);
    }

    @Test
    @Order(18)
    void demoUserProtection() {
        loginAgain();

        var demoId = authedInertia()
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .extract().path("props.users.find{it.email=='johndoe@example.com'}.id");

        authedInertia()
        .when()
            .delete("/users/" + demoId)
        .then()
            .statusCode(303);
    }

    @Test
    @Order(19)
    void organizationSearch() {
        loginAgain();

        authedInertia()
            .queryParam("search", "Test")
        .when()
            .get("/organizations")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(20)
    void contactSearch() {
        loginAgain();

        authedInertia()
            .queryParam("search", "Jane")
        .when()
            .get("/contacts")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(21)
    void userSearch() {
        loginAgain();

        authedInertia()
            .queryParam("search", "Bob")
        .when()
            .get("/users")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(22)
    void organizationPagination() {
        loginAgain();

        authedInertia()
            .queryParam("page", "2")
        .when()
            .get("/organizations")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(23)
    void restoreOrganization() {
        loginAgain();

        var id = authedInertia()
            .queryParam("trashed", "only")
        .when()
            .get("/organizations")
        .then()
            .statusCode(200)
            .extract().path("props.organizations.data[0]?.id");

        if (id != null) {
            authedInertia()
            .when()
                .put("/organizations/" + id + "/restore")
            .then()
                .statusCode(303);
        }
    }

    @Test
    @Order(24)
    void restoreContact() {
        loginAgain();

        var id = authedInertia()
            .queryParam("trashed", "only")
        .when()
            .get("/contacts")
        .then()
            .statusCode(200)
            .extract().path("props.contacts.data[0]?.id");

        if (id != null) {
            authedInertia()
            .when()
                .put("/contacts/" + id + "/restore")
            .then()
                .statusCode(303);
        }
    }

    @Test
    @Order(25)
    void restoreUser() {
        loginAgain();

        var id = authedInertia()
            .queryParam("trashed", "only")
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .extract().path("props.users.data.find{it.deleted_at != null}?.id");

        if (id != null) {
            authedInertia()
            .when()
                .put("/users/" + id + "/restore")
            .then()
                .statusCode(303);
        }
    }

    private void loginAgain() {
        var res = inertia()
            .contentType("application/json")
            .body("{\"email\":\"johndoe@example.com\",\"password\":\"secret\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(303)
            .extract();
        captureSession(res);
    }
}
