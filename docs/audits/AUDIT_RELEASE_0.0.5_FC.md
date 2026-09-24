# Auditoría release-0.0.5 — Fase C demos certificadas (9/9)

- Rama: `release-0.0.5`. Fecha: 2026-09-24.
- Resultado: **VERDE.** Ver `docs/demo-certification.md` (matriz 9/9).

## Criterios 100% Fase C

- [x] `spring-pingcrm-react` tiene webui React 3.7.1 completo (portado de
  `quarkus/pingcrm-react`: `app.tsx`, Pages, Shared, `tsconfig.json`,
  `package.json` + lock 3.7.1, `.npmrc`, `vite.config.ts` estilo Spring,
  `templates/index.html`): `vite build` verde (app.js + app.css + manifest).
- [x] Módulos registrados en el reactor (`spring-pingcrm-react`,
  `spring-pingcrm-svelte` en `examples/spring/pom.xml`); pines
  `inertia.version=0.0.5` alineados + `check-versions.mjs` §4 extendido.
- [x] Hallazgos corregidos: `repackage` ausente (jar no ejecutable),
  BOM Spring ausente (`ClassNotFoundException: net.bytebuddy` en boot).
- [x] Certificación visit por demo (jar empaquetado, boot, shell v3 +
  JSON con componente esperado): 9/9 verde (ver matriz en
  `docs/demo-certification.md`).
- [x] CI: job `demo-visit` (matriz 9 demos, boot con CWD = módulo, asserts
  shell + JSON, log en fallo) + ejemplos `test` ampliados a react/svelte/
  demo-app.
- [x] `verify:metadata` verde (incluye nuevo lock + manifest react).

## Regla operativa (causa raíz documentada)

Arrancar cada demo con CWD = su módulo: las H2 file (`./data/…`) e `images/`
son relativas al CWD y Spring/Quarkus PingCRM comparten el nombre
`./data/pingcrm` — arrancar ambas desde la raíz corrompe el fichero
(`Wrong user name or password`). Residuos `data/*.mv.db` en la raíz están
ignorados por git y no son fuentes.
