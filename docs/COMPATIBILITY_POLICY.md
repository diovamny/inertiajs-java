# Compatibility policy — Inertia.js Java (release-0.0.5)

Scope: `0.0.5`, branch `release-0.0.5`. Single source of truth for what
"compatible" means. Replaces informal "58/59" claims.

## 1. Only Inertia v3 — no v2 client, no legacy API

- Supported client: `@inertiajs/*` **v3 only** (Vue 3, React 19, Svelte 5).
- Bootstrap is v3-pure: `<div id="app">` without `data-page`, payload once in
  `<script type="application/json" data-page="app">` (see `PROTO-059`).
- The legacy v1/v2 `<div data-page>` template is intentionally unsupported,
  not even opt-in. There is no legacy mode, no `2.x` support row.
- Server APIs are v3-only. No `legacy` flag will be added to inflate a score.

## 2. Stable transports (Reactive graduates stable inside 0.0.5)

`Spring MVC`, `Quarkus REST` and `Quarkus Reactive Routes` are all stable
targets in `0.0.5` and must reach the same contract. Differences must be a
justified `NO_APLICA` row or an open P0/P1 bug — never a hidden doc note.
Reactive graduation is declared complete when the Fase F fixes land
(no event-loop blocking, no split package) together with full I100;
the 30-day clock is replaced by fixes-now plus `reactive-stress` evidence
(decision 2026-09-24, recorded here instead of silent compliance).

## 3. The three measurements (C100 / I100 / R100)

| Indicator | 100% means |
|---|---|
| **C100 contract** | Every normative requirement of the frozen v3 snapshot has explicit status, direct test and no undeclared exception, on all 3 transports. |
| **I100 interop** | Every applicable E2E scenario passes with official Vue, React and Svelte clients: 9 cells (3 clients x 3 transports). A hand-made HTTP request counts for C100, never for I100. |
| **R100 release** | Clean-checkout reproducible build (Windows + Linux via Maven Wrapper), blocking quality + security gates, signed artifacts + SBOM + hashes + provenance, coherent docs, external review, zero open P0/P1. |

## 4. Evidence ladder (the only allowed statuses)

| Status | Minimum evidence | Counts for C100 | Counts for I100 |
|---|---|---:|---:|
| `NO_EVALUADO` | No decision, no test. | No | No |
| `PARCIAL` | Partial function, announced semantics missing. | No | No |
| `IMPLEMENTADO` | Code + direct unit test. | No | No |
| `TCK_VERIFICADO` (`TESTED` alias) | Same normative TCK case green on all 3 transports. | Yes | No |
| `E2E_VERIFICADO` (`E2E_VERIFIED` alias) | TCK/integration + real official client in all applicable cells. | Yes | Yes |
| `NO_APLICA` (`NOT_APPLICABLE` alias) | Justification linked to official docs; not an omission. | Yes | Yes |

Legacy `TESTED` = `TCK_VERIFICADO`, `E2E_VERIFIED` = `E2E_VERIFICADO`,
`NOT_APPLICABLE` = `NO_APLICA`, `NOT_SUPPORTED` is kept only for
explicitly out-of-scope items with a tracking issue. Every row stores:
official source, snapshot version/date, transports, test, CI artifact,
applicable E2E clients, limitation and owner. The generator fails when any
field is missing or when `IMPLEMENTADO` has no `test_ref`.

## 5. Frozen v3 snapshot

- Snapshot: `specs/inertia-v3-baseline-2026-09-23.yaml` (frozen 2026-09-23,
  protocol + validation + merging-props + instant-visits + SSR).
- Every new upstream capability starts as `NO_EVALUADO`, never as supported
  by inference. Each upstream release opens a new snapshot and re-measures.
- Normative sources: protocol v3, validation, merging-props, instant-visits,
  SSR (see baseline header for URLs).

## 6. Update process

New Inertia/Java/framework change -> new snapshot file -> new `NO_EVALUADO`
rows -> TCK + E2E -> status promotion in `specs/inertia-v3-compliance.yaml`
-> regenerated `docs/protocol-compatibility.md` (never hand-edited) ->
CHANGELOG + migration notes. `node scripts/check-versions.mjs` and the
matrix generators are blocking in CI.
