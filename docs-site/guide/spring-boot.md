# Spring Boot

`spring-inertia` integrates the Inertia v3 protocol with Spring MVC on Spring
Boot 4.1. Controllers return pages and redirects; the adapter handles
detection, partial reloads, versioning, validation, CSRF and the root template.

## Requirements

| Tool | Version |
|---|---|
| JDK | 21+ |
| Spring Boot | 4.1.1 |
| Maven | 3.9+ |
| Node.js | 22+ |

## Install

```xml
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

```properties
inertia.root-template=index.html
```

The root template lives in `src/main/resources/templates/index.html`. See
[Root Template](/guide/root-template) for the placeholders and the v3-pure
contract.

## What is auto-configured

| Bean | Responsibility |
|---|---|
| `Inertia` | Request-scoped facade: render, redirect, share, props, head |
| `InertiaReturnValueHandler` | Serves `InertiaResult` subtypes from controller methods |
| Detection filter | Reads `X-Inertia`, version, partial and precognition headers |
| `InertiaSharedDataContributor` discovery | Invokes every contributor before rendering |
| `PrecognitionHandler` | Turns validation exceptions into errors or `422` |
| CSRF integration | Active when Spring Security is on the classpath |
| Version provider | `sha256`, Vite manifest or custom |
| SSR client | Optional Node.js sidecar with CSR fallback |

## First controller

```java
@RestController
public class WelcomeController {

    private final Inertia inertia;

    public WelcomeController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/")
    public Object welcome() {
        return inertia.render("Welcome", Map.of(
            "appName", "Hello Inertia",
            "framework", "Spring Boot"
        ));
    }
}
```

A full CRUD controller follows the same shape:

```java
@Controller
public class ContactsController {

    private final Inertia inertia;
    private final ContactRepository repository;

    public ContactsController(Inertia inertia, ContactRepository repository) {
        this.inertia = inertia;
        this.repository = repository;
    }

    @GetMapping("/contacts")
    public Object index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", repository.findAll()));
    }

    @GetMapping("/contacts/create")
    public Object create() {
        return inertia.render("Contacts/Create", Map.of());
    }

    @PostMapping("/contacts")
    public Object store(@Valid ContactForm form, BindingResult result) {
        if (result.hasErrors()) {
            return inertia.back()
                .withErrors(fieldErrors(result))
                .withInput(inputOf(form));
        }
        repository.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact created.");
    }

    @GetMapping("/contacts/{id}/edit")
    public Object edit(@PathVariable long id) {
        var contact = repository.find(id);
        if (contact == null) {
            return inertia.back().withFlash("error", "Contact not found.");
        }
        return inertia.render("Contacts/Edit", Map.of("contact", contact));
    }

    @PutMapping("/contacts/{id}")
    public Object update(@PathVariable long id,
            @Valid ContactForm form, BindingResult result) {
        if (result.hasErrors()) {
            return inertia.back()
                .withErrors(fieldErrors(result))
                .withInput(inputOf(form));
        }
        repository.update(id, form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact updated.");
    }

    @DeleteMapping("/contacts/{id}")
    public Object destroy(@PathVariable long id) {
        repository.delete(id);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact deleted.");
    }
}
```

Use `@Controller` rather than `@RestController` when returning view-like page
objects; both work, but `@Controller` communicates intent and avoids accidental
message conversion of `Object` returns.

## Typed results

Spring MVC serves typed results directly:

```java
@GetMapping("/users/{id}")
public InertiaPageResult show(@PathVariable long id) {
    return InertiaPageResult.of("Users/Show", Map.of("user", users.find(id)));
}

@PostMapping("/users")
public InertiaRedirectResult store(@Valid UserForm form) {
    users.create(form);
    return InertiaRedirectResult.to("/users");
}
```

`InertiaPageResult`, `InertiaRedirectResult` and `InertiaLocationResult` are
sealed types in `inertia-core`; `Object` returns remain supported.

## Security integration

With `spring-inertia-security` on the classpath and
`inertia.security.mode=auto` (the default), the adapter resolves to framework
mode:

- unauthenticated Inertia visits answer `409 + X-Inertia-Location` to the login
  URL;
- authenticated but forbidden visits render the configured Inertia `403`
  component;
- CSRF is owned by Spring Security, and the adapter only emits the
  `XSRF-TOKEN` cookie;
- Inertia CSRF failures redirect back with `303` and a generic flash message.

```properties
inertia.security.mode=auto              # auto | framework | adapter | disabled
inertia.security.fail-on-fallback=true  # refuse to boot on the adapter fallback
inertia.security.login-url=/login
inertia.security.forbidden-component=Errors/Forbidden
inertia.security.cookie-secure=true     # HTTPS deployments
```

See [Security](/guide/security) for the full contract.

## Error pages

```properties
inertia.error-status=500
inertia.error-component=ErrorPage
inertia.error-details-enabled=false
```

Errors render an Inertia page with a `status` prop instead of a blank screen.
Keep `error-details-enabled=false` outside development.

## Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class WelcomeControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void initialVisitReturnsHtml() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("id=\"app\"")));
    }

    @Test
    void inertiaVisitReturnsJson() throws Exception {
        mvc.perform(get("/").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Inertia", "true"))
            .andExpect(InertiaResultMatchers.inertia().component("Welcome"));
    }
}
```

Add `spring-boot-starter-test` and keep `inertia.security.mode=disabled` in
tests that do not exercise CSRF. See [Testing](/guide/testing) for the full DSL.

## Production notes

- Package the frontend before the JAR: `npm run build` writes the manifest and
  hashed assets into `src/main/resources/static`.
- Prefer `version-strategy=vite-manifest` so a deploy invalidates stale tabs.
- Set `template-cache-enabled=false` only in development; caching is safe in
  production because the template is immutable per build.
- Bound pages with `inertia.max-page-bytes` (32 MiB by default) and return DTOs,
  not JPA entities.
- Run the SSR sidecar as a separate process; never expose it publicly.

## Module map

| Module | Purpose |
|---|---|
| `inertia-core` | Protocol model, props, BigInt, SSR policy |
| `spring-inertia` | Spring MVC adapter |
| `spring-inertia-security` | Spring Security bridge |
| `inertia-tck` | Executable conformance suites |

Next: [Quarkus](/guide/quarkus) · [Server Setup](/guide/server-setup) ·
[Security](/guide/security) · [SSR](/guide/ssr)
