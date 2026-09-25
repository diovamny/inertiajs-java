# Testing Guide — Inertia.js Java Adapters

This guide explains how to write tests for Inertia.js responses in both the **Spring Boot** and **Quarkus** adapters.

---

## Table of Contents

1. [Spring Boot — MockMvc](#spring-boot--mockmvc)
2. [Quarkus — REST-assured + InertiaPage](#quarkus--rest-assured--inertiapage)
3. [Common Patterns](#common-patterns)
4. [End-to-End — Playwright](#end-to-end--playwright)

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

---

## End-to-End — Playwright

Browser contracts against the real demos with the official `@inertiajs/*`
clients (pinned `3.7.1`). Specs live in `e2e/` (`inertia-contracts`,
`pingcrm-contracts`, `feature-matrix`, `ssr-contracts`); each run targets
**one booted demo** selected with `E2E_BASE_URL`. This is the procedure the
`e2e` CI job follows, verified end-to-end on 2026-09-24 (118/118 green).

### 1. Package the demos (once)

```bash
./mvnw -B -DskipTests install -P archetypes   # adapters + archetypes to ~/.m2
./mvnw -B -DskipTests package -Pexamples \
  -pl examples/spring/spring-kitchen-sink,examples/spring/spring-pingcrm,\
examples/spring/spring-pingcrm-react,examples/spring/spring-pingcrm-svelte,\
examples/quarkus/kitchen-sink,examples/quarkus/pingcrm,\
examples/quarkus/pingcrm-react,examples/quarkus/quarkus-pingcrm-svelte
```

### 2. Boot one demo (CWD = module dir, H2 files resolve relative)

| Demo | Port | Health | Spec(s) |
|---|---|---|---|
| `examples/spring/spring-kitchen-sink` | 8080 | `/actuator/health` | `inertia-contracts.spec.ts` |
| `examples/quarkus/kitchen-sink` | 8081 | `/q/health` | `inertia-contracts.spec.ts` |
| `examples/spring/spring-pingcrm` | 8080 | `/actuator/health` | `pingcrm-contracts.spec.ts` |
| `examples/spring/spring-pingcrm-react` | 8080 | `/actuator/health` | `pingcrm-contracts` + `feature-matrix` |
| `examples/spring/spring-pingcrm-svelte` | 8181 | `/actuator/health` | `pingcrm-contracts` + `feature-matrix` |
| `examples/quarkus/pingcrm` | 8081 | `/q/health` | `pingcrm-contracts.spec.ts` |
| `examples/quarkus/pingcrm-react` | 8080 | `/q/health` | `pingcrm-contracts` + `feature-matrix` |
| `examples/quarkus/quarkus-pingcrm-svelte` | 8082 | `/q/health` | `pingcrm-contracts` + `feature-matrix` |

```bash
cd examples/spring/spring-pingcrm-react
java -jar target/spring-pingcrm-react-0.0.5.jar
# Quarkus over plain http needs:
#   QUARKUS_REST_CSRF_COOKIE_FORCE_SECURE=false java -jar target/quarkus-app/quarkus-run.jar
```

### 3. Run the specs (serial, warmed up)

```bash
cd e2e
E2E_BASE_URL=http://localhost:8080 npx playwright test --config playwright.config.ts \
  --reporter=line --workers=1 pingcrm-contracts.spec.ts feature-matrix.spec.ts
```

Run **serially (`--workers=1`) after a warm-up request** (one `GET /login`
+ one `GET /e2e-probe`): on cold/slow machines, parallel workers against a
freshly booted demo produce flakes (observed 2026-09-24: 7/11 cold-parallel
vs 11/11 warm-serial on the same demo, root cause confirmed as cold-start
contention, not product behavior). CI sets `workers: 1` via `$CI`.

### 4. SSR (E2E-09): generated starter + sidecar

```bash
# generate (archetype default appName is already "Hello Inertia")
./mvnw -B archetype:generate -DarchetypeCatalog=local \
  -DarchetypeGroupId=io.github.diovamny \
  -DarchetypeArtifactId=inertia-spring-vue-archetype -DarchetypeVersion=0.0.5 \
  -DgroupId=com.acme -DartifactId=hello-inertia -Dpackage=com.acme.hello \
  -DinertiaAdapterVersion=0.0.5 -DjavaVersion=21 -DframeworkVersion=4.1.0
cd hello-inertia/src/main/webui
npm install && npm run build && npm run build:ssr   # NOT `npm ci`: starters ship no lockfile
cd ../../.. && ./mvnw -B -DskipTests package
# terminal 1: node src/main/webui/ssr-server.mjs    # sidecar :13714
# terminal 2: INERTIA_SSR_ENABLED=true java -jar target/hello-inertia-*.jar
cd <repo>/e2e
E2E_SSR_URL=http://localhost:8080 npx playwright test --config playwright.config.ts \
  --reporter=line --workers=1 ssr-contracts.spec.ts -g "server-rendered|hydrates"
# stop the sidecar, keep the app, then:
E2E_SSR_URL=http://localhost:8080 npx playwright test --config playwright.config.ts \
  --reporter=line --workers=1 ssr-contracts.spec.ts -g "fallback"
```

Expected text markers: `Hello Inertia powered by` (visible even with
JavaScript disabled = server-rendered proof) and `Powered by Spring Boot`
(or `Quarkus`). The `fallback` case proves CSR still renders with the
sidecar down (SSR is never fatal).

### 5. SSR through Reactive Routes (E2E-09 reactive cells)

The SSR Java path (`SsrHandler` + sidecar contract) is transport-independent,
but the matrix proves it per transport. For a Quarkus starter, add a
throwaway `@Route` twin of the Welcome page (E2E fixture only, never part of
the archetype) and run the same suite with `E2E_SSR_PATH`:

```java
// RxWelcomeRoute.java (TEMP fixture in the generated starter;
// same package as WelcomeController, same Welcome props)
import java.util.Map;
import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.quarkus.vertx.web.Route;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class RxWelcomeRoute {
    @Inject
    Inertia inertia;

    @ConfigProperty(name = "app.name", defaultValue = "Hello Inertia")
    String appName;

    @Route(path = "/ssr-rx", methods = Route.HttpMethod.GET)
    @Blocking
    public Uni<Object> welcome() {
        return inertia.render("Welcome", Map.of(
            "appName", appName,
            "framework", "Quarkus",
            "frameworkVersion", "3.39.2",
            "inertiaUrl", "https://inertiajs.com/"));
    }
}
```

```bash
# + quarkus-reactive-routes dependency in the generated pom, then:
./mvnw -B -DskipTests package
# terminal 1: node src/main/webui/ssr-server.mjs
# terminal 2: INERTIA_SSR_ENABLED=true java -jar target/quarkus-app/quarkus-run.jar
cd <repo>/e2e
E2E_SSR_URL=http://localhost:8080 E2E_SSR_PATH=/ssr-rx npx playwright test \
  --config playwright.config.ts --reporter=line --workers=1 ssr-contracts.spec.ts \
  -g "server-rendered|hydrates"
# stop the sidecar, keep the app, then the fallback case with the same env
```

This proves render + hydration + fallback through `@Route`, closing the
reactive SSR cells with the same assertions as the REST ones.
