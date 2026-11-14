package ${package};

import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * End-to-end coverage of the starter CRUD through the real Inertia contract:
 * visits receive page objects, mutations answer 303 + Location, and
 * validation failures redirect back so the flashed errors and input appear
 * on the next visit.
 *
 * <p>CSRF stays enabled: the token is fetched from a first visit and replayed
 * as the {@code X-XSRF-TOKEN} header, exactly as the Inertia frontend does.</p>
 */
@QuarkusTest
class ContactsResourceTest {

    @Test
    void indexRendersTheHtmlShell() {
        given()
            .when().get("/contacts")
            .then()
                .statusCode(200)
                .body(containsString("id=\"app\""));
    }

    @Test
    void indexVisitReturnsTheContactsPageObject() {
        given()
            .header("X-Inertia", "true")
            .when().get("/contacts")
            .then()
                .statusCode(200)
                .header("X-Inertia", equalTo("true"))
                .body("component", equalTo("Contacts/Index"))
                .body("props.contacts[0].name", equalTo("Ada Lovelace"));
    }

    @Test
    void editVisitReturnsTheContactPageObject() {
        given()
            .header("X-Inertia", "true")
            .when().get("/contacts/1/edit")
            .then()
                .statusCode(200)
                .body("component", equalTo("Contacts/Edit"))
                .body("props.contact.name", equalTo("Ada Lovelace"));
    }

    @Test
    void storeCreatesContactAndFlashesSuccess() {
        var session = newSession();

        given()
            .cookies(session.cookies())
            .header("X-XSRF-TOKEN", session.token())
            .header("X-Inertia", "true")
            .formParam("name", "Linus Torvalds")
            .formParam("email", "linus@example.com")
            .formParam("phone", "+34 600 111 000")
            .redirects().follow(false)
            .when().post("/contacts")
            .then()
                .statusCode(303)
                .header("Location", "/contacts");

        given()
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .when().get("/contacts")
            .then()
                .statusCode(200)
                .body(containsString("linus@example.com"))
                .body("props.success", equalTo("Contacto creado con éxito"));
    }

    @Test
    void storeWithInvalidDataRedirectsBackWithErrors() {
        var session = newSession();

        given()
            .cookies(session.cookies())
            .header("X-XSRF-TOKEN", session.token())
            .header("X-Inertia", "true")
            .header("Referer", "/contacts/create")
            .formParam("name", " ")
            .formParam("email", "not-an-email")
            .formParam("phone", "12345678901234567890123")
            .redirects().follow(false)
            .when().post("/contacts")
            .then()
                .statusCode(303)
                .header("Location", "/contacts/create");

        given()
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .when().get("/contacts/create")
            .then()
                .statusCode(200)
                .body("props.errors.name", equalTo("El nombre es obligatorio"))
                .body("props.errors.email", equalTo("El email no es válido"))
                .body("props.errors.phone", equalTo("El teléfono no puede superar los 20 caracteres"))
                .body("props.input.email", equalTo("not-an-email"));
    }

    @Test
    void updateAndDeleteCompleteTheCrudCycle() {
        var session = newSession();

        given()
            .cookies(session.cookies())
            .header("X-XSRF-TOKEN", session.token())
            .header("X-Inertia", "true")
            .formParam("name", "Grace Field")
            .formParam("email", "grace.field@example.com")
            .formParam("phone", "")
            .redirects().follow(false)
            .when().post("/contacts")
            .then()
                .statusCode(303);

        var id = given()
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .when().get("/contacts")
            .then()
                .statusCode(200)
                .extract().jsonPath()
                .getInt("props.contacts.find { it.email == 'grace.field@example.com' }.id");

        given()
            .cookies(session.cookies())
            .header("X-XSRF-TOKEN", session.token())
            .header("X-Inertia", "true")
            .formParam("name", "Grace Fielding")
            .formParam("email", "grace.field@example.com")
            .formParam("phone", "+34 600 222 111")
            .redirects().follow(false)
            .when().put("/contacts/" + id)
            .then()
                .statusCode(303)
                .header("Location", "/contacts");

        given()
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .when().get("/contacts")
            .then()
                .statusCode(200)
                .body(containsString("Grace Fielding"))
                .body("props.success", equalTo("Contacto actualizado con éxito"));

        given()
            .cookies(session.cookies())
            .header("X-XSRF-TOKEN", session.token())
            .header("X-Inertia", "true")
            .redirects().follow(false)
            .when().delete("/contacts/" + id)
            .then()
                .statusCode(303)
                .header("Location", "/contacts");

        given()
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .when().get("/contacts")
            .then()
                .statusCode(200)
                .body(not(containsString("Grace Fielding")))
                .body("props.success", equalTo("Contacto eliminado con éxito"));
    }

    private record Session(Map<String, String> cookies, String token) {
    }

    private Session newSession() {
        var response = given()
            .when().get("/contacts")
            .then()
                .statusCode(200)
                .extract();
        return new Session(response.cookies(), response.cookie("XSRF-TOKEN"));
    }
}
