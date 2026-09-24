# AUDIT release-0.0.5 — Fase E: feature-matrix React/Svelte (E2E-01..08, E2E-10)

- Branch: `release-0.0.5`. No push, no tag (policy).
- Scope: public `/e2e-probe` fixture in the 4 PingCRM React/Svelte demos +
  `e2e/feature-matrix.spec.ts` (15 cases) + framework error-bag fix on both
  adapters + `specs/e2e-compliance.yaml` + CI matrix + migration note.

## Fixtures (new)

- Spring (x2, identical): `...pingcrm/controller/ProbeController.java` —
  deferred `probe/slow`, once `notice`, mergeable `entries` (matchOn `id`,
  entries-scoped session counter, single-fresh-entry partials), manual
  `withErrors` validation + secondary, multipart upload, `flash+back`
  redirect, delayed `/target`. Public via `/e2e-probe/**` permitAll.
- Quarkus JAX-RS (x2, identical): `...pingcrm/controller/ProbeResource.java`
  — same contract (`merge(..., false, "id")`, `rc.session()`, `@RestForm`
  upload, `renderSync`/`toResponse`). Public via properties permit.
- Clients: `Pages/Probe/Index.tsx` + `Target.tsx` (React x2, standalone, no
  layout) and `lib/Pages/Probe/Index.svelte` + `Target.svelte` (Svelte 5
  runes x2). All sections use `data-testid="probe-*"`.

## Framework fix (behavior, both adapters)

- Finding: manual `back().withErrors(...)` flashed RAW errors, dropping
  `X-Inertia-Error-Bag`; only the validation-exception path wrapped bags.
  Proven by network trace (POST 303 → follow-up JSON `errors` flat).
- Fix: new leaf helpers `support.ErrorBags` (Spring reads
  `InertiaRequestContext`, Quarkus reads `InertiaContextLocals`, both
  null-safe) + `withErrors`/`withValidationErrors`/`withErrorMessages`
  wrapped in `InertiaRedirect`/`InertiaRender`/`InertiaResponse` x2.
  Additive only (no signature change; flat shape preserved without a bag).
- Unit proof: `ErrorBagsTest` 6/6 Spring (incl. redirect-with-bag), 3/3
  Quarkus. Documented in `docs/migration.md` (0.0.4 → 0.0.5, Fase E).

## Evidence (all green, this branch)

- `e2e/feature-matrix.spec.ts`: **15/15 x4 = 60/60**
  (spring-pingcrm-react:8080, spring-pingcrm-svelte:8181,
  quarkus-pingcrm-react:8080, quarkus-pingcrm-svelte:8082), official
  clients `3.7.1`, zero console/page errors per test.
- Adapters: spring-inertia **219/219**, quarkus-inertia **334/334**
  (`./mvnw -B -pl spring-inertia|quarkus-inertia test`).
- Regression: `pingcrm-contracts.spec.ts` 5/5 on spring-pingcrm-react;
  `npm run verify:metadata` OK (9 locks `3.7.1`, matrix refs resolved).
- Matrix: C100 **61/62** (unchanged), I100 **37/90 → 57/90**
  (`specs/e2e-compliance.yaml` verified_cells, generator re-run).

## Spec corrections during the run (no app bug)

- Deferred auto-fetch: official clients reload deferred props after mount;
  probe counter now advances only on `entries` partials; deferred test
  asserts request-level exclusion + settled value.
- Once suppression: server omission + client retention are BOTH asserted
  (request JSON lacks `notice`, DOM keeps it); predicate uses sync
  `request.headers()` (`headerValue()` is async and never matches).
- Merge proof strengthened: partials return the single fresh entry, so only
  a merging client reaches "2 entries" with Alpha retained.

## Honest remainder (not in this phase)

- Reactive cells for React/Svelte (no fixture app serves them yet),
  E2E-09 SSR for React/Svelte (Vue starters only), rescue/scroll browser
  depth, PIT Linux verdict, external review, RC. Tracked in
  `specs/e2e-compliance.yaml` (PARCIAL) — nothing silently closed.
