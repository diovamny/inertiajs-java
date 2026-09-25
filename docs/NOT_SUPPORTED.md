# What is NOT supported (yet) and why

Honest scope for `0.0.5` (branch `release-0.0.5`). Each item links its tracking row in
[`specs/inertia-v3-compliance.yaml`](../specs/inertia-v3-compliance.yaml)
or its release task. Policy: [`COMPATIBILITY_POLICY.md`](COMPATIBILITY_POLICY.md).
v3 clients only; no legacy v1/v2 mode, no `2.x` support row.

## Protocol surface (status verificado en `specs/inertia-v3-compliance.yaml`)

- **Explicit `append()` API** (`PROTO-054B`, `TESTED`): `MergePlan` +
  `mergeable().append().prepend().matchOn()` con TCK en 3 transportes y
  E2E Vue/Spring; resto de celdas E2E en M4b.
- **Multiple messages per field** (`PROTO-053B`, `TESTED`): `ValidationErrors`
  + `inertia.validation.all-errors` (default `false` = legacy
  `Map<field, message>`; `true` = arrays ordenados) en flash, bags y
  Precognition, mismo JSON en 3 transportes; E2E Vue/Spring verde, resto en M4b.
- **Instant-visits `sharedProps` client behavior** (`PROTO-057B`,
  `IMPLEMENTADO`): el campo se serializa (`PROTO-057A`); observado por
  cliente oficial solo en Vue/Spring (M4a); E2E completo en M4b.
- **`browserApi` / `sourceLocation` in SSR failures**: local failures
  classify as `unreachable / timeout / error-status / unknown` with hints
  (server-side only); sidecar-reported browser details travel inside render
  payloads, not in the classification.
- **Legacy v1/v2 root template** (`<div data-page>`): intentionally
  unsupported, not even opt-in (v3-pure, −59% bootstrap bytes). No legacy API.

## Transports and platforms (0.0.5: Reactive graduates stable in this release)

- **Quarkus Reactive Routes**: stable target in `0.0.5`, same contract as
  Spring MVC and Quarkus REST. Graduation completes with the Fase F fixes
  (no event-loop blocking, no split package) + full I100 — no 30-day wait,
  fixes now. Until then: TCK 43/43 + `reactive-stress` green, E2E Vue cells
  in M4b. Any remaining divergence is a `NO_APLICA` row with justification
  or an open P0/P1 bug — see [reactive parity](reactive-parity.md).
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
