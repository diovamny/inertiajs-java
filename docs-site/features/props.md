# Props

- **Optional**: resolved only when a partial requests them.
- **Deferred**: resolved per group on partial reloads.
- **Once**: header-tracked (`X-Inertia-Except-Once-Props`), with custom
  tracking keys and TTL expiry (Spring `Duration`, Quarkus `Instant`).
- **Merge**: `merge` / `prepend` / `deepMerge` metadata plus `matchPropsOn`
  dedup keys; `Reset` clears them. Explicit per-path `append()`/`prepend()`
  is available through `inertia.mergeable(key, value).append(path)...value()`.
- **Infinite scroll**: `scrollProps` metadata; the
  `X-Inertia-Infinite-Scroll-Merge-Intent: prepend` header switches sides.
- **Live props**: `liveProps` metadata with public/private/presence
  channels, events, and throttle; the resolver runs on full visits and on
  partial reloads that select the key, never for foreign keys.
  `defer().live()` emits the key in both `deferredProps` and `liveProps`.
