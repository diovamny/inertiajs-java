# Shared Data

Shared data is resolved on every render and merged into the page props. Use it
for values almost every page needs: the authenticated user, permissions, flash
messages, feature flags or the application version.

Shared data is not a global singleton. It is scoped to the current request and
evaluated again on every visit, so it can safely depend on the session, the
tenant or the current user.

## The contributor SPI

The recommended way to contribute shared data is the
`InertiaSharedDataContributor` SPI. Every bean implementing it is discovered
automatically and invoked before each render.

::: code-group

```java [Spring Boot]
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import io.github.diovamny.spring.inertia.spi.InertiaSharedDataContributor;
import org.springframework.stereotype.Component;

@Component
public class AuthSharedData implements InertiaSharedDataContributor {

    private final UserService users;

    public AuthSharedData(UserService users) {
        this.users = users;
    }

    @Override
    public Map<String, Object> contribute(HttpServletRequest request) {
        return Map.of("auth", Map.of(
            "user", users.currentUser(),
            "permissions", users.currentPermissions()
        ));
    }
}
```

```java [Quarkus]
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;
import io.github.diovamny.quarkus.inertia.spi.InertiaSharedDataContributor;

@ApplicationScoped
public class AuthSharedData implements InertiaSharedDataContributor {

    @Inject UserService users;

    @Override
    public Map<String, Object> contribute(RoutingContext context) {
        return Map.of("auth", Map.of(
            "user", users.currentUser(),
            "permissions", users.currentPermissions()
        ));
    }
}
```

:::

Several contributors are supported. They run in bean registration order
(Spring) or CDI resolution order (Quarkus); later contributions win when two
providers use the same key.

## Programmatic sharing

Controllers can share values for a single visit without a contributor:

```java
@GetMapping("/dashboard")
public Object dashboard() {
    inertia.share("notifications", notifications.unread());
    inertia.share("serverTime", Instant.now());
    return inertia.render("Dashboard", Map.of("stats", stats.summary()));
}
```

| Method | Effect |
|---|---|
| `share(key, value)` | Adds one shared prop for the current request |
| `share(Map<String, Object>)` | Adds several shared props |
| `share(ProvidesInertiaProperties)` | Resolves and shares a self-describing DTO |
| `shared(key)` | Reads a shared prop (or `null`) |
| `shared(key, fallback)` | Reads a shared prop with a default |
| `flushShared()` | Removes shared props, keeping `always` props |

`flushShared()` is useful when a single action must render a page without the
default global payload, for example a public status page inside an otherwise
authenticated application.

## Always props

A normal shared prop participates in partial reload filtering: it travels on
full visits and on partial reloads that include it. An `always` prop is
immortal — it is included in every response, even when `only` selects a single
unrelated key, and it survives `flushShared()`.

```java
inertia.always("app", Map.of(
    "name", "Acme CRM",
    "version", buildInfo.version()
));
```

Use `always` for tiny, cheap values the layout cannot work without: the app
name, the current locale, a CSRF-safe feature flag. Do not put an expensive
query behind `always`; it will run on every partial reload.

## Shared data vs props vs view data

| Mechanism | Reaches components | Scope | Filtered by partial reloads |
|---|---|---|---|
| `share` | Yes, as page props | Request | Yes, unless `always` |
| `always` | Yes, as page props | Request | No |
| Page props | Yes | Page | Yes |
| `viewData` | No, root template only | Request | Not applicable |
| `flash` | Yes, as props | One redirect | No |

Shared props are the base layer: page props with the same key win, and a
shared key never overwrites an explicit page prop. This is `putIfAbsent`
semantics, so a controller can always override a global default.

## Flash messages

Flash data is stored in the session for exactly one request and then exposed as
a normal prop. The starter kits use it after every mutation:

```java
return inertia.redirect("/contacts")
    .withFlash("success", "Contact created.");
```

The client reads `props.flash.success`. Additional session keys can be flashed
on every page with `inertia.flash-keys=success,error`; the adapter drains them
once, exactly like Laravel's `session()->flash()`.

## Example: navigation payload

A typical layout needs the user, permissions and the counts shown in the
sidebar. Compute all three in one contributor so the controller stays small:

```java
@Component
public class NavigationSharedData implements InertiaSharedDataContributor {

    private final NavigationService navigation;

    public NavigationSharedData(NavigationService navigation) {
        this.navigation = navigation;
    }

    @Override
    public Map<String, Object> contribute(HttpServletRequest request) {
        return Map.of(
            "auth", navigation.auth(),
            "nav", Map.of(
                "inbox", navigation.unreadInbox(),
                "tasks", navigation.openTasks()
            )
        );
    }
}
```

## DTO providers

A DTO can implement `ProvidesInertiaProperties` and be shared as a unit:

```java
public record CurrentUser(Long id, String name, String email)
        implements ProvidesInertiaProperties {

    @Override
    public Map<String, Object> toInertiaProperties(RenderContext context) {
        return Map.of(
            "id", id,
            "name", name,
            "email", email
        );
    }
}

inertia.share(currentUser);
```

The provider is resolved during rendering, so `RenderContext` can inspect the
target component, the URL and the partial reload selection.

## Client consumption

Shared data arrives as ordinary props:

::: code-group

```vue [Vue 3]
<script setup lang="ts">
defineProps<{
  auth: { user: { name: string }; permissions: string[] }
  flash?: { success?: string }
}>()
</script>

<template>
  <header>
    <span>{{ auth.user.name }}</span>
    <span v-if="flash?.success" class="flash">{{ flash.success }}</span>
  </header>
</template>
```

```tsx [React 19]
export default function Layout({
  auth,
  flash,
}: {
  auth: { user: { name: string } }
  flash?: { success?: string }
}) {
  return (
    <header>
      <span>{auth.user.name}</span>
      {flash?.success && <span className="flash">{flash.success}</span>}
    </header>
  )
}
```

```svelte [Svelte 5]
<script lang="ts">
  let { auth, flash } = $props()
</script>

<header>
  <span>{auth.user.name}</span>
  {#if flash?.success}<span class="flash">{flash.success}</span>{/if}
</header>
```

:::

## Rules of thumb

- Keep shared data small and predictable; it runs on every visit.
- Never share secrets, raw tokens or full JPA entities.
- Prefer `always` only for values required by the layout on partial reloads.
- Use `viewData` for HTML-only metadata, not shared props.
- Test shared data by asserting the prop on a partial reload, not only on the
  first visit.

Next: [Props](/guide/props) · [Partial Reloads](/guide/partial-reloads) ·
[View Data](/guide/viewdata)
