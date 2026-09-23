# ADR 006 — Protocol decisions inventory: what lives in `inertia-core` (P100-12/13, M6-lite)

- Status: accepted (release-0.0.5; full extraction deferred to post-0.0.5).
- Context: Spring MVC, Quarkus REST and Reactive Routes must not diverge
  when Inertia evolves. A big-bang move of code into the core risks
  regressions without locked observable behavior first (M4 TCK + E2E now
  green on 3 transports).
- Decision (M6-lite for `0.0.5`): no file moves; freeze the inventory of
  pure protocol decisions that already have a single core source, and add
  new decisions only in core:
  - `model/ValidationErrors` (M2), `model/MergePlan` + `model/MergeableBuilder`
    (M3), `ssr/SsrEndpointPolicy` (M5) — all framework-free, all covered by
    core unit tests and by the same TCK on 3 transports;
  - pre-existing: `model/PageObject`, prop markers (`AlwaysProp`,
    `DeferredProp`, `OnceProp`), `http/VaryHeaders`, `security/SafeJsonEncoder`,
    `security/RedirectTargets`, `security/PageSizeGuard`,
    `ssr/SsrFailureClassifier`/`SsrCircuitBreaker`, sealed `result/*`.
  - Adapters keep only: request/response conversion, session/flash/security
    per framework, template integration, framework-idiomatic facades over
    the same core models.
  - Next (post-0.0.5): migrate one family per small PR (partial selection +
    metadata first), each with characterization tests + `japicmp` + consumer
    compilation, starting from this inventory.
- Consequences: zero behavior change in `0.0.5`; drift is prevented by
  construction for every new decision; the inventory is the checklist for
  the post-0.0.5 extraction.
