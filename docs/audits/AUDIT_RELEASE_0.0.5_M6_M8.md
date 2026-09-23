# Auditoría release-0.0.5 — M6-lite + M8 gates (P100-12/13/14/15/16)

- Rama: `release-0.0.5`. Fecha: 2026-09-23.
- Resultado: **VERDE con gates pre-tag documentados (M4b, revisión externa,
  smoke nativo en CI de tag). Sin `git push` realizado.**

## M6-lite (P100-12/13): inventario, cero movimientos

- Sin reescritura: el comportamiento queda congelado bajo TCK+E2E verdes.
- `docs/adr/006-core-decision-inventory.md`: lo que ya vive en core
  (`ValidationErrors`, `MergePlan`/`MergeableBuilder`, `SsrEndpointPolicy`,
  `PageObject`, markers, `VaryHeaders`, guards, SSR, `result/*`) + regla
  (toda decisión nueva nace en core) + orden de extracción post-0.0.5.
- ADRs 003/004/005 documentan las decisiones M2/M3/M5 (erasure, rutas,
  fail-fast).

## M8 gates (P100-14/15/16)

- [x] `.\mvnw.cmd -B verify -Pquality-gates -DskipStaticAnalysis=false
  -DskipDependencyCheck=true -DskipMutationAnalysis=true` →
  **BUILD SUCCESS** (7/7 módulos): 0 violaciones Checkstyle (2 novias
  encontradas y corregidas en este ciclo: `MissingSwitchDefault` en
  `MergePlan`, `UnusedImports` en test Quarkus), SpotBugs High limpio,
  suites completas verdes (core, tck, quarkus 331, spring, security ×2).
- [x] `japicmp` (`-Papicheck`, oldVersion 0.0.4): core/spring/quarkus
  **MINOR** (solo adiciones; la ruptura transitoria del `@Bean`
  `precognitionHandler` se revirtió a setter opcional).
- [x] PIT: subido a 1.17.4; el minion aborta (`UNKNOWN_ERROR`) también
  acotado a un paquete, sin JaCoCo y con `--add-opens` (3 repros locales).
  Causa ambiental de la estación (pendiente veredicto Linux CI). Gate
  sigue report-only + ticket P100-14; paso `release.yml` actualizado
  (versión + `-Djacoco.skip=true` + comentario de seguimiento).
- [x] SCA fail-closed: `release.yml` exige `NVD_API_KEY` en tags (falla
  cerrado); Dependency-Check 12.1.0, CVSS ≥ 8 bloquea. SBOM agregado
  post-deploy (paso existente). Firmas GPG + provenance (existentes).
- [x] `verify:metadata` verde; matriz `60/62 verified, 8 E2E`.

## Pre-tag gates (bloquean `v0.0.5`, ninguno sale de 0.0.5)

1. M4b E2E cells (Quarkus-Vue login demo, React/Svelte fixtures,
   Reactive dedicada, SSR) — ticket en `AUDIT_RELEASE_0.0.5_M4a.md`.
2. Revisión externa documentada (C100, APIs errores/merge, SSR, Reactive).
3. Native smoke en CI de tag (`native-tests.yml` ya corre en `v*`).
4. PIT: veredicto Linux CI; si corre, umbrales progresivos; si aborta,
   queda report-only justificado (no llamarlo gate).

## R100 a esta fecha

Wrapper ✓, versiones ✓, calidad bloqueante ✓ (salvo PIT justificado),
SCA fail-closed en tags ✓, SBOM/firmas/provenance (workflow, se ejecutan
en tag) ⏳, compatibilidad binaria ✓, docs coherentes ✓, revisión
externa ⏳, P0/P1 abiertos: 0 en producto (M4b es cobertura E2E, no defecto).
