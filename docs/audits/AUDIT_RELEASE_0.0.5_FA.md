# Auditoría release-0.0.5 — Fase A clientes oficiales 3.7.1 pineados

- Rama: `release-0.0.5`. Fecha: 2026-09-24.
- Resultado: **VERDE.**

## Criterios 100% Fase A

- [x] Los 14 `package.json` (6 plantillas de arquetipo + 8 demos con webui)
  declaran exactamente `3.7.1` para `@inertiajs/vue3|react|svelte` (sin `^`).
- [x] Los 8 `package-lock.json` resuelven `3.7.1` (verificado por script;
  `spring-pingcrm-svelte` no tenía lock: creado).
- [x] Gate permanente `scripts/check-client-versions.mjs` cableado en
  `verify:metadata` y en el job `unit-and-quality` de CI: falla ante
  cualquier deriva.
- [x] `.npmrc` con `save-exact=true` en los 14 webuis (plantillas incluidas
  vía fileSet `src/main/webui/**`; se verifica generando en Fase B).
- [x] Snapshot v3.7.1: notas del release oficial revisadas (sin cambios de
  wire; #3253 escape-`<` ya cubierto + test explícito
  `escapesLeadingLessThanPerInertia371`); README dice `3.7.1 (pinned)`.
- [x] `npm run verify:metadata` verde (versiones + clientes + matriz + conformancia).

## Notas

- `spring-pingcrm-react` no tiene webui: fuera del alcance de esta fase,
  se crea en Fase C.
- `@inertiajs/vite` (tooling de build) mantiene su rango: no es contrato
  de runtime y queda registrado aquí como decisión explícita.
