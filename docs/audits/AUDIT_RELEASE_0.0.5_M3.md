# Auditoría release-0.0.5 — M3 Merge append + rutas anidadas (P100-06/07)

- Rama: `release-0.0.5`. Fecha: 2026-09-23.
- Resultado: **VERDE — puede continuar a M5.**

## Criterios 100% M3

- [x] `append()` explícito documentado y probado con rutas anidadas: core
  `MergePlan` (inmutable, reglas de duplicados/inválidas/matchOn múltiple) +
  `MergeableBuilder` fluido + `inertia.mergeable(key, value)...value()` y
  `applyMergePlan(plan)` en ambas fachadas (mismo plan, mismo wire).
- [x] TCK de merge en 3 transportes: `08-merge.yaml` +
  `merge-nested-append-prepend-metadata` (`posts.data` append,
  `posts.pinned` prepend, `config.theme` deep, `matchPropsOn posts.data.id`)
  + `merge-nested-reset-clears-child-path` (reset hijo selectivo);
  `TckSpringTest`, `TckQuarkusTest` (suite completa) y `TckReactiveTest`
  (subset + 2 casos) verdes.
- [x] Sin ruptura: `merge(key, value, rule, matchOn...)` conservado en `0.x`;
  sin cambio de semántica en metadata existente (scroll intent, deep merge,
  `X-Inertia-Reset` con poda de descendentes).
- [x] Casos: append+prepend en el mismo prop compuesto, matchOn simple y
  múltiple, merge/prepend/deep combinados, recarga parcial + reset de hija,
  serialización exacta de las 4 listas de metadata, interacción con
  deferred/once vía suites existentes (sin regresión TCK).

## Evidencia

- Core: `MergePlanUnitTest` 5/5 + `MergeableBuilderUnitTest` 2/2.
- Spring: `TckSpringTest` + `MergePropProcessorTest` verdes.
- Quarkus: `TckQuarkusTest` + `TckReactiveTest` + `MergePropsBuilderUnitTest` verdes.
- `verify:metadata` verde; matriz `62 rows, 60/62 verified`
  (PROTO-054B TESTED; E2E client en M4).

## Docs

`docs/props-guide.md` (DSL + reglas), `docs/migration.md` (M3),
matriz regenerada. `PROTO-054B` → `E2E_VERIFICADO` en M4.
