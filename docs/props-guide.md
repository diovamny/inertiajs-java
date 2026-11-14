# Props Guide: the Complete Typed Hierarchy

Reference for every prop kind both adapters support, the sealed result
types, and the testing equivalents. Wire behavior for each kind is pinned
by the executable TCK (`inertia-tck/src/main/resources/protocol-v3/`).

## Prop kinds

| Kind | Declared with | First visit | Partial reload |
|---|---|---|---|
| Plain | `render("Page", Map.of("a", 1))` | resolved | filtered by `only`/`except` |
| Shared | `inertia.share(...)` / `always(...)` | merged into every page | merged, `always` survives `flushShared()` |
| Deferred | `inertia.deferred("slow", "data", supplier)` | sent as `null` + `deferredProps` metadata | resolved when requested via `X-Inertia-Partial-Data` |
| Optional | `inertia.optional("section", supplier)` | omitted unless requested | resolved when requested |
| Once | `inertia.once("notice", value)` | delivered once | omitted afterwards; `X-Inertia-Except-Once-Props` suppresses by name |
| Merge / prepend / deep-merge | `inertia.merge(...)` and friends | listed in `mergeProps`/`prependProps`/`deepMergeProps` metadata | client merges instead of replacing |
| Explicit append + nested routes | `inertia.mergeable(key, value).append(path).prepend(path).deep(path).matchOn(fields...).value()` or `applyMergePlan(plan)` | dotted `mergeProps`/`prependProps`/`deepMergeProps` + `matchPropsOn` | same; `X-Inertia-Reset` of a parent prunes dotted descendants |
| Scroll | `inertia.scroll(key, value, metadata)` | value + `scrollProps` metadata | infinite-scroll protocol |
| Live | `inertia.live("stockCount", supplier, throttleMs, listeners...)` | value + `liveProps` metadata | resolved when selected; foreign keys stay lazy |
| Cached | `CachedPropStore` (TTL memoization) | resolved once per TTL | same |
| `RawJson` | `inertia.rawJson("...")` | embedded verbatim, never re-escaped | same |
| Flash | `redirect(...).with(...)` / `render(...).flash(...)` | drained into `props` once, then consumed | same |
| Head | `inertia.head()` + `inertia.server-head=true` | published as `props.head` | same |

TCK rules: `02-partial.yaml` (`only`/`except`/mismatch),
`03-deferred-once.yaml`, `07-validation.yaml` (error bags),
`08-merge.yaml` (merge/prepend/deep-merge, nested `merge-nested` routes,
`matchPropsOn`, reset), `09-scroll.yaml` (scroll metadata and merge
intent) and `11-live-props.yaml` (live props).

```java
// Explicit per-path merge (both adapters, same MergePlan + wire):
return inertia.render("Posts/Index", Map.of(
    "posts", inertia.mergeable("posts", posts)
        .append("data")      // -> mergeProps: posts.data
        .prepend("pinned")   // -> prependProps: posts.pinned
        .matchOn("data.id")  // -> matchPropsOn: posts.data.id
        .value()));
```

Rules: paths are dot-notation relative to the prop; blank or malformed
paths fail fast; a path declared twice keeps the last operation; `matchOn`
fields are relative to the prop root and may be several. The legacy
`merge(key, value, rule, matchOn...)` stays available in `0.x`.

## Live props

Live props are resolved by the server like any other prop, but their wire
metadata tells the official client which broadcast channel/event refreshes
them and how often. The resolver lives only in the adapter; `inertia-core`
serializes the pure wire model (`LiveChannel`, `LivePropListener`,
`LivePropMetadata`) and omits `liveProps` when no key is registered.

```java
// Spring: Supplier<Object>; Quarkus: Supplier<Uni<Object>>.
inertia.live("stockCount", stock::current, 2000,
    new LivePropListener(LiveChannel.privateChannel("inventory"), "StockUpdated"));
return inertia.render("Inventory/Index", props);
```

The initial/full visit resolves `stockCount` and emits
`liveProps.stockCount`; a partial reload only evaluates it when
`X-Inertia-Partial-Data` selects the key. `defer().live()` is supported by
chaining both registrations on the same key: the prop appears in
`deferredProps` and `liveProps`, and the resolver stays deferred until a
partial selects it. The demo is `/features/live-props` in both
kitchen-sinks; the contract is pinned by `11-live-props.yaml` and the
Spring/Quarkus live-props integration suites.

## Typed results (`inertia-core`, `result` package)

`InertiaResult` is sealed over `InertiaPageResult(component, props, meta)`,
`InertiaRedirectResult(url, fullPage)` and `InertiaLocationResult(url)`.
Both adapters accept them through typed overloads
(`render`/`redirect`/`location`); Spring MVC additionally serves them as
direct controller returns via `InertiaReturnValueHandler`. The legacy
`Object` / `Uni<Object>` signatures stay available.

## Testing

`InertiaPage` (both adapters) asserts components, props, flash, deferred /
merge / scroll metadata, plus deep paths: `where("users.data[0].name",
"Alice")`, `has("users.data", 10)`, `missing("secretToken")`,
`assertFlash("success", "OK")`, `dumpDiff(expectedPage)` (recursive visual
diff on failure). `assertHasProps(...)` matches partially — the native
equivalent of Laravel's `etc()`; `assertHasExactProps(...)` matches
strictly.
