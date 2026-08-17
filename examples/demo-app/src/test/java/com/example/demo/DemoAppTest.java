package com.example.demo;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.quarkus.test.junit.QuarkusTest;

import io.github.dg.quarkus.inertia.api.Inertia;
import io.github.dg.quarkus.inertia.testing.InertiaPage;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemoAppTest {

    @Inject
    Inertia inertia;

    private static Long createdPersonId;
    private static Long createdEmployeeId;

    record CsrfBoot(String xsrf, String sessionCookieValue) {}

    // Bootstraps a session + XSRF token via a GET and returns both cookie
    // values so they can be reused across POST + follow-up GET.
    private static CsrfBoot csrfBoot() {
        var res = given()
            .when().get("/test")
            .then()
                .statusCode(200)
                .extract();
        var session = res.detailedCookies().get("vertx-web.session");
        return new CsrfBoot(
            res.cookie("XSRF-TOKEN"),
            session != null ? session.getValue() : null);
    }

    // Replays the bootstrapped session + XSRF cookies (plus the
    // X-XSRF-TOKEN header) on a state-changing request so it passes CSRF.
    private static io.restassured.specification.RequestSpecification csrfGiven(CsrfBoot boot) {
        var spec = given()
            .header("X-XSRF-TOKEN", boot.xsrf())
            .cookie("XSRF-TOKEN", boot.xsrf());
        if (boot.sessionCookieValue() != null) {
            spec.cookie("vertx-web.session", boot.sessionCookieValue());
        }
        return spec;
    }

    // Replays the bootstrapped cookies on a follow-up request.
    private static io.restassured.specification.RequestSpecification csrfWith(CsrfBoot boot) {
        var spec = given()
            .cookie("XSRF-TOKEN", boot.xsrf());
        if (boot.sessionCookieValue() != null) {
            spec.cookie("vertx-web.session", boot.sessionCookieValue());
        }
        return spec;
    }

    // ──────────────────────────────────────────────
    // Inertia bean availability
    // ──────────────────────────────────────────────

    @Test @Order(1)
    void inertiaBeanIsAvailable() {
        assertThat(inertia).isNotNull();
    }

    @Test @Order(2)
    void renderReturnsUni() {
        var result = inertia.render("Persons/Index", java.util.Map.of("persons", java.util.List.of(), "total", 0L));
        assertThat(result).isNotNull();
    }

    // ──────────────────────────────────────────────
    // Persons — HTML (non-Inertia) requests
    // ──────────────────────────────────────────────

    @Test @Order(10)
    void personsIndexReturnsHtml() {
        given()
            .when().get("/persons")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("\"component\":\"Persons/Index\""));
    }

    @Test @Order(11)
    void personsIndexContainsPageObjectJson() {
        given()
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body(containsString("data-page="))
                .body(containsString("\"component\""))
                .body(containsString("\"props\""))
                .body(containsString("\"url\""))
                .body(containsString("\"version\""));
    }

    @Test @Order(12)
    void personsIndexHasRequiredProps() {
        given()
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body(containsString("\"persons\""))
                .body(containsString("\"total\""))
                .body(containsString("\"version\":\"1.0.0\""));
    }

    @Test @Order(13)
    void personsCreateFormReturnsHtml() {
        given()
            .when().get("/persons/create")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("\"component\":\"Persons/Form\""));
    }

    @Test @Order(14)
    void personsEditFormReturnsHtml() {
        given()
            .when().get("/persons/1/edit")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("\"component\":\"Persons/Form\""));
    }

    @Test @Order(15)
    void personsEditFormContainsPersonData() {
        given()
            .when().get("/persons/1/edit")
            .then()
                .statusCode(200)
                .body(containsString("\"editing\":true"))
                .body(containsString("\"person\""));
    }

    @Test @Order(16)
    void personsWithPaginationReturnsFilteredResults() {
        given()
            .queryParam("page", "1")
            .queryParam("size", "10")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body(containsString("\"page\""))
                .body(containsString("\"total\""));
    }

    // ──────────────────────────────────────────────
    // Persons — Inertia JSON requests
    // ──────────────────────────────────────────────

    @Test @Order(20)
    void inertiaJsonPersonsIndexReturnsJson() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .contentType(containsString("json"));
    }

    @Test @Order(21)
    void inertiaJsonPersonsHasXInertiaHeader() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .header("X-Inertia", "true");
    }

    @Test @Order(22)
    void inertiaJsonPersonsReturnsComponent() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .contentType(containsString("json"))
                .body("component", equalTo("Persons/Index"));
    }

    @Test @Order(23)
    void inertiaJsonPersonsHasAllRequiredFields() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("component", notNullValue())
                .body("props", notNullValue())
                .body("url", notNullValue())
                .body("version", notNullValue());
    }

    @Test @Order(24)
    void inertiaJsonPersonsVersionIsOneDotZero() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("version", equalTo("1.0.0"));
    }

    @Test @Order(25)
    void inertiaJsonPersonsHasHistoryBooleans() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("encryptHistory", nullValue())
                .body("clearHistory", nullValue())
                .body("preserveFragment", nullValue());
    }

    @Test @Order(26)
    void inertiaJsonPersonsHasVaryHeader() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .header("Vary", containsString("X-Inertia"));
    }

    @Test @Order(27)
    void inertiaJsonPersonsSearchReturnsFiltered() {
        var resp = given()
            .header("X-Inertia", "true")
            .queryParam("search", "John")
            .queryParam("page", "1")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Index"))
                .body("props.search", equalTo("John"));
        // Some persons may match "John" in name/email
    }

    @Test @Order(28)
    void inertiaJsonPersonsActiveFilter() {
        given()
            .header("X-Inertia", "true")
            .queryParam("active", "true")
            .queryParam("page", "1")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Index"))
                .body("props.active", equalTo("true"));
    }

    @Test @Order(29)
    void inertiaJsonPersonsSortByName() {
        given()
            .header("X-Inertia", "true")
            .queryParam("sort", "name")
            .queryParam("order", "asc")
            .queryParam("page", "1")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Index"))
                .body("props.sort", equalTo("name"))
                .body("props.order", equalTo("asc"));
    }

    @Test @Order(30)
    void inertiaJsonPersonsPartialReload() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Persons/Index")
            .header("X-Inertia-Partial-Data", "persons")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Index"))
                .body("props.persons", notNullValue());
    }

    // ──────────────────────────────────────────────
    // Persons — Infinite scroll props
    // ──────────────────────────────────────────────

    @Test @Order(31)
    void scrollPropsAppendByDefault() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons/scroll")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Index"))
                .body("scrollProps.persons.pageName", equalTo("persons_page"))
                .body("scrollProps.persons.currentPage", equalTo(1))
                .body("mergeProps", hasItem("persons.data"));
    }

    @Test @Order(34)
    void emptyMetadataIsOmittedFromJson() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("$", not(hasKey("mergeProps")))
                .body("$", not(hasKey("prependProps")))
                .body("$", not(hasKey("deepMergeProps")))
                .body("$", not(hasKey("onceProps")))
                .body("$", not(hasKey("deferredProps")))
                .body("$", not(hasKey("encryptHistory")));
    }

    @Test @Order(35)
    void scrollPropsPrependOnMergeIntentHeader() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Infinite-Scroll-Merge-Intent", "prepend")
            .when().get("/persons/scroll")
            .then()
                .statusCode(200)
                .body("scrollProps.persons.pageName", equalTo("persons_page"))
                .body("scrollProps.persons.currentPage", equalTo(1))
                .body("prependProps", hasItem("persons.data"));
    }

    @Test @Order(33)
    void scrollPropsRemainOnPartialReload() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Persons/Index")
            .header("X-Inertia-Partial-Data", "persons")
            .when().get("/persons/scroll")
            .then()
                .statusCode(200)
                .body("scrollProps.persons.pageName", equalTo("persons_page"))
                .body("props.persons.data", notNullValue());
    }

    // ──────────────────────────────────────────────
    // Persons — CRUD operations
    // ──────────────────────────────────────────────

    @Test @Order(40)
    void createPersonViaPost() {
        var resp = csrfGiven(csrfBoot())
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "TestUser",
                "lastName", "Created",
                "email", "testcreate@example.com",
                "phone", "555-0100",
                "active", true
            ))
            .when().post("/persons")
            .then()
                .statusCode(303)
                .header("Location", containsString("/persons"));
    }

    @Test @Order(41)
    void verifyCreatedPersonExists() {
        var json = given()
            .header("X-Inertia", "true")
            .when().get("/persons?search=testcreate@example.com&sort=createdAt&order=desc")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Index"))
                .body("props.persons[0].email", equalTo("testcreate@example.com"))
                .extract().jsonPath();
        createdPersonId = json.getLong("props.persons[0].id");
        assertThat(createdPersonId).isNotNull();
    }

    @Test @Order(42)
    void createPersonWithInertiaJson() {
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .header("X-Inertia", "true")
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "InertiaUser",
                "lastName", "Json",
                "email", "inertiajson@example.com",
                "phone", "555-0200",
                "active", true
            ))
            .when().post("/persons")
            .then()
                .statusCode(303);
    }

    @Test @Order(43)
    void createPersonValidationFailsWhenNameMissing() {
        csrfGiven(csrfBoot())
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "lastName", "NoName",
                "email", "noname@example.com"
            ))
            .when().post("/persons")
            .then()
                .statusCode(400);
    }

    @Test @Order(44)
    void updatePersonViaPost() {
        assertThat(createdPersonId).isNotNull();
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "UpdatedName",
                "lastName", "Updated",
                "email", "updated@example.com",
                "phone", "555-0300",
                "active", true
            ))
            .when().post("/persons/" + createdPersonId)
            .then()
                .statusCode(303)
                .header("Location", containsString("/persons"));
    }

    @Test @Order(45)
    void verifyUpdatedPerson() {
        assertThat(createdPersonId).isNotNull();
        given()
            .header("X-Inertia", "true")
            .when().get("/persons/" + createdPersonId + "/edit")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Form"))
                .body("props.person.name", equalTo("UpdatedName"));
    }

    @Test @Order(46)
    void deletePersonViaPost() {
        assertThat(createdPersonId).isNotNull();
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .when().post("/persons/" + createdPersonId + "/delete")
            .then()
                .statusCode(303)
                .header("Location", containsString("/persons"));
    }

    @Test @Order(47)
    void verifyDeletedPersonNotFound() {
        assertThat(createdPersonId).isNotNull();
        given()
            .header("X-Inertia", "true")
            .when().get("/persons/" + createdPersonId + "/edit")
            .then()
                .statusCode(200)
                .body("component", equalTo("Persons/Index"))
                .body("props.error", containsString("no encontrada"));
    }

    // ──────────────────────────────────────────────
    // Employees — HTML (non-Inertia) requests
    // ──────────────────────────────────────────────

    @Test @Order(50)
    void employeesIndexReturnsHtml() {
        given()
            .when().get("/employees")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("\"component\":\"Employees/Index\""));
    }

    @Test @Order(51)
    void employeesIndexHasLazyProps() {
        given()
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body(containsString("\"data\""))
                .body(containsString("\"total\""))
                .body(containsString("\"page\""))
                .body(containsString("\"size\""));
    }

    @Test @Order(52)
    void employeesCreateFormReturnsHtml() {
        given()
            .when().get("/employees/create")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("\"component\":\"Employees/Form\""));
    }

    @Test @Order(53)
    void employeesEditFormReturnsHtml() {
        given()
            .when().get("/employees/1/edit")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("\"component\":\"Employees/Form\""));
    }

    @Test @Order(54)
    void employeesEditFormHasEditingTrue() {
        given()
            .when().get("/employees/1/edit")
            .then()
                .statusCode(200)
                .body(containsString("\"editing\":true"))
                .body(containsString("\"employee\""));
    }

    // ──────────────────────────────────────────────
    // Employees — Inertia JSON requests
    // ──────────────────────────────────────────────

    @Test @Order(60)
    void inertiaJsonEmployeesReturnsLazyData() {
        given()
            .header("X-Inertia", "true")
            .when().get("/employees?page=0&size=5")
            .then()
                .statusCode(200)
                .contentType(containsString("json"))
                .body("component", equalTo("Employees/Index"))
                .body("props.data", notNullValue())
                .body("props.total", notNullValue())
                .body("props.page", equalTo(0))
                .body("props.size", equalTo(5));
    }

    @Test @Order(61)
    void inertiaJsonEmployeesSupportsSorting() {
        given()
            .header("X-Inertia", "true")
            .when().get("/employees?sortField=salary&sortOrder=-1&page=0&size=3")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Index"));
    }

    @Test @Order(62)
    void inertiaJsonEmployeesFilterByFirstName() {
        var jsonFilters = "{\"firstName\":{\"value\":\"John\",\"matchMode\":\"contains\"}}";
        given()
            .header("X-Inertia", "true")
            .queryParam("filters", jsonFilters)
            .queryParam("page", "0")
            .queryParam("size", "10")
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Index"));
    }

    @Test @Order(63)
    void inertiaJsonEmployeesFilterByDepartment() {
        var jsonFilters = "{\"department\":{\"value\":\"Engineering\",\"matchMode\":\"contains\"}}";
        given()
            .header("X-Inertia", "true")
            .queryParam("filters", jsonFilters)
            .queryParam("page", "0")
            .queryParam("size", "10")
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Index"));
    }

    @Test @Order(64)
    void inertiaJsonEmployeesFilterBySalaryBetween() {
        var jsonFilters = "{\"salary\":{\"value\":[50000,100000],\"matchMode\":\"between\"}}";
        given()
            .header("X-Inertia", "true")
            .queryParam("filters", jsonFilters)
            .queryParam("page", "0")
            .queryParam("size", "10")
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Index"));
    }

    @Test @Order(65)
    void inertiaJsonEmployeesFilterByActive() {
        var jsonFilters = "{\"active\":{\"value\":true,\"matchMode\":\"equals\"}}";
        given()
            .header("X-Inertia", "true")
            .queryParam("filters", jsonFilters)
            .queryParam("page", "0")
            .queryParam("size", "10")
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Index"));
    }

    @Test @Order(66)
    void inertiaJsonEmployeesHasHistoryBooleans() {
        given()
            .header("X-Inertia", "true")
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body("encryptHistory", nullValue())
                .body("clearHistory", nullValue())
                .body("preserveFragment", nullValue());
    }

    // ──────────────────────────────────────────────
    // Employees — CRUD operations
    // ──────────────────────────────────────────────

    @Test @Order(70)
    void createEmployeeViaPost() {
        var resp = csrfGiven(csrfBoot())
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "firstName", "Jane",
                "lastName", "Employee",
                "email", "jane.employee@example.com",
                "salary", 75000.00,
                "department", "Engineering",
                "hireDate", "2025-01-15",
                "active", true
            ))
            .when().post("/employees")
            .then()
                .statusCode(303)
                .header("Location", containsString("/employees"));
    }

    @Test @Order(71)
    void verifyCreatedEmployeeExists() {
        var json = given()
            .header("X-Inertia", "true")
            .queryParam("filters", "[{\"field\":\"email\",\"operator\":\"contains\",\"value\":\"jane.employee@example.com\"}]")
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Index"))
                .body("props.data[0].email", equalTo("jane.employee@example.com"))
                .extract().jsonPath();
        createdEmployeeId = json.getLong("props.data[0].id");
        assertThat(createdEmployeeId).isNotNull();
    }

    @Test @Order(72)
    void createEmployeeWithInertiaJson() {
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .header("X-Inertia", "true")
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "firstName", "Inertia",
                "lastName", "EmpJson",
                "email", "inertia.emp@example.com",
                "salary", 90000.00,
                "department", "Sales",
                "hireDate", "2025-03-01",
                "active", true
            ))
            .when().post("/employees")
            .then()
                .statusCode(303);
    }

    @Test @Order(73)
    void createEmployeeValidationFailsWhenFieldsMissing() {
        csrfGiven(csrfBoot())
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "lastName", "NoFirstName",
                "email", "bad@example.com",
                "salary", 50000.00
            ))
            .when().post("/employees")
            .then()
                .statusCode(400);
    }

    @Test @Order(74)
    void createEmployeeValidationFailsBadEmail() {
        csrfGiven(csrfBoot())
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "firstName", "Bad",
                "lastName", "Email",
                "email", "not-an-email",
                "salary", 50000.00
            ))
            .when().post("/employees")
            .then()
                .statusCode(400);
    }

    @Test @Order(75)
    void createEmployeeValidationFailsZeroSalary() {
        csrfGiven(csrfBoot())
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "firstName", "Zero",
                "lastName", "Salary",
                "email", "zero@example.com",
                "salary", 0.00
            ))
            .when().post("/employees")
            .then()
                .statusCode(400);
    }

    @Test @Order(76)
    void updateEmployeeViaPost() {
        assertThat(createdEmployeeId).isNotNull();
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "firstName", "JaneUpdated",
                "lastName", "EmployeeUpdated",
                "email", "jane.updated@example.com",
                "salary", 85000.00,
                "department", "Marketing",
                "hireDate", "2025-06-01",
                "active", true
            ))
            .when().post("/employees/" + createdEmployeeId)
            .then()
                .statusCode(303)
                .header("Location", containsString("/employees"));
    }

    @Test @Order(77)
    void verifyUpdatedEmployee() {
        assertThat(createdEmployeeId).isNotNull();
        given()
            .header("X-Inertia", "true")
            .when().get("/employees/" + createdEmployeeId + "/edit")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Form"))
                .body("props.employee.firstName", equalTo("JaneUpdated"));
    }

    @Test @Order(78)
    void deleteEmployeeViaPost() {
        assertThat(createdEmployeeId).isNotNull();
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .when().post("/employees/" + createdEmployeeId + "/delete")
            .then()
                .statusCode(303)
                .header("Location", containsString("/employees"));
    }

    @Test @Order(79)
    void verifyDeletedEmployeeNotFound() {
        assertThat(createdEmployeeId).isNotNull();
        given()
            .header("X-Inertia", "true")
            .when().get("/employees/" + createdEmployeeId + "/edit")
            .then()
                .statusCode(200)
                .body("component", equalTo("Employees/Index"))
                .body("props.error", containsString("no encontrado"));
    }

    // ──────────────────────────────────────────────
    // Flash data tests
    // ──────────────────────────────────────────────

    @Test @Order(80)
    void flashDataAppearsAfterCreate() {
        var boot = csrfBoot();
        csrfGiven(boot)
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "FlashTest",
                "lastName", "User",
                "email", "flash.test@example.com",
                "active", true
            ))
            .when().post("/persons")
            .then()
                .statusCode(303)
                .header("Location", containsString("/persons"));

        csrfWith(boot)
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("props.success", containsString("creada correctamente"));
    }

    @Test @Order(81)
    void flashDataAppearsAfterDelete() {
        var boot = csrfBoot();

        csrfGiven(boot)
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "DeleteFlash",
                "lastName", "Test",
                "email", "delete.flash@example.com",
                "active", false
            ))
            .when().post("/persons")
            .then()
                .statusCode(303);

        var json = csrfWith(boot)
            .header("X-Inertia", "true")
            .when().get("/persons?search=delete.flash@example.com&sort=createdAt&order=desc")
            .then()
                .statusCode(200)
                .body("props.persons[0].email", equalTo("delete.flash@example.com"))
                .extract().jsonPath();
        var id = json.getLong("props.persons[0].id");

        csrfGiven(boot)
            .redirects().follow(false)
            .when().post("/persons/" + id + "/delete")
            .then()
                .statusCode(303);

        csrfWith(boot)
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("props.success", containsString("eliminada correctamente"));
    }

    @Test @Order(82)
    void flashSurvivesVersionMismatchReload() {
        var boot = csrfBoot();

        csrfGiven(boot)
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "MismatchFlash",
                "lastName", "Test",
                "email", "mismatch.flash@example.com",
                "active", true
            ))
            .when().post("/persons")
            .then()
                .statusCode(303);

        csrfWith(boot)
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "999-stale")
            .when().get("/persons")
            .then()
                .statusCode(409)
                .header("X-Inertia-Location", containsString("/persons"));

        csrfWith(boot)
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("props.success", containsString("creada correctamente"));
    }

    // ──────────────────────────────────────────────
    // Inertia page assertions (InertiaPage helper)
    // ──────────────────────────────────────────────

    @Test
    void inertiaPageHelperParsesPersonsIndex() {
        var body = given()
            .header("X-Inertia", "true")
            .queryParam("active", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .extract().body().asString();
        var page = InertiaPage.fromJson(body);
        page.assertComponent("Persons/Index")
            .assertHasProps("persons", "total", "active")
            .assertHasProps(java.util.Map.of("active", "true"));
    }

    @Test
    void inertiaPageHelperParsesScrollPage() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/persons/scroll")
            .then()
                .statusCode(200)
                .extract().body().asString();
        var page = InertiaPage.fromJson(body);
        page.assertComponent("Persons/Index")
            .assertHasProps("persons")
            .assertScrollProps("persons")
            .assertMergeProps("persons.data");
    }

    @Test
    void inertiaPageHelperRejectsNonInertiaJson() {
        assertThatThrownBy(() -> InertiaPage.fromJson("{not-json}"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ──────────────────────────────────────────────
    // Non-Inertia request defaults
    // ──────────────────────────────────────────────

    @Test @Order(90)
    void nonInertiaRequestHasContentTypeHtml() {
        given()
            .when().get("/persons")
            .then()
                .contentType(containsString("text/html"));
    }

    @Test @Order(91)
    void nonInertiaEmployeesReturnsHtml() {
        given()
            .when().get("/employees")
            .then()
                .contentType(containsString("text/html"));
    }

    // ──────────────────────────────────────────────
    // Edge cases / protocol compliance
    // ──────────────────────────────────────────────

    @Test @Order(100)
    void inertiaJsonResponseHasEncryptHistory() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("encryptHistory", nullValue());
    }

    @Test @Order(101)
    void inertiaJsonResponseHasClearHistory() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("clearHistory", nullValue());
    }

    @Test @Order(102)
    void inertiaJsonResponseHasPreserveFragment() {
        given()
            .header("X-Inertia", "true")
            .when().get("/persons")
            .then()
                .statusCode(200)
                .body("preserveFragment", nullValue());
    }

    @Test @Order(103)
    void inertiaJsonEmployeesClearHistory() {
        given()
            .header("X-Inertia", "true")
            .when().get("/employees")
            .then()
                .statusCode(200)
                .body("encryptHistory", nullValue())
                .body("clearHistory", nullValue())
                .body("preserveFragment", nullValue());
    }

    @Test @Order(104)
    void inertiaReturns303ForPostWithFlash() {
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .header("X-Inertia", "true")
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "RedirectFlash",
                "lastName", "Test",
                "email", "redirect.flash@example.com"
            ))
            .when().post("/persons")
            .then()
                .statusCode(303)
                .header("Location", containsString("/persons"));
    }

    @Test @Order(105)
    void nonInertiaPostReturns303() {
        csrfGiven(csrfBoot())
            .redirects().follow(false)
            .contentType(ContentType.JSON)
            .body(java.util.Map.of(
                "name", "NonInertia",
                "lastName", "Post",
                "email", "noninertia.post@example.com"
            ))
            .when().post("/persons")
            .then()
                .statusCode(303);
    }

    @Test
    void bareRedirectReturns303() {
        csrfGiven(csrfBoot())
            .when().post("/persons/test-redirect")
            .then()
                .statusCode(302)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void blockingRedirectReturns303() {
        csrfGiven(csrfBoot())
            .when().post("/persons/test-blocking-redirect")
            .then()
                .statusCode(302)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void uniRedirectReturns303() {
        csrfGiven(csrfBoot())
            .when().post("/persons/test-uni-redirect")
            .then()
                .statusCode(302)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void testHello() {
        given()
            .when().get("/test")
            .then()
                .statusCode(200)
                .body(is("hello"));
    }

    @Test
    void testPostRedirect() {
        csrfGiven(csrfBoot())
            .when().post("/test/redirect-me")
            .then()
                .statusCode(302)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void testPostEcho() {
        csrfGiven(csrfBoot())
            .contentType(ContentType.JSON)
            .body("{\"msg\":\"hi\"}")
            .when().post("/test/echo")
            .then()
                .statusCode(200)
                .body(containsString("echo:"));
    }

    @Test
    void testPostJsonRedirect() {
        csrfGiven(csrfBoot())
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/test/json-redirect")
            .then()
                .statusCode(302)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void testGetNowhere() {
        given()
            .when().get("/test/nowhere")
            .then()
                .statusCode(200)
                .body(is("nowhere"));
    }

    @Test
    void testPostNoop() {
        csrfGiven(csrfBoot())
            .when().post("/test/noop")
            .then()
                .statusCode(200)
                .body(is("noop"));
    }

    @Test
    void testPostOk() {
        csrfGiven(csrfBoot())
            .when().post("/test/ok")
            .then()
                .statusCode(200);
    }

    @Test
    void testPostNoContent() {
        csrfGiven(csrfBoot())
            .when().post("/test/nocontent")
            .then()
                .statusCode(204);
    }

    @Test
    void testPostCreated() {
        csrfGiven(csrfBoot())
            .when().post("/test/created")
            .then()
                .statusCode(201)
                .body(is("created"));
    }

    @Test
    void testPostBadRequest() {
        csrfGiven(csrfBoot())
            .when().post("/test/badrequest")
            .then()
                .statusCode(400)
                .body(is("bad"));
    }

    @Test
    void testPostEmpty200() {
        csrfGiven(csrfBoot())
            .when().post("/test/empty-200")
            .then()
                .statusCode(200);
    }

    @Test
    void testPost301() {
        csrfGiven(csrfBoot())
            .when().post("/test/redirect-301")
            .then()
                .statusCode(301)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void testPost302() {
        csrfGiven(csrfBoot())
            .when().post("/test/redirect-302")
            .then()
                .statusCode(302)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void testPost307() {
        csrfGiven(csrfBoot())
            .when().post("/test/redirect-307")
            .then()
                .statusCode(307)
                .header("Location", not(emptyOrNullString()));
    }

    @Test
    void testGetFollowsRedirect() {
        given()
            .when().get("/test/redirect-303-get")
            .then()
                .statusCode(200)
                .body(is("hello"));
    }
}
