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
| Scroll | `inertia.scroll(key, value, metadata)` | value + `scrollProps` metadata | infinite-scroll protocol |
| Cached | `CachedPropStore` (TTL memoization) | resolved once per TTL | same |
| `RawJson` | `inertia.rawJson("...")` | embedded verbatim, never re-escaped | same |
| Flash | `redirect(...).with(...)` / `render(...).flash(...)` | drained into `props` once, then consumed | same |
| Head | `inertia.head()` + `inertia.server-head=true` | published as `props.head` | same |

TCK rules: `02-partial.yaml` (`only`/`except`/mismatch),
`03-deferred-once.yaml`, `07-validation.yaml` (error bags).

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
