# Props

- **Optional**: resolved only when a partial requests them.
- **Deferred**: resolved per group on partial reloads.
- **Once**: header-tracked (`X-Inertia-Except-Once-Props`), with custom
  tracking keys and TTL expiry (Spring `Duration`, Quarkus `Instant`).
- **Merge**: `merge` / `prepend` / `deepMerge` metadata plus `matchPropsOn`
  dedup keys; `Reset` clears them. No explicit `append()` API (append intent
  via merge + scroll).
- **Infinite scroll**: `scrollProps` metadata; the
  `X-Inertia-Infinite-Scroll-Merge-Intent: prepend` header switches sides.
