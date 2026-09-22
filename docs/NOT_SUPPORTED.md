# What is NOT supported (yet) and why

Honest scope for `0.0.4`. Each item links its tracking row in
[`specs/inertia-v3-compliance.yaml`](../specs/inertia-v3-compliance.yaml)
or its roadmap phase.

## Protocol surface

- **Explicit `append()` API** (`PROTO-054`, `IMPLEMENTED`): append intent is
  covered via `mergeProps` + scroll merge-intent, but there is no dedicated
  `append()` method. A dedicated TCK lands with the merge-matrix work.
- **Multiple messages per field** in error bags: the wire shape is
  `Map<field, message>` (one message per field, default or named bag).
- **Instant-visits `sharedProps` population**: the field exists and is
  emitted; client-observed population across instant visits is verified next.
- **`browserApi` / `sourceLocation` in SSR failures**: local failures
  classify as `unreachable / timeout / error-status / unknown` with hints
  (server-side only); sidecar-reported browser details travel inside render
  payloads, not in the classification.
- **Legacy v1/v2 root template** (`<div data-page>`): intentionally
  unsupported, not even opt-in (v3-pure since `0.0.4`, −59% bootstrap bytes).

## Transports and platforms

- **Quarkus Reactive Routes in production**: stabilization in course (G-17
  regression suite permanent in CI). Prefer Spring MVC or Quarkus REST until
  a full cycle passes without regressions of this class.
- **Inertia DevTools module**: out of scope on purpose — the browser
  extension already interops with any correct v3 server.
- **Frontend behavior** (polling, prefetching, UI state): client-side, proven
  by E2E contracts, not implemented server-side.

## Operations

- **Configurable JPA guardrails**: serializing full JPA entities as props is
  discouraged (lazy-loading cascades, private-field leaks) — use
  DTOs/records/projections. No automatic entity filter exists.
- **Remote SSR sidecars**: supported only with explicit validation; default
  posture is `localhost` (see `ssr-setup.md#trust-boundary`).
- **PIT mutation gate on Windows dev machines**: the PIT coverage minion
  aborts locally (tracked); the release gate runs it on Linux CI.
