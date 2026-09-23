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
| Cached | `CachedPropStore` (TTL memoization) | resolved once per TTL | same |
| `RawJson` | `inertia.rawJson("...")` | embedded verbatim, never re-escaped | same |
| Flash | `redirect(...).with(...)` / `render(...).flash(...)` | drained into `props` once, then consumed | same |
| Head | `inertia.head()` + `inertia.server-head=true` | published as `props.head` | same |

TCK rules: `02-partial.yaml` (`only`/`except`/mismatch),
`03-deferred-once.yaml`, `07-validation.yaml` (error bags),
`08-merge.yaml` (merge/prepend/deep-merge, nested `merge-nested` routes,
`matchPropsOn`, reset).

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
