# Props

Props are the data a page receives. The Java adapter supports the full Inertia
v3 prop taxonomy: plain values, deferred groups, optional props, once props,
mergeable collections, scroll metadata, live props and server-side caching.

Every strategy is declared on the `Inertia` facade before rendering. The page
object then carries the value plus the protocol metadata the official client
needs to merge, defer or refresh it.

Snippets without a framework tab use the Spring Boot API. In Quarkus, resolver
parameters are `Supplier<Uni<Object>>` — write
`() -> Uni.createFrom().item(value)` or return the `Uni` from a reactive
client.

## Prop kinds at a glance

| Kind | Declared with | First visit | Partial reload |
|---|---|---|---|
| Plain | `render("Page", Map.of(...))` | Resolved | Filtered by `only`/`except` |
| Shared | `share(...)` / `always(...)` | Merged into every page | `always` survives |
| Deferred | `deferred(group, key, supplier)` | `null` + `deferredProps` | Resolved when selected |
| Optional | `optional(key, supplier)` | Omitted | Resolved when requested |
| Once | `once(key, value[, ttl])` | Delivered once | Omitted afterwards |
| Merge | `merge(key, value, rule)` | Listed in `mergeProps` | Client merges |
| Append plan | `mergeable(key, value)...value()` | Dotted merge metadata | Per-path merge |
| Scroll | `scroll(key, value, metadata)` | Value + `scrollProps` | Infinite scroll |
| Live | `live(key, supplier[, throttle], listeners...)` | Value + `liveProps` | Refreshed on event |
| Cached | Spring `cached(...)` / Quarkus `cache(...)` | Resolved once per TTL | Reuses cache |
| Raw JSON | `rawJson(json)` | Embedded verbatim | Same |
| Flash | `redirect(...).withFlash(...)` | Props once | Same |

## Plain props

::: code-group

```java [Spring Boot]
@GetMapping("/users/{id}")
public Object show(@PathVariable long id) {
    var user = users.findById(id);
    return inertia.render("Users/Show", Map.of(
        "user", user,
        "canEdit", permissions.canEdit(user)
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
        "canEdit", permissions.canEdit(user)
    ));
}
```

:::

A record, DTO or `Map` can be used directly. DTOs implementing
`ProvidesInertiaProperties` decide which fields they contribute and are
resolved recursively at any depth:

```java
public record UserDto(Long id, String name, String email)
        implements ProvidesInertiaProperties {

    @Override
    public Map<String, Object> toInertiaProperties(RenderContext context) {
        var props = new HashMap<String, Object>();
        props.put("id", id);
        props.put("name", name);
        if (context.isPropRequested("email")) {
            props.put("email", email);
        }
        return props;
    }
}
```

## Deferred props

Deferred props keep the first paint fast. The server sends `null` plus a
`deferredProps` map; the client requests the group after mount.

```java
inertia.deferred("slow", "stats", () -> analytics.summary());
inertia.deferred("slow", "activity", () -> audit.recent(20));
return inertia.render("Dashboard", Map.of("user", user));
```

On the client, reload the group when the section becomes visible:

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { router, WhenVisible } from '@inertiajs/vue3'
</script>

<template>
  <WhenVisible data="stats" #default="{ loading }">
    <div v-if="loading">Loading stats…</div>
    <Stats v-else :stats="$attrs.stats" />
  </WhenVisible>
</template>
```

```tsx [React 19]
import { WhenVisible } from '@inertiajs/react'

<WhenVisible data="stats" fallback={<div>Loading stats…</div>}>
  <Stats />
</WhenVisible>
```

```svelte [Svelte 5]
<script lang="ts">
  import { WhenVisible } from '@inertiajs/svelte'
</script>

<WhenVisible data="stats">
  <Stats />
</WhenVisible>
```

:::

`WhenVisible` performs a partial reload with `only: ['stats']` when the element
enters the viewport. You can also trigger it manually:

```js
router.reload({ only: ['stats'] })
```

## Optional props

Optional props are never sent on a full visit and are not announced in
`deferredProps`. They exist only for explicit partial reloads, which makes them
ideal for panels the user opens on demand:

```java
inertia.optional("audit", () -> audit.forEntity(entityId));
return inertia.render("Orders/Show", Map.of("order", order));
```

```js
router.reload({ only: ['audit'] })
```

## Once props

A once prop is delivered once per client session and then remembered through
`X-Inertia-Except-Once-Props`. Use it for announcements, onboarding copy or
one-time configuration:

::: code-group

```java [Spring Boot]
inertia.once("announcement", announcements.current());
inertia.once("welcome", welcomePayload, "onboarding");
inertia.once("trialBanner", banner, Duration.ofDays(7));
```

```java [Quarkus]
inertia.once("announcement",
    () -> Uni.createFrom().item(announcements.current()));
inertia.once("welcome", welcomePayload, "onboarding");
inertia.once("trialBanner",
    () -> Uni.createFrom().item(banner),
    "onboarding", Instant.now().plus(Duration.ofDays(7)));
```

:::

| Signature (Spring) | Meaning |
|---|---|
| `once(key, value)` | Default client tracking key |
| `once(key, value, customKey)` | Several props can share one tracking key |
| `once(key, value, ttl)` | The client forgets it after the TTL |
| `once(key, value, customKey, ttl)` | Both custom key and expiry |

In Quarkus, the value can also be a `Supplier<Uni<Object>>`; expiry uses an
absolute `Instant`:
`once(key, supplier, customKey, expiresAt)`.

A client can force a once prop to be re-sent by listing it in `only`; the
adapter always honors an explicit request.

## Merge props

Merge props let the client append a new page of data to the previous value
instead of replacing it. The adapter emits `mergeProps`, `prependProps`,
`deepMergeProps` and `matchPropsOn` metadata.

```java
// Append a page to the existing list
inertia.merge("posts", posts.page(2), Inertia.MergeRule.MERGE);

// Add pinned items at the top
inertia.merge("notifications", newOnes, Inertia.MergeRule.PREPEND);

// Recursively merge nested maps
inertia.merge("settings", patch, Inertia.MergeRule.DEEP_MERGE);
```

### Explicit per-path plans

For nested payloads use `mergeable()`, which maps directly to the official
`append()` API:

```java
return inertia.render("Posts/Index", Map.of(
    "posts", inertia.mergeable("posts", page)
        .append("data")       // mergeProps: posts.data
        .prepend("pinned")    // prependProps: posts.pinned
        .matchOn("data.id")   // matchPropsOn: posts.data.id
        .value()
));
```

Rules:

- Paths are dot-notation and relative to the prop root.
- Blank or malformed paths fail fast.
- A path declared twice keeps the last operation.
- `matchOn` accepts several fields and is relative to the prop root.
- `X-Inertia-Reset` of a parent prunes the dotted descendants.

The legacy `merge(key, value, rule, matchOn...)` overload remains available in
`0.x` and uses the same wire metadata.

## Scroll props

Scroll props power the official `InfiniteScroll` component. The metadata tells
the client which page it is on, whether there is a next page and how to match
elements:

```java
inertia.scroll("contacts", page.content(), Map.of(
    "currentPage", page.number(),
    "previousPage", page.number() - 1,
    "nextPage", page.hasNext() ? page.number() + 1 : null,
    "pageName", "page",
    "matchOn", "id"
));
return inertia.render("Contacts/Index", Map.of());
```

`rememberScrollProp(key, value)` (Spring) is a shortcut for a plain scroll prop
whose position should survive a return visit.

## Cached props

Caching memoizes an expensive resolver server-side without changing the wire
shape. Spring calls it `cached`, Quarkus calls it `cache`:

::: code-group

```java [Spring Boot]
inertia.cached("revenue", Duration.ofMinutes(5), () -> reports.revenue());
inertia.cached("exchangeRates", () -> fx.latest());
return inertia.render("Finance", Map.of());
```

```java [Quarkus]
inertia.cache("revenue", Duration.ofMinutes(5),
    () -> Uni.createFrom().item(reports.revenue()));
inertia.cache("exchangeRates", () -> financialClient.latest());
return inertia.render("Finance", Map.of());
```

:::

A failed or `null` deferred/cached resolver can be marked as rescued with
`inertia.rescue("revenue")`; the key is then listed in `rescuedProps` and the
client can render a fallback instead of failing the visit.

## Raw JSON

When a prop is already a JSON document and must not be re-escaped, wrap it:

```java
inertia.rawJson("""
    {"type":"FeatureCollection","features":[]}
    """);
```

`RawJson` is embedded verbatim. Only use it with trusted, valid JSON; it is not
a sanitizer.

## Client-side typing

Declare props explicitly so TypeScript catches server/client drift:

```ts
interface Contact {
  id: number
  name: string
  email: string
  phone: string | null
}

defineProps<{ contacts: Contact[]; flash?: { success?: string } }>()
```

## Testing props

Both adapters ship fluent assertions for props, deferred groups and merge
metadata:

```java
// Spring
result.andExpect(InertiaResultMatchers.inertia()
    .component("Dashboard")
    .prop("user.name", "Alice")
    .hasDeferredProps("stats"));

// Quarkus
InertiaPage.fromJson(response.asString())
    .assertComponent("Dashboard")
    .assertProp("user.name", "Alice")
    .assertDeferredProps("stats");
```

See [Testing](/guide/testing) for the full DSL, [Live Props](/guide/live-props)
for real-time values and [Partial Reloads](/guide/partial-reloads) for the
request protocol.
