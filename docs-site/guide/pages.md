# Pages

A page is the unit the server tells the client to render. It has a component
name, a set of props, a canonical URL and protocol metadata. The controller
returns a page result; the adapter serializes it.

## Rendering a page

::: code-group

```java [Spring Boot]
@GetMapping("/users/{id}")
public Object show(@PathVariable long id) {
    var user = users.findById(id);
    return inertia.render("Users/Show", Map.of(
        "user", user,
        "permissions", permissions.of(user)
    ));
}
```

```java [Quarkus]
@GET
@Path("/users/{id}")
public Uni<Object> show(@PathParam("id") long id) {
    var user = users.findById(id);
    return inertia.render("Users/Show", Map.of(
        "user", user,
        "permissions", permissions.of(user)
    ));
}
```

:::

The first argument is the **component name**. The client resolves it against
its pages directory:

| Component name | Vue | React | Svelte |
|---|---|---|---|
| `Welcome` | `pages/Welcome.vue` | `pages/Welcome.tsx` | `pages/Welcome.svelte` |
| `Users/Show` | `pages/Users/Show.vue` | `pages/Users/Show.tsx` | `pages/Users/Show.svelte` |

Names are case-sensitive and use `/` as the directory separator. There is no
route table on the client: the server chooses the component on every visit.

## Props sources

The second argument accepts:

1. A `Map<String, Object>` of plain props.
2. A record or DTO, either as the props map or nested inside it.
3. A DTO implementing `ProvidesInertiaProperties` to compute props lazily.
4. A props builder for advanced strategies (`defer`, `optional`, `once`,
   `merge`, `scroll`, `live`).

```java
// Map
return inertia.render("Dashboard", Map.of("stats", stats));

// Record
public record UserDto(Long id, String name, String email) {}
return inertia.render("Users/Show", new UserDto(1L, "Ada", "ada@example.com"));

// Nested DTO
return inertia.render("Dashboard", Map.of("user", userDto, "stats", statsDto));
```

## Self-determining DTOs

`ProvidesInertiaProperties` lets a DTO decide which fields it contributes,
including expensive or sensitive fields that should only be resolved when the
client explicitly requests them:

```java
public record UserDto(Long id, String name, String email, String role)
        implements ProvidesInertiaProperties {

    @Override
    public Map<String, Object> toInertiaProperties(RenderContext context) {
        var props = new HashMap<String, Object>();
        props.put("id", id);
        props.put("name", name);
        props.put("email", email);

        if (context.isPropRequested("permissions")) {
            props.put("permissions", List.of("VIEW_DASHBOARD", "EDIT_PROFILE"));
        }
        return props;
    }
}
```

`RenderContext` exposes:

| Method | Meaning |
|---|---|
| `component()` | Target component, e.g. `Users/Profile` |
| `url()` | Request URI |
| `isPartial()` | Whether this is a partial reload |
| `partialData()` | Keys requested by `X-Inertia-Partial-Data` |
| `partialExcept()` | Keys excluded by `X-Inertia-Partial-Except` |
| `isPropRequested(name)` | Helper honoring both lists |

DTOs are resolved recursively at any depth, and can also be shared globally
with `inertia.share(dto)`.

## Convention routing

Instead of repeating component names, enable convention resolution:

```properties
inertia.convention-routing-enabled=true
inertia.convention-routing-prefix=Pages/
```

Then `inertia.render(userDto)` derives the component from the DTO or controller
name and prepends the configured prefix. Convention routing is opt-in; explicit
names are always clearer for public pages.

## Typed results

`InertiaResult` is a sealed hierarchy in `inertia-core`:

```java
public sealed interface InertiaResult
    permits InertiaPageResult, InertiaRedirectResult, InertiaLocationResult {}
```

| Type | Fields | HTTP behavior |
|---|---|---|
| `InertiaPageResult` | `component`, `props`, `meta` | `200` page (HTML or JSON) |
| `InertiaRedirectResult` | `url`, `fullPage` | `302`/`303` or `409` for external |
| `InertiaLocationResult` | `url` | `409` + `X-Inertia-Location` |

Both adapters accept them through typed overloads:

```java
return InertiaPageResult.of("Users/Show", Map.of("user", user));
return InertiaRedirectResult.to("/users");
return InertiaLocationResult.to("https://example.com");
```

Spring MVC additionally serves typed results directly from controller methods
through `InertiaReturnValueHandler`, so `public InertiaPageResult show()` works
without wrapping. Quarkus accepts the same types from JAX-RS and Reactive
Routes, wrapped in `Uni` when the resource is reactive.

## Page metadata

The render result also carries protocol flags:

```java
// Spring
inertia.setEncryptHistory(true);
inertia.preserveFragment(true);
return ((InertiaRender) inertia.render("Orders/Show", Map.of("order", order)))
    .preserveBigIntegers();

// Quarkus
inertia.encryptHistory(true);
inertia.preserveFragment(true);
return inertia.render("Orders/Show", Map.of("order", order));
```

Metadata is serialized into the page object and consumed by the official
client. See [Responses & BigInt](/guide/responses) for the complete wire shape
and [Props](/guide/props) for the prop strategies.

## Error pages

Unhandled errors render the configured error component instead of a blank page:

```properties
inertia.error-status=500
inertia.error-component=ErrorPage
inertia.error-details-enabled=false
```

The client receives a normal Inertia page with a `status` prop and can render a
friendly error screen. Exception details are hidden by default and must only be
enabled in development.

Next: [Responses & BigInt](/guide/responses) · [Routing](/guide/routing) ·
[Props](/guide/props)
