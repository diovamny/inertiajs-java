# Partial Reloads

A partial reload asks the server for a subset of the current page's props
without leaving the page. Inertia uses it for deferred props, infinite scroll,
polling and manual refreshes. The server decides what to evaluate; the client
only lists keys.

## Request headers

| Header | Meaning |
|---|---|
| `X-Inertia-Partial-Component` | Component currently mounted on the client |
| `X-Inertia-Partial-Data` | Comma-separated keys to include (`only`) |
| `X-Inertia-Partial-Except` | Comma-separated keys to exclude (`except`) |
| `X-Inertia-Reset` | Keys whose merge state must be reset |
| `X-Inertia-Except-Once-Props` | Once props the client already has |
| `X-Inertia-Infinite-Scroll-Merge-Intent` | `append` or `prepend` for scroll props |

A partial reload is only honored when `X-Inertia-Partial-Component` matches the
component being rendered. This prevents a stale component from filtering a new
page by accident.

## Client API

::: code-group

```js [Vue 3]
import { router } from '@inertiajs/vue3'

// Only these props travel
router.reload({ only: ['contacts', 'flash'] })

// Everything except these
router.reload({ except: ['stats'] })

// Reset merge metadata for a collection
router.reload({ only: ['contacts'], reset: ['contacts'] })

// Change the query string as part of the visit
router.get('/contacts', { page: 2 }, { only: ['contacts'], preserveState: true })
```

```js [React 19]
import { router } from '@inertiajs/react'

router.reload({ only: ['contacts', 'flash'] })
router.reload({ except: ['stats'] })
router.reload({ only: ['contacts'], reset: ['contacts'] })
router.get('/contacts', { page: 2 }, { only: ['contacts'], preserveState: true })
```

```js [Svelte 5]
import { router } from '@inertiajs/svelte'

router.reload({ only: ['contacts', 'flash'] })
router.reload({ except: ['stats'] })
router.reload({ only: ['contacts'], reset: ['contacts'] })
router.get('/contacts', { page: 2 }, { only: ['contacts'], preserveState: true })
```

:::

`<Link>` accepts the same options:

```vue
<Link href="/contacts" :only="['contacts']" preserve-scroll>Refresh list</Link>
```

## Server-side filtering

The adapter filters plain props after they are declared:

```java
@GetMapping("/contacts")
public Object index(@RequestParam(defaultValue = "1") int page) {
    return inertia.render("Contacts/Index", Map.of(
        "contacts", contacts.page(page),
        "stats", contacts.stats(),          // filtered out by only: ['contacts']
        "filters", filters.current()
    ));
}
```

A request with `X-Inertia-Partial-Data: contacts` receives only `contacts`
(plus `always` shared props and protocol metadata). The controller does not
branch on headers.

### Dot notation

Keys may select nested values:

| Header value | Included |
|---|---|
| `only: ['user']` | The whole `user` prop |
| `only: ['user.name']` | `user` with only `name` evaluated |
| `only: ['auth.user.email']` | Deeply nested selection |
| `except: ['user.email']` | `user` without `email` |

This is what makes `ProvidesInertiaProperties` useful: it receives the selection
and can skip the expensive fields.

```java
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
```

## Strategy interaction

| Prop kind | Full visit | `only` includes key | `except` excludes key |
|---|---|---|---|
| Plain | Included | Included | Omitted |
| Shared | Included | Included | Omitted |
| `always` | Included | Included | Included |
| Deferred | `null` + metadata | Resolved | Omitted |
| Optional | Omitted | Resolved | Omitted |
| Once | Included once | Forced again | Omitted |
| Merge | Included | Included, merges | Omitted |
| Live | Resolved | Resolved fresh | Omitted |
| Scroll | Included | Included, merges | Omitted |

This matrix is pinned by TCK suites `02-partial.yaml` and
`03-deferred-once.yaml` on Spring MVC, Quarkus REST and Quarkus Reactive.

## Reset

Merge metadata survives partial reloads by design. To start a collection from
scratch, send `reset`:

```js
router.reload({ only: ['contacts'], reset: ['contacts'] })
```

`reset: ['posts']` also clears dotted descendants such as `posts.data` and
`posts.pinned`. Use it after changing a filter, switching a tenant or returning
from a create form.

## Deferred groups

Deferred props are grouped on the server:

::: code-group

```java [Spring Boot]
inertia.deferred("slow", "stats", () -> analytics.summary());
inertia.deferred("slow", "activity", () -> audit.recent());
```

```java [Quarkus]
inertia.deferred("slow", "stats",
    () -> Uni.createFrom().item(analytics.summary()));
inertia.deferred("slow", "activity",
    () -> Uni.createFrom().item(audit.recent()));
```

:::

Client:

```js
router.reload({ only: ['stats', 'activity'] })
```

`WhenVisible` and `InfiniteScroll` emit these reloads automatically. A group
name is metadata only; the actual selection is always the key list. Loading one
key of a group does not require loading the rest, although loading the group
together is usually the better user experience.

## Once props

Once props are omitted after the first delivery. The client reports what it
already has through `X-Inertia-Except-Once-Props`, and the adapter filters
accordingly. An explicit `only` wins over that suppression:

```js
// Force the announcement again
router.reload({ only: ['announcement'] })
```

## Testing partial reloads

Send the headers explicitly in integration tests:

```java
// Spring
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

```java
// Quarkus
InertiaPage page = InertiaPage.fromJson(
    given()
        .header("X-Inertia", "true")
        .header("X-Inertia-Partial-Component", "Dashboard")
        .header("X-Inertia-Partial-Data", "user")
        .when().get("/dashboard")
        .then().statusCode(200)
        .extract().asString()
);

page.assertPropExists("user").assertMissing("stats");
```

Both `InertiaPage` implementations also expose active reload helpers
(`reloadOnly`, `reloadExcept`, `loadDeferredProps`) when constructed with a
MockMvc or a Quarkus executor.

## Version and ETag interaction

- A partial reload with a stale `X-Inertia-Version` answers `409` and forces a
  full page load; props are not partially merged across versions.
- With `inertia.lazy-etag-enabled=true`, a repeated partial reload may answer
  `304 Not Modified` when nothing changed.
- `Vary` includes the partial headers, so caches do not serve a filtered
  payload to a full visit.

## Best practices

- Pass `preserveState: true` when reloading form-heavy pages.
- Prefer `only` over `except`: an explicit allowlist is easier to reason about.
- Reset merge collections when the query or tenant changes.
- Keep deferred resolvers independent; do not rely on evaluation order.
- Assert both presence and absence in tests, not only the happy path.

Next: [Props](/guide/props) · [Scroll Management](/guide/scroll-management) ·
[Testing](/guide/testing)
