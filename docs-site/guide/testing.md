# Testing

Inertia applications are tested at three levels: the page object (cheap unit
assertions), the HTTP contract (MockMvc or RestAssured against the adapter) and
the browser (Playwright with the official client). The adapter ships fluent
assertions for the first two, and the TCK pins the wire contract for all
transports.

## What to assert

| Layer | Tool | Proves |
|---|---|---|
| Unit | Plain JUnit | Props, DTOs, version policy |
| Adapter integration | MockMvc / RestAssured + `InertiaPage` | Component, props, metadata, headers, status |
| Protocol | `InertiaTckRunner` | The 11 normative YAML suites |
| Browser | Playwright | Hydration, navigation, forms, SSR |

Do not test the same rule at every layer. Use integration tests for the wire
contract and reserve Playwright for behavior a JVM test cannot observe.

## Spring Boot: MockMvc

```java
@SpringBootTest
@AutoConfigureMockMvc
class DashboardTest {

    @Autowired MockMvc mvc;

    @Test
    void inertiaVisitReturnsPageObject() throws Exception {
        mvc.perform(get("/dashboard").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Inertia", "true"))
            .andExpect(InertiaResultMatchers.inertia().component("Dashboard"))
            .andExpect(InertiaResultMatchers.inertia()
                .prop("user.name", "Alice"))
            .andExpect(InertiaResultMatchers.inertia().url("/dashboard"));
    }
}
```

`InertiaResultMatchers.inertia()` returns a facade of `ResultMatcher`s. The
commonly used ones:

| Matcher | Asserts |
|---|---|
| `component(name)` | `component` field |
| `url(path)` | `url` field |
| `hasProp(name)` | Prop presence |
| `prop(name, value)` / `prop(name, matcher)` | Prop value or Hamcrest matcher |
| `hasProps(names...)` | Several props exist |
| `exactProps(map)` | Exact top-level prop map |
| `propCount(name, count)` | Collection/array/map size |
| `missing(name)` / `noProp(name)` | Prop absent |
| `version(v)` | Asset version |
| `flash(key, value)` | One flash entry |
| `hasDeferredProps(names...)` | Deferred keys |
| `hasDeferredGroup(group, names...)` | Group membership |
| `hasMergeProps(names...)` | Merge metadata |
| `hasPrependProps(names...)` | Prepend metadata |
| `hasDeepMergeProps(names...)` | Deep-merge metadata |
| `hasMatchPropsOn(fields...)` | Match fields |
| `hasOnceProps(names...)` | Once metadata |
| `hasScrollProps(keys...)` | Scroll metadata |
| `hasRescuedProps(keys...)` | Rescued keys |
| `encryptHistory(boolean)` | History flag |
| `clearHistory(boolean)` | History flag |
| `preserveFragment(boolean)` | Fragment flag |
| `meta(key, value)` | Page metadata |
| `dump()` / `dump(key)` | Print the page for debugging |

### Partial reloads

Always send the partial component header; without it the adapter treats the
request as a full visit:

```java
var result = mvc.perform(get("/dashboard")
        .header("X-Inertia", "true")
        .header("X-Inertia-Partial-Component", "Dashboard")
        .header("X-Inertia-Partial-Data", "user"))
    .andExpect(status().isOk())
    .andExpect(InertiaResultMatchers.inertia().hasProp("user"))
    .andReturn();

InertiaPage.from(result)
    .assertPropExists("user")
    .assertMissing("stats");
```

`InertiaPage.from(MvcResult)` parses the body and keeps the response available
for richer assertions. `InertiaPage.from(mvc, "/dashboard")` performs the
initial visit and attaches MockMvc so the page can reload itself:

```java
InertiaPage page = InertiaPage.from(mvc, "/dashboard");

page.assertComponent("Dashboard")
    .assertProp("user.name", "Alice")
    .reloadOnly("stats")
    .assertPropExists("stats")
    .assertPropAbsent("user");
```

| Method | Effect |
|---|---|
| `reloadOnly(props...)` | Sends a partial reload with `only` |
| `reloadExcept(props...)` | Sends a partial reload with `except` |
| `loadDeferredProps()` | Reloads every deferred prop |
| `loadDeferredProps(group)` | Reloads one deferred group |
| `assertPropAbsent(key)` / `assertMissing(key)` | Key is not present |
| `where(path, value)` | Dot-notation assertion |
| `has(path, size)` | Collection size assertion |
| `assertPropMap(key, consumer)` | Nested map assertions |
| `dumpDiff(other)` | Prints the differences between two pages |

### Validation and redirects

```java
@Test
void invalidContactRedirectsBackWithErrors() throws Exception {
    mvc.perform(post("/contacts")
            .param("name", "")
            .param("email", "not-an-email"))
        .andExpect(status().isSeeOther())
        .andExpect(redirectedUrl("/contacts/create"))
        .andExpect(flash().attributeExists("errors"));
}

@Test
void staleVersionIs409() throws Exception {
    mvc.perform(get("/dashboard")
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "stale"))
        .andExpect(status().isConflict())
        .andExpect(header().exists("X-Inertia-Location"));
}
```

For CSRF tests either disable security in the slice
(`inertia.security.mode=disabled`) or fetch the `XSRF-TOKEN` cookie first and
send it as `X-XSRF-TOKEN`. Test both the happy path and the failure path.

## Quarkus: RestAssured

```java
@QuarkusTest
class DashboardTest {

    @Test
    void inertiaVisitReturnsPageObject() {
        var response = given()
            .header("X-Inertia", "true")
            .when().get("/dashboard")
            .then().statusCode(200)
            .header("X-Inertia", equalTo("true"))
            .extract().response();

        InertiaPage.fromJson(response.asString())
            .assertComponent("Dashboard")
            .assertProp("user.name", "Alice")
            .assertUrl("/dashboard");
    }
}
```

`InertiaPage` has two real factories:

| Factory | Input |
|---|---|
| `InertiaPage.fromJson(String)` | Response body (JSON) |
| `InertiaPage.from(PageObject)` | An already-built page object |

There is no `from(HttpResponse)` overload. Parse the body explicitly, or build a
`PageObject` and wrap it.

Common assertions:

```java
InertiaPage page = InertiaPage.fromJson(response.asString());

page.assertComponent("Contacts/Index")
    .assertPropExists("contacts")
    .assertPropCount("contacts", 25)
    .assertMissing("secret")
    .assertDeferredPropsInGroup("slow", "stats")
    .assertMergeProps("contacts")
    .assertOnceProps("announcement")
    .assertScrollProps("contacts")
    .assertEncryptHistory(false)
    .assertFlash("success", "Contact created.");
```

For active reloads, attach an executor. A RestAssured-backed implementation is
short:

```java
InertiaReloadExecutor executor = (url, component, version, only, except) -> {
    var request = given()
        .header("X-Inertia", "true")
        .header("X-Inertia-Partial-Component", component)
        .header("X-Inertia-Version", version);

    if (only != null && !only.isEmpty()) {
        request.header("X-Inertia-Partial-Data", String.join(",", only));
    }
    if (except != null && !except.isEmpty()) {
        request.header("X-Inertia-Partial-Except", String.join(",", except));
    }

    var body = request.when().get(url)
        .then().statusCode(200)
        .extract().asString();

    return InertiaPage.fromJson(body);
};

InertiaPage page = InertiaPage.fromJson(response.asString())
    .withExecutor(executor)
    .reloadOnly("stats")
    .assertPropExists("stats")
    .assertMissing("user");
```

Without an executor, reload helpers throw `IllegalStateException`. That is
intentional: a read model should not guess how to issue HTTP requests.

## Testing props and metadata

| Concern | Spring matcher | Quarkus assertion |
|---|---|---|
| Deferred key | `hasDeferredProps("stats")` | `assertDeferredProps("stats")` |
| Deferred group | `hasDeferredGroup("slow", "stats")` | `assertDeferredPropsInGroup("slow", "stats")` |
| Merge | `hasMergeProps("posts")` | `assertMergeProps("posts")` |
| Prepend | `hasPrependProps("pinned")` | `assertPrependProps("pinned")` |
| Deep merge | `hasDeepMergeProps("settings")` | `assertDeepMergeProps("settings")` |
| Match fields | `hasMatchPropsOn("id")` | `assertMatchPropsOn("id")` |
| Once | `hasOnceProps("notice")` | `assertOnceProps("notice")` |
| Scroll | `hasScrollProps("contacts")` | `assertScrollProps("contacts")` |
| Rescued | `hasRescuedProps("revenue")` | `assertRescuedProps("revenue")` |

Metadata assertions catch regressions that a prop-value assertion misses. A
deferred prop that silently becomes eager still passes `assertProp`, but fails
`assertDeferredProps`.

## Live props

Assert the value and the wire metadata separately:

```java
var body = given()
    .header("X-Inertia", "true")
    .when().get("/inventory")
    .then().statusCode(200)
    .extract().asString();

InertiaPage.fromJson(body)
    .assertComponent("Inventory/Index")
    .assertProp("stockCount", 42);

var json = new ObjectMapper().readTree(body);
assertThat(json.at("/liveProps/stockCount/throttle").asInt()).isEqualTo(2000);
assertThat(json.at("/liveProps/stockCount/listeners/0/channel/type").asText())
    .isEqualTo("private");
```

The Playwright `live-props-contracts.spec.ts` (`LP-01..LP-04`) performs the same
checks against a real browser and both kitchen sinks.

## The TCK

`inertia-tck` is an executable, stack-agnostic suite. Each YAML file under
`inertia-tck/src/main/resources/protocol-v3/` describes requests and expected
wire responses:

| Suite | Covers |
|---|---|
| `01-handshake` | `X-Inertia`, `Vary`, 409, initial HTML |
| `02-partial` | `only`, `except`, dot notation, reset |
| `03-deferred-once` | Deferred groups, once props, TTL |
| `04-redirects` | `302`/`303`/`409`, `back`, external location |
| `05-version` | Version matching and mismatch |
| `06-csrf` | Token flow and failure |
| `07-validation` | Errors, bags, Precognition |
| `08-merge` | Merge, prepend, deep merge, nested paths |
| `09-scroll` | Scroll metadata and intent |
| `10-uploads` | Multipart requests |
| `11-live-props` | Live prop channels, events, throttle |

Run it against a booted application:

```java
InertiaTckRunner.assertGreen("http://localhost:8080", "spring");
InertiaTckRunner.assertGreen("http://localhost:8081", "quarkus");
```

`runSubset(baseUri, stack, ids, pathMapper)` certifies an alternate transport by
rewriting paths. The Quarkus reactive suite uses it to run the same normative
cases under `/tck-reactive`.

## Playwright

The `e2e/` project drives the real demos with the official clients. Specs:
`inertia-contracts`, `live-props-contracts`, `pingcrm-contracts`,
`feature-matrix` and `ssr-contracts`.

```bash
cd e2e
E2E_BASE_URL=http://localhost:8080 npx playwright test \
  --config playwright.config.ts --reporter=line --workers=1 \
  inertia-contracts.spec.ts
```

Run serially after a warm-up request. Cold parallel workers against a freshly
booted JVM demo produce flakes unrelated to product behavior.

SSR specs need the sidecar:

```bash
# terminal 1
cd src/main/webui && node dist-ssr/ssr.js

# terminal 2
INERTIA_SSR_ENABLED=true java -jar target/hello-inertia-*.jar
```

Assert server-rendered text with JavaScript disabled, then stop the sidecar and
assert the same page still renders through CSR. See [SSR](/guide/ssr).

## Test configuration

| Setting | Test value |
|---|---|
| `inertia.security.mode` | `disabled` in unit slices; `framework` in security tests |
| `inertia.template-cache-enabled` | `false` when asserting template changes |
| `inertia.ssr-enabled` | `false` unless the test starts a sidecar |
| `inertia.version-strategy` | Fixed `version-custom` for deterministic `409` tests |
| `inertia.lazy-etag-enabled` | `false` if the test asserts full bodies repeatedly |

Keep a dedicated profile rather than scattering properties across tests, so the
production defaults stay covered somewhere in the suite.

## Checklist

- [ ] Every controller has an initial HTML test and an `X-Inertia` JSON test.
- [ ] Partial reload tests assert both presence and absence.
- [ ] Deferred, merge, once and scroll metadata have explicit assertions.
- [ ] Validation tests assert the redirect, the flash bag and `old` input.
- [ ] CSRF tests cover success and failure.
- [ ] Version mismatch answers `409 + X-Inertia-Location`.
- [ ] The TCK is green on every transport you ship.
- [ ] At least one Playwright flow runs against a production build.

Next: [Security](/guide/security) · [Precognition](/guide/precognition) ·
[Configuration](/reference/configuration)
