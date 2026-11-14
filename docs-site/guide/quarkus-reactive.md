# Quarkus Reactive Routes

`quarkus-inertia` supports the [Reactive Routes](https://quarkus.io/guides/reactive-routes)
programming model: `@RouteBase` and `@Route` on a CDI bean, with handlers
running on the Vert.x event loop. The Inertia contract is identical to Spring
MVC and Quarkus REST — the transport differs, not the protocol.

Reactive Routes are a **stable** target in 0.0.6. They are covered by the same
TCK suites, the same validation bags and the same CSRF rules as the other two
transports.

## When to use them

| Choose | When |
|---|---|
| Quarkus REST (`@Path`, `@GET`) | Your application already uses JAX-RS resources |
| Reactive Routes | You want a Vert.x-native router, fine-grained route matching or non-blocking handlers |
| Spring MVC | You are on Spring Boot |

Both Quarkus transports can coexist in one application; the adapter detects the
request regardless of how the route was registered.

## Install

```xml
<dependency>
  <groupId>io.github.diovamny.quarkus.inertia</groupId>
  <artifactId>quarkus-inertia</artifactId>
  <version>0.0.6</version>
</dependency>

<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-reactive-routes</artifactId>
</dependency>
```

```properties
inertia.root-template=index.html
inertia.session.enabled=true
```

## First route

```java
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@RouteBase(path = "/contacts")
public class ContactsRoutes {

    @Inject Inertia inertia;
    @Inject ContactRepository repository;

    @Route(path = "", methods = Route.HttpMethod.GET)
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", repository.listAll()));
    }
}
```

`@RouteBase` prefixes every route in the class. `@Route(path = "")` matches the
collection root (`/contacts`), not a trailing slash variant. The handler returns
`Uni<Object>` because the adapter serializes asynchronously.

## Full CRUD

```java
@RouteBase(path = "/contacts")
public class ContactsRoutes {

    @Inject Inertia inertia;
    @Inject ContactRepository repository;

    @Route(path = "", methods = Route.HttpMethod.GET)
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", repository.listAll()));
    }

    @Route(path = "create", methods = Route.HttpMethod.GET)
    public Uni<Object> create() {
        return inertia.render("Contacts/Create", Map.of());
    }

    @Route(path = "", methods = Route.HttpMethod.POST)
    @Blocking
    public Uni<Object> store(@Valid ContactForm form) {
        repository.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact created.");
    }

    @Route(path = ":id/edit", methods = Route.HttpMethod.GET)
    public Uni<Object> edit(@Param("id") long id) {
        var contact = repository.find(id);
        if (contact == null) {
            return inertia.back().withFlash("error", "Contact not found.");
        }
        return inertia.render("Contacts/Edit", Map.of("contact", contact));
    }

    @Route(path = ":id", methods = Route.HttpMethod.PUT)
    @Blocking
    public Uni<Object> update(@Param("id") long id, @Valid ContactForm form) {
        repository.update(id, form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact updated.");
    }

    @Route(path = ":id", methods = Route.HttpMethod.DELETE)
    @Blocking
    public Uni<Object> destroy(@Param("id") long id) {
        repository.delete(id);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact deleted.");
    }
}
```

`@Param` comes from `io.quarkus.vertx.web.Param`. The `:id` syntax is Vert.x
path-parameter matching, not JAX-RS `{id}`.

## Reading the request

Handlers may accept a `RoutingContext` when they need headers, query parameters
or the body:

```java
@Route(path = "search", methods = Route.HttpMethod.GET)
public Uni<Object> search(RoutingContext rc) {
    var term = rc.request().getParam("q");
    var page = parseInt(rc.request().getParam("page"), 1);
    return inertia.render("Contacts/Search", Map.of(
        "term", term == null ? "" : term,
        "results", repository.search(term, page)
    ));
}
```

Inside a reactive route, prefer the arguments Quarkus injects over static
accessors. The adapter binds a request-scoped context for every Inertia request,
so shared data, flash, CSRF and header detection work on the event loop.

## Event loop versus `@Blocking`

`@Route` handlers run on the Vert.x event loop by default. Blocking there stalls
every request served by that event loop.

| Handler work | Annotation |
|---|---|
| Reactive client returning `Uni` | No annotation |
| Hibernate Reactive / Panache Reactive | No annotation |
| Classic JPA, JDBC, filesystem, remote blocking HTTP | `@Blocking` |
| CPU-heavy computation | Offload explicitly (worker or `Uni` executor) |

```java
@Route(path = "", methods = Route.HttpMethod.POST)
@Blocking // classic JDBC repository below
public Uni<Object> store(@Valid ContactForm form) {
    repository.create(form);
    return inertia.redirect("/contacts");
}
```

The adapter itself never blocks the event loop: response writing and template
rendering are wrapped in `Uni`. If your handler is marked `@Blocking`, the
adapter still detects the correct context.

## Sessions and flash

`inertia.session.enabled=true` installs a Vert.x `SessionHandler` with
`LocalSessionStore` on every router, ordered before the Inertia handler. Flash
messages and session-backed CSRF then work without manual wiring.

```properties
inertia.session.enabled=true
```

If the application registers its own `SessionHandler` (for example a Redis
store), set `inertia.session.enabled=false`; two handlers would fight over the
same cookie. Disabling auto-provisioning emits a startup `WARN` because flash
and session CSRF degrade without it. See [Quarkus](/guide/quarkus) for the full
session contract.

## CSRF

`quarkus-rest-csrf` only sees JAX-RS endpoints. Reactive Routes are invisible to
it, so the adapter needs an explicit list of mutating prefixes:

```properties
inertia.security.mode=framework
inertia.security.reactive-csrf-paths=/contacts,/api
quarkus.rest-csrf.cookie-name=XSRF-TOKEN
quarkus.rest-csrf.token-header-name=X-XSRF-TOKEN
quarkus.rest-csrf.cookie-http-only=false
```

With that list, a `POST /contacts` without a valid `X-XSRF-TOKEN` answers `419`
(or the configured failure) instead of reaching the handler. The official client
echoes the cookie automatically.

Rules:

- Prefixes are matched on the raw path, so `/contacts` covers `/contacts/1`.
- List every mutating `@RouteBase`; missing prefixes fail open at the framework
  layer.
- Keep `inertia.security.mode=framework` (or `auto`, which resolves to framework
  when the extension is present) so the switch is enforced.
- `inertia.security.fail-on-fallback=true` refuses to boot when the adapter
  cannot honor the configured mode.

## Concurrency and request isolation

Vert.x context locals are shared across requests on the same event loop and are
visible to `@Blocking` workers. A naive implementation can leak headers, flash
data or CSRF state between concurrent requests.

0.0.6 ships the G-17 fix: `InertiaContextLocals` resolves through a
request-scoped holder first, then the Vert.x local, then `CurrentVertxRequest`,
then the CDI `RoutingContext`. Worker threads never read the shared map. The
permanent regression guard is `ReactiveStressTest`, which runs eight identities
over twelve rounds and asserts sessions, flash, CSRF, `303`/`409`, bags and
once/deferred/partial/reset metadata stay isolated.

You do not need to configure anything; the guarantee is part of the adapter.
Do not cache request-scoped values in fields of a `@RouteBase` bean: the bean is
application-scoped and shared by every request.

## Transport parity

| Area | Spring MVC | Quarkus REST | Reactive Routes |
|---|---|---|---|
| Handshake, partials, deferred, once | ✅ | ✅ | ✅ |
| Redirects `302`/`303`/`409` and versioning | ✅ | ✅ | ✅ |
| CSRF `303` + flash, `419` | ✅ | ✅ | ✅ |
| Validation bags and multi-message arrays | ✅ | ✅ | ✅ |
| Merge, append, nested routes, reset | ✅ | ✅ | ✅ |
| Scroll metadata and infinite-scroll intent | ✅ | ✅ | ✅ |
| Multipart uploads | ✅ | ✅ | ✅ |
| SSR with CSR fallback | ✅ | ✅ | ✅ |
| Concurrency isolation | HTTP session | HTTP session | `ReactiveStressTest` |

Version mismatch on a reactive route answers `409` with an
`X-Inertia-Location` pointing at the same reactive path
(`/tck-reactive/...` in the TCK); the semantics are identical, only the path
prefix differs.

## Testing

```java
@QuarkusTest
class ContactsRoutesTest {

    @Test
    void initialVisitReturnsHtml() {
        given().when().get("/contacts")
            .then().statusCode(200)
            .body(containsString("id=\"app\""));
    }

    @Test
    void inertiaVisitReturnsJson() {
        given()
            .header("X-Inertia", "true")
            .when().get("/contacts")
            .then().statusCode(200)
            .header("X-Inertia", equalTo("true"))
            .body("component", equalTo("Contacts/Index"));
    }

    @Test
    void staleVersionAnswers409() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "stale")
            .when().get("/contacts")
            .then().statusCode(409)
            .header("X-Inertia-Location", containsString("/contacts"));
    }
}
```

`InertiaPage.fromJson(body)` adds fluent assertions for component, props,
deferred groups, merge metadata and bags. The TCK itself runs the same YAML
suites against `TckReactiveTest`, so if the contract passes there it passes for
your routes.

## Operational notes

- Keep the `reactive-stress` profile in CI; it catches context leaks that
  functional tests miss.
- Watch for `BlockedThreadChecker` warnings on the first SSR render; they are
  self-healing while the JVM warms up.
- Do not call `await()` on a `Uni` inside a route handler; compose instead.
- Return `Uni<Object>` even for pages that could be synchronous, so the router
  has one handler shape.
- Log request identifiers with the Vert.x context, not thread names: several
  requests share an event loop thread.

Next: [Quarkus](/guide/quarkus) · [Server Setup](/guide/server-setup) ·
[SSR](/guide/ssr) · [Security](/guide/security)
