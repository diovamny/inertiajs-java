# Quarkus

`quarkus-inertia` integrates the Inertia v3 protocol with Quarkus 3.40. It
supports JAX-RS resources, Reactive Routes and Qute root templates, and
auto-provisions the Vert.x session needed by flash messages and session-based
CSRF.

## Requirements

| Tool | Version |
|---|---|
| JDK | 21+ |
| Quarkus | 3.40.1 (3.39.2 also certified) |
| Maven | 3.9+ |
| Node.js | 22+ |

## Install

```xml
<dependency>
  <groupId>io.github.diovamny.quarkus.inertia</groupId>
  <artifactId>quarkus-inertia</artifactId>
  <version>0.0.6</version>
</dependency>

<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-rest-jackson</artifactId>
</dependency>

<!-- Optional: @RouteBase / @Route reactive routes -->
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-reactive-routes</artifactId>
</dependency>
```

```properties
inertia.root-template=index.html
inertia.use-qute=true
inertia.session.enabled=true
```

::: warning Prefix
Quarkus binds the same `inertia.*` prefix as Spring. Do **not** write
`quarkus.inertia.*`: those keys are ignored by the config mapping.
:::

## Auto-provisioned Vert.x session

Since 0.0.6 the adapter registers a Vert.x `SessionHandler` with
`order(-2)` on every router when `inertia.session.enabled=true` (the default).
The handler uses a `LocalSessionStore` and makes flash messages and
session-based CSRF work without manual wiring.

```properties
# default
inertia.session.enabled=true

# only when the application registers its own SessionHandler
inertia.session.enabled=false
```

Disabling it emits a startup `WARN` describing the consequences: flash data is
lost and session-based CSRF cannot be validated. In a clustered deployment the
local store is per-JVM, so either register a distributed `SessionHandler`
(Redis, Infinispan) and disable auto-provisioning, or avoid flash and
session-based CSRF entirely.

Spring Boot needs no equivalent setting: the servlet container provides the HTTP
session and `spring-session` handles clustering.

## First resource

```java
@Path("/")
public class WelcomeResource {

    @Inject Inertia inertia;

    @GET
    public Uni<Object> welcome() {
        return inertia.render("Welcome", Map.of(
            "appName", "Hello Inertia",
            "framework", "Quarkus"
        ));
    }
}
```

Return `Uni<Object>` for JAX-RS resources. A synchronous `Response` variant is
available through `renderSync` when the resource is fully blocking.

## CRUD resource

```java
@Path("/contacts")
public class ContactsResource {

    @Inject Inertia inertia;
    @Inject ContactRepository repository;

    @GET
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", repository.listAll()));
    }

    @GET
    @Path("create")
    public Uni<Object> create() {
        return inertia.render("Contacts/Create", Map.of());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Object> store(@Valid ContactForm form) {
        repository.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact created.");
    }

    @GET
    @Path("{id}/edit")
    public Uni<Object> edit(@PathParam("id") long id) {
        var contact = repository.find(id);
        if (contact == null) {
            return inertia.back().withFlash("error", "Contact not found.");
        }
        return inertia.render("Contacts/Edit", Map.of("contact", contact));
    }

    @PUT
    @Path("{id}")
    public Uni<Object> update(@PathParam("id") long id,
            @Valid ContactForm form) {
        repository.update(id, form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact updated.");
    }

    @DELETE
    @Path("{id}")
    public Uni<Object> destroy(@PathParam("id") long id) {
        repository.delete(id);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact deleted.");
    }
}
```

Add `@Blocking` when the repository performs blocking I/O (classic JPA/JDBC).
Remove it when the client is reactive.

## Qute root template

Enable Qute with `inertia.use-qute=true` and add the `quarkus-qute` extension.
Qute receives `pageTitle`, `ssrHead`, `ssrBody`, `dataPage`, `dataPageAttr` and
`cspNonce`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>{pageTitle ?: 'Hello Inertia'}</title>
    {#if ssrHead}{ssrHead.raw}{/if}
    <link rel="stylesheet" href="/assets/app.css" />
</head>
<body>
    <noscript>This application requires JavaScript.</noscript>
    {#if ssrBody}{ssrBody.raw}{#else}<div id="app"></div>
    <script type="application/json" data-page="app">{dataPage}</script>{/if}
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

The adapter wraps `dataPage` in a `RawString`, so Qute does not escape the JSON.
Without Qute, use the `__INERTIA_*__` placeholders described in
[Root Template](/guide/root-template).

## Reactive Routes

`@RouteBase` + `@Route` are covered in
[Quarkus Reactive](/guide/quarkus-reactive). The short version:

```java
@RouteBase(path = "/contacts")
public class ContactsRoutes {

    @Inject Inertia inertia;

    @Route(path = "", methods = HttpMethod.GET)
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", repository.listAll()));
    }
}
```

## Security integration

With `quarkus-inertia-security` and `inertia.security.mode=auto`:

- unauthenticated Inertia visits answer `409 + X-Inertia-Location`;
- forbidden pages render the configured Inertia component;
- CSRF is owned by `quarkus-rest-csrf`, and the adapter checks that
  `quarkus.rest-csrf.verify-token=true` at startup in `framework` mode;
- mutating Reactive Route prefixes must be declared explicitly with
  `inertia.security.reactive-csrf-paths`, because `quarkus-rest-csrf` only sees
  JAX-RS endpoints.

```properties
inertia.security.mode=framework
inertia.security.fail-on-fallback=true
inertia.security.reactive-csrf-paths=/contacts,/api
```

## Validation

Add `quarkus-hibernate-validator`, annotate the parameter with `@Valid` and let
the adapter map failures. Normal submits redirect back with `errors` and `old`;
Precognition requests answer `422`.

## Testing

```java
@QuarkusTest
class WelcomeResourceTest {

    @Test
    void initialVisitReturnsHtml() {
        given().when().get("/")
            .then().statusCode(200)
            .body(containsString("id=\"app\""));
    }

    @Test
    void inertiaVisitReturnsJson() {
        given()
            .header("X-Inertia", "true")
            .when().get("/")
            .then().statusCode(200)
            .header("X-Inertia", equalTo("true"))
            .body("component", equalTo("Welcome"));
    }
}
```

Use `InertiaPage.fromJson(body)` for fluent prop, bag and metadata assertions.

## Production notes

- Run `npm run build` before packaging; the Maven build copies the frontend
  output into the application.
- Quarkus native builds are supported; see [Native Image](/guide/native-image).
- Configure a distributed session store before scaling horizontally with flash
  or session CSRF.
- The first SSR render may log one `BlockedThreadChecker` warning while the JVM
  warms up; it is self-healing.
- Keep `inertia.security.mode=auto` unless you need a hard guardrail; use
  `fail-on-fallback=true` in regulated environments.

## Module map

| Module | Purpose |
|---|---|
| `inertia-core` | Protocol model and shared wire types |
| `quarkus-inertia` | JAX-RS, Reactive Routes, Qute, Vert.x session |
| `quarkus-inertia-security` | Quarkus Security bridge |
| `inertia-tck` | Executable conformance suites |

Next: [Quarkus Reactive](/guide/quarkus-reactive) · [Server Setup](/guide/server-setup) ·
[SSR](/guide/ssr) · [Security](/guide/security)
