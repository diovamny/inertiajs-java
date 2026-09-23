# Auditoría release-0.0.5 — M0 Verdad pública (P100-01)

- Rama: `release-0.0.5` (commit auditado: ver `git log` al pie).
- Fecha: 2026-09-23. Alcance: solo cliente Inertia v3, 3 transportes estables.
- Resultado: **VERDE — puede continuar a M1.**

## Criterios 100% M0 (todos cumplidos)

- [x] Cero menciones de soporte `2.x` sin E2E-v2: `README.md` dice `3.x only`,
  `COMPATIBILITY_POLICY.md §1` prohíbe modo legacy, `NOT_SUPPORTED.md` y
  `docs-site/` actualizados. Verificado con `grep 2.x` solo en historial
  `CHANGELOG 0.0.4` y notas de ruptura (no como soporte).
- [x] Sin sobreafirmaciones: `PROTO-053A` (bags, TESTED) vs `PROTO-053B`
  (multi-mensaje, PARCIAL); `PROTO-054A` (wire, IMPLEMENTED) vs `PROTO-054B`
  (append explícito, PARCIAL); `PROTO-057A` (serialización, TESTED) vs
  `PROTO-057B` (comportamiento cliente, IMPLEMENTADO). Reactive como objetivo
  estable, sin etiqueta experimental al cierre de 0.0.5.
- [x] Toda fila con estado explícito + fuente oficial o justificación:
  `specs/inertia-v3-compliance.yaml` (62 filas, `updated: 2026-09-23`),
  snapshot congelado `specs/inertia-v3-baseline-2026-09-23.yaml`, política
  `docs/COMPATIBILITY_POLICY.md` con escalera y proceso de actualización.
- [x] Lector externo distingue contrato/E2E/release en <5 min:
  `README.md` enlaza matriz generada + política + NOT_SUPPORTED sin cifras
  de certificación; `docs-site/compatibility.md` sin `58/59`.

## Evidencia reproducible (M0)

- `node scripts/generate-compatibility-matrix.mjs` → `Matrix generated:
  62 rows, 58/62 verified, 5 E2E.` (honesto: 3 filas PARCIAL/IMPLEMENTADO
  abiertas para M2/M3/M4).
- `node scripts/check-conformance-matrix.mjs` → `117 test references resolved`.
- `node scripts/check-versions.mjs` → `all sources agree on 0.0.5`.
- `docs/protocol-compatibility.md` regenerado (no editado a mano).

## Documentación actualizada (M0)

`docs/COMPATIBILITY_POLICY.md` (nuevo), `specs/inertia-v3-baseline-*.yaml`
(nuevo), `specs/inertia-v3-compliance.yaml`, `docs/protocol-compatibility.md`
(generado), `README.md`, `docs/NOT_SUPPORTED.md`, `ROADMAP.md`,
`CHANGELOG.md` (`## 0.0.5`), `docs/migration.md`, `docs/getting-started-*.md`,
`docs-site/{index,compatibility,changelog,migration,not-supported,guide/*,features/ssr}.md`.

## Límites declarados (no se ocultan)

- `PROTO-053B/054B` PARCIAL y `PROTO-057B` IMPLEMENTADO: cierran en M2/M3/M4.
- E2E Reactive (3 celdas) y shareProps observada: cierran en M4/M7.
- PIT/SCA fail-closed + SBOM por release: cierran en M8.
