# Server Setup

This page adds the Java adapter to an existing application. If you want a
working app in one command, start with [Starter Kits](/guide/starter-kits).

## Requirements

| Tool | Version |
|---|---|
| JDK | 21+ |
| Maven | 3.9+ (or the Maven Wrapper) |
| Node.js | 22+ |
| npm | 10+ |
| Spring Boot | 4.1.1 |
| Quarkus | 3.40.1 |

## Dependencies

::: code-group

```xml [Maven · Spring Boot]
<dependency>
  <groupId>io.github.diovamny.spring.inertia</groupId>
  <artifactId>spring-inertia</artifactId>
  <version>0.0.6</version>
</dependency>

<!-- Optional: Spring Security integration -->
<dependency>
  <groupId>io.github.diovamny.spring.inertia</groupId>
  <artifactId>spring-inertia-security</artifactId>
  <version>0.0.6</version>
</dependency>
```

```xml [Maven · Quarkus]
<dependency>
  <groupId>io.github.diovamny.quarkus.inertia</groupId>
  <artifactId>quarkus-inertia</artifactId>
  <version>0.0.6</version>
</dependency>

<!-- JSON provider used by page responses -->
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-rest-jackson</artifactId>
</dependency>

<!-- Only for @RouteBase / @Route reactive routes -->
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-reactive-routes</artifactId>
</dependency>
```

```groovy [Gradle · Spring Boot]
implementation 'io.github.diovamny.spring.inertia:spring-inertia:0.0.6'
// optional
implementation 'io.github.diovamny.spring.inertia:spring-inertia-security:0.0.6'
```

```groovy [Gradle · Quarkus]
implementation 'io.github.diovamny.quarkus.inertia:quarkus-inertia:0.0.6'
implementation 'io.quarkus:quarkus-rest-jackson'
// only for reactive routes
implementation 'io.quarkus:quarkus-reactive-routes'
```

:::

The adapter depends on `inertia-core` at the same version; Maven resolves it
transitively. If you manage versions explicitly, pin `inertia-core` to `0.0.6`
as well.

## Minimal configuration

::: code-group

```properties [Spring Boot]
# application.properties
inertia.root-template=index.html
```

```properties [Quarkus]
# application.properties
inertia.root-template=index.html

# Render the root template with Qute when quarkus-qute is present
inertia.use-qute=true

# Default: true. Auto-registers the Vert.x SessionHandler (flash + CSRF).
inertia.session.enabled=true
```

:::

Both adapters read the same `inertia.*` prefix. Do **not** use
`quarkus.inertia.*`: the Quarkus config mapping is bound to `inertia`.

The root template lives in `src/main/resources/templates/index.html`. See
[Root Template](/guide/root-template) for the required placeholders.

## What gets auto-configured

### Spring Boot

- An injectable `Inertia` bean backed by the request-scoped context.
- `InertiaReturnValueHandler`, so controllers can return `InertiaResult`
  subtypes directly.
- Detection, partial reload and version filters.
- Discovery of all `InertiaSharedDataContributor` beans.
- Validation error handling and CSRF integration when Spring Security is
  present.

### Quarkus

- A CDI `Inertia` bean for JAX-RS resources and Reactive Routes.
- Request/response filters for detection, partials, versioning and CSRF.
- `InertiaSharedDataContributor` discovery through CDI.
- A Vert.x `SessionHandler` with `order(-2)` when
  `inertia.session.enabled=true` (the default), so flash and session-based
  CSRF work without manual wiring.
- Qute support when `inertia.use-qute=true`.

## First controller

::: code-group

```java [Spring Boot]
@RestController
public class WelcomeController {

    private final Inertia inertia;

    public WelcomeController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/")
    public Object welcome() {
        return inertia.render("Welcome",
            Map.of("appName", "Hello Inertia"));
    }
}
```

```java [Quarkus]
@Path("/")
public class WelcomeController {

    @Inject Inertia inertia;

    @GET
    public Uni<Object> welcome() {
        return inertia.render("Welcome",
            Map.of("appName", "Hello Inertia"));
    }
}
```

:::

Create `src/main/webui/src/pages/Welcome.vue` (or `.tsx` / `.svelte`) and wire
the client as described in [Client Setup](/guide/client-setup).

## Verify the two visit types

```bash
# Initial visit: full HTML with the page object in a script tag
curl -i http://localhost:8080/

# Inertia visit: JSON page object with X-Inertia: true
curl -i -H "X-Inertia: true" http://localhost:8080/
```

The first response must contain `<div id="app">` and
`<script type="application/json" data-page="app">`. The second must be JSON
with `"component":"Welcome"`, `"props"`, `"url"` and `"version"`, plus the
`X-Inertia: true` response header.

## Adapter modules

| Module | Description |
|---|---|
| `inertia-core` | Protocol v3 model, serialization, props, BigInt, SSR policy |
| `spring-inertia` | Spring MVC adapter |
| `spring-inertia-security` | Spring Security integration |
| `quarkus-inertia` | Quarkus JAX-RS, Reactive Routes and Vert.x session |
| `quarkus-inertia-security` | Quarkus Security integration |
| `inertia-tck` | Shared executable conformance suites 01–11 |

## Next steps

- [Client Setup](/guide/client-setup)
- [Root Template](/guide/root-template)
- [Spring Boot deep dive](/guide/spring-boot)
- [Quarkus deep dive](/guide/quarkus)
- [Configuration reference](/reference/configuration)
