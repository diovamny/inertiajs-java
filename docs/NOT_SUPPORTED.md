# What is NOT supported (yet) and why

Honest scope for `0.0.5` (branch `release-0.0.5`). Each item links its tracking row in
[`specs/inertia-v3-compliance.yaml`](../specs/inertia-v3-compliance.yaml)
or its release task. Policy: [`COMPATIBILITY_POLICY.md`](COMPATIBILITY_POLICY.md).
v3 clients only; no legacy v1/v2 mode, no `2.x` support row.

## Protocol surface (closes inside 0.0.5, tracked as PARCIAL/IMPLEMENTADO)

- **Explicit `append()` API** (`PROTO-054B`, `PARCIAL`): wire metadata
  (`PROTO-054A`) exists via `mergeProps` + scroll merge-intent, but there is
  no dedicated `append()` method yet. Closes in M3 via `MergePlan` +
  `mergeable().append().prepend().matchOn()` with TCK on 3 transports + E2E.
- **Multiple messages per field** (`PROTO-053B`, `PARCIAL`): the wire shape is
  still `Map<field, message>` (one message per field, default or named bag).
  Closes in M2 via `ValidationErrors` + `inertia.validation.all-errors`
  (default `false`), preserved in flash, bags and Precognition, same JSON on
  3 transports + E2E.
- **Instant-visits `sharedProps` client behavior** (`PROTO-057B`,
  `IMPLEMENTADO`): the field is serialized (`PROTO-057A`); client-observed
  persist/update/exclude/collision across instant visits closes in M4 with
  official Vue/React/Svelte clients.
- **`browserApi` / `sourceLocation` in SSR failures**: local failures
  classify as `unreachable / timeout / error-status / unknown` with hints
  (server-side only); sidecar-reported browser details travel inside render
  payloads, not in the classification.
- **Legacy v1/v2 root template** (`<div data-page>`): intentionally
  unsupported, not even opt-in (v3-pure, −59% bootstrap bytes). No legacy API.

## Transports and platforms (all stable targets in 0.0.5)

- **Quarkus Reactive Routes**: stable target in `0.0.5`, same contract as
  Spring MVC and Quarkus REST. Any remaining divergence is a `NO_APLICA`
  row with justification or an open P0/P1 bug. Evidence: full TCK on
  3 transports, `reactive-stress` profile, 9-cell E2E matrix —
  see [reactive parity](reactive-parity.md).
- **Inertia DevTools module**: out of scope on purpose — the browser
  extension already interops with any correct v3 server.
- **Frontend behavior** (polling, prefetching, UI state): client-side, proven
  by E2E contracts, not implemented server-side.

## Operations

- **Configurable JPA guardrails**: serializing full JPA entities as props is
  discouraged (lazy-loading cascades, private-field leaks) — use
  DTOs/records/projections. No automatic entity filter exists.
- **Remote SSR sidecars**: supported only with explicit validation
  (`SsrEndpointPolicy`, fail-fast at startup); default posture is
  `localhost` (see `ssr-setup.md#trust-boundary`). Closes in M5.
- **PIT mutation gate on Windows dev machines**: the PIT coverage minion
  aborts locally (tracked); the release gate runs it blocking on Linux CI
  (M8 makes PIT + SCA fail-closed with SBOM per release).
