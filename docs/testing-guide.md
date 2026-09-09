# Testing Guide — Inertia.js Java Adapters

This guide explains how to write tests for Inertia.js responses in both the **Spring Boot** and **Quarkus** adapters.

---

## Table of Contents

1. [Spring Boot — MockMvc](#spring-boot--mockmvc)
2. [Quarkus — REST-assured + InertiaPage](#quarkus--rest-assured--inertiapage)
3. [Common Patterns](#common-patterns)

---

## Spring Boot — MockMvc

### Setup

```xml
<dependency>
    <groupId>io.github.diovamny</groupId>
    <artifactId>spring-inertia</artifactId>
</dependency>
```

```java
@SpringBootTest
@AutoConfigureMockMvc
class MyInertiaTest {
    @Autowired MockMvc mvc;
}
```

### InertiaResultMatchers

`InertiaResultMatchers` provides a fluent DSL for asserting Inertia responses on `MockMvc` `ResultActions`.

```java
import io.github.diovamny.spring.inertia.testing.InertiaResultMatchers;

result.andExpect(InertiaResultMatchers.inertia().component("Dashboard"))
      .andExpect(InertiaResultMatchers.inertia().hasProp("user.name", "Alice"))
      .andExpect(InertiaResultMatchers.inertia().propEquals("count", 42))
      .andExpect(InertiaResultMatchers.inertia().url("/dashboard"))
      .andExpect(InertiaResultMatchers.inertia().encryptHistory())
      .andExpect(InertiaResultMatchers.inertia().clearHistory())
      .andExpect(InertiaResultMatchers.inertia().preserveFragment())
      .andExpect(InertiaResultMatchers.inertia().deferredProp("heavyData"))
      .andExpect(InertiaResultMatchers.inertia().rescuedProps("user"));
```

| Matcher | Description |
|---------|-------------|
| `component(name)` | Asserts the rendered component name |
| `url(path)` | Asserts the canonical URL |
| `hasProp(key)` | Asserts a top-level prop key exists |
| `hasProp(dotPath, value)` | Asserts a nested prop value using dot notation |
| `hasProps(keys...)` | Asserts multiple prop keys exist |
| `propEquals(key, value)` | Asserts prop equals a specific value |
| `encryptHistory()` | Asserts `encryptHistory=true` |
| `clearHistory()` | Asserts `clearHistory=true` |
| `preserveFragment()` | Asserts `preserveFragment=true` |
| `deferredProp(key)` | Asserts a prop is in the deferred group |
| `rescuedProps(keys...)` | Asserts rescued props |
| `version(v)` | Asserts the asset version string |

### InertiaPage (Spring)

For richer assertions, parse the response into an `InertiaPage`:

```java
import io.github.diovamny.spring.inertia.testing.InertiaPage;

InertiaPage page = InertiaPage.from(mvcResult);

page.assertComponent("Home")
    .assertProp("title", "Welcome")
    .assertPropExists("user")
    .assertUrl("/home")
    .assertEncryptHistory(false)
    .assertClearHistory(false)
    .assertPreserveFragment(false)
    .dump();
```

| Method | Description |
|--------|-------------|
| `from(mockMvc, url)` | Executes initial Inertia GET and returns page with reload capabilities |
| `from(mockMvc, result)` | Wraps MvcResult and attaches MockMvc for reload capabilities |
| `withClient(mockMvc)` | Attaches MockMvc to enable reload operations |
| `reloadOnly(props...)` | Actively reloads requesting only specified props |
| `reloadExcept(props...)` | Actively reloads excluding specified props |
| `loadDeferredProps(group?, callback?)` | Actively reloads deferred props (all or by group) |
| `assertComponent(name)` | Asserts component name (returns `this`) |
| `assertUrl(url)` | Asserts URL (returns `this`) |
| `assertProp(key, value)` | Asserts prop value, supports dot notation |
| `assertPropExists(key)` | Asserts prop key is present |
| `assertPropAbsent(key)` | Asserts prop key is absent |
| `assertMissing(key)` | Alias for `assertNoProp` / `assertPropAbsent` (Laravel parity) |
| `assertPropCount(key, count)` | Asserts element count of Collection, Map or Array prop |
| `assertPropMap(key, callback)` | Validates nested Map props with consumer assertions |
| `assertEncryptHistory(bool)` | Asserts encryptHistory flag |
| `assertClearHistory(bool)` | Asserts clearHistory flag |
| `assertPreserveFragment(bool)` | Asserts preserveFragment flag |
| `assertRescuedProps(keys...)` | Asserts the rescued props set |
| `dump()` | Prints full page JSON to stdout |
| `getProps()` | Returns raw `Map<String, Object>` |
| `getComponent()` | Returns component name string |

### Spring Example Tests

```java
@Test
void dashboardRendersUserProps() throws Exception {
    mvc.perform(get("/dashboard").header("X-Inertia", "true"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Inertia", "true"))
        .andExpect(InertiaResultMatchers.inertia().component("Dashboard"))
        .andExpect(InertiaResultMatchers.inertia().hasProp("user.name", "Alice"));
}

@Test
void partialReloadOnlyFetchesRequestedProps() throws Exception {
    mvc.perform(get("/dashboard")
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Dashboard")
            .header("X-Inertia-Partial-Data", "user"))
        .andExpect(status().isOk())
        .andExpect(InertiaResultMatchers.inertia().hasProp("user"))
        .andExpect(InertiaResultMatchers.inertia().component("Dashboard"));
}

@Test
void versionMismatchReturns409() throws Exception {
    mvc.perform(get("/page")
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "old-version"))
        .andExpect(status().isConflict())
        .andExpect(header().exists("X-Inertia-Location"));
}
```

---

## Quarkus — REST-assured + InertiaPage

### Setup

```java
@QuarkusTest
class MyInertiaTest { }
```

### InertiaPage (Quarkus)

```java
import io.github.diovamny.quarkus.inertia.testing.InertiaPage;

InertiaPage page = InertiaPage.from(
    given()
        .header("X-Inertia", "true")
        .when().get("/dashboard")
        .then().statusCode(200)
        .extract().response()
);

page.assertComponent("Dashboard")
    .assertProp("user.name", "Alice")
    .assertPropExists("notifications")
    .assertUrl("/dashboard")
    .assertEncryptHistory(false)
    .dump();
```

| Method | Description |
|--------|-------------|
| `from(Response)` | Static factory, parses the Inertia JSON body |
| `withExecutor(executor)` | Attaches an `InertiaReloadExecutor` for active reloads |
| `reloadOnly(props...)` | Actively reloads requesting only specified props |
| `reloadExcept(props...)` | Actively reloads excluding specified props |
| `loadDeferredProps(group?, callback?)` | Actively reloads deferred props (all or by group) |
| `assertComponent(name)` | Asserts component name (fluent) |
| `assertUrl(url)` | Asserts URL field |
| `assertProp(dotPath, value)` | Asserts prop value, supports dot notation |
| `assertPropExists(key)` | Asserts prop key is present |
| `assertNoProp(key)` / `assertMissing(key)` | Asserts prop key is absent |
| `assertPropCount(key, count)` | Asserts element count of Collection, Map or Array prop |
| `assertPropMap(key, callback)` | Validates nested Map props with consumer assertions |
| `assertEncryptHistory(bool)` | Asserts encryptHistory flag |
| `assertClearHistory(bool)` | Asserts clearHistory flag |
| `assertPreserveFragment(bool)` | Asserts preserveFragment flag |
| `assertDeferredProps(props...)` | Asserts deferred props exist |
| `assertDeferredPropsInGroup(group, props...)` | Asserts deferred group membership |
| `assertRescuedProps(keys...)` | Asserts rescued props set |
| `dump()` | Prints full page JSON to stdout |

### Quarkus Example Tests

```java
@Test
void dashboardRendersProps() {
    InertiaPage page = InertiaPage.from(
        given()
            .header("X-Inertia", "true")
            .when().get("/dashboard")
            .then().statusCode(200)
            .extract().response()
    );
    page.assertComponent("Dashboard")
        .assertProp("user.name", "Alice");
}

@Test
void versionMismatchReturns409() {
    given()
        .header("X-Inertia", "true")
        .header("X-Inertia-Version", "stale")
        .when().get("/page")
        .then()
        .statusCode(409)
        .header("X-Inertia-Location", notNullValue());
}
```

---

## Common Patterns

### Testing Shared Data

```java
// Spring
result.andExpect(InertiaResultMatchers.inertia().hasProp("auth.user"));

// Quarkus
page.assertPropExists("auth")
    .assertProp("auth.user.email", "alice@example.com");
```

### Testing viewData (HTML only)

```java
// Spring — no X-Inertia header, returns full HTML
mvc.perform(get("/home"))
    .andExpect(content().string(containsString("My App Title")));

// Quarkus
given().when().get("/home")
    .then().body(containsString("My App Title"));
```

### Testing First Visit (Full HTML)

```java
// Spring
mvc.perform(get("/"))
    .andExpect(content().string(containsString("id=\"app\"")));

// Quarkus
given().when().get("/")
    .then().body(containsString("data-page="));
```

### Debugging with dump()

```java
page.assertComponent("Dashboard")
    .dump()    // prints JSON here for debugging
    .assertProp("user.name", "Alice");
```
