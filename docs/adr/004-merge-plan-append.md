# ADR 004 — Explicit `append()` via `MergePlan` (P100-06)

- Status: accepted (release-0.0.5, M3).
- Context: the wire already carried `mergeProps`/`prependProps`/
  `deepMergeProps`/`matchPropsOn` (even dotted, e.g. `posts.data`), but the
  Java API only exposed whole-prop `merge(key, value, rule, matchOn...)`
  with no way to say "append `posts.data`, prepend `posts.pinned`" on one
  composite prop (Laravel `append()` / Rails per-path parity).
- Decision: immutable core `MergePlan` (prop key + value + per-path
  append/prepend/deep sets + `matchOn` fields, all relative to the prop
  root) plus request-bound `MergeableBuilder`
  (`inertia.mergeable(key, value).append(path).prepend(path).deep(path)
  .matchOn(fields...).value()` and `applyMergePlan(plan)` on both facades).
  Rules: dot-notation relative paths, blank/malformed rejected, duplicates
  keep the last operation, `matchOn` resolves against the prop root (same
  rule as the legacy overload), parent `X-Inertia-Reset` prunes dotted
  descendants (pre-existing). Legacy `merge(...)` unchanged in `0.x`.
- Consequences: same plan produces the same wire on Spring MVC, Quarkus
  REST and Reactive Routes (TCK `08-merge.yaml` + `merge-nested`); additive
  API only (`japicmp` MINOR).
