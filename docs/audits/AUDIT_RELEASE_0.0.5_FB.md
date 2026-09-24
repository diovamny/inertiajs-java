# Auditoría release-0.0.5 — Fase B arquetipos con inertia-core

- Rama: `release-0.0.5`. Fecha: 2026-09-24.
- Resultado: **VERDE.**

## Criterios 100% Fase B

- [x] Los 6 `archetype-resources/pom.xml` declaran `io.github.diovamny.inertia:inertia-core`
  explícito con `inertia-core.version = ${inertiaAdapterVersion}` (misma versión
  que el adapter, sin deriva transitiva). Verificado por `check-versions.mjs` §3b
  (nuevo, bloqueante en `verify:metadata`).
- [x] `npm.version` de plantillas `10.9.2` → `11.17.0` (coherente con `engines`).
- [x] CI `archetypes`: matriz con `starter-kind`/`starter-jar` + paso
  boot-smoke (package, boot, `id="app"`, `"component":"Welcome"`, log en fallo).
- [x] Prueba local de fuego (spring-vue): generate ✓, `.npmrc` generado ✓,
  `inertia-core@0.0.5` en pom ✓, `@inertiajs/vue3` `3.7.1` ✓, `mvn -B test`
  2/2 ✓, boot ✓, HTML shell v3-pura ✓, JSON `Welcome` ✓.
- [x] Hallazgo corregido: starters Spring no eran ejecutables (faltaba
  `repackage` en las 3 plantillas) — añadida ejecución `repackage-starter`.
- [x] `docs/archetype-certification.md` con la matriz 6 starters.
- [x] `verify:metadata` verde.

## Notas

- PowerShell local: citar todos los `-D` (`"-Dkey=value"`); sin comillas el
  plugin archetype trunca coordenadas (`io:...:0`). Solo afecta a la
  invocación manual en Windows; CI usa bash.
- El catálogo local de arquetipos vive en `~/.m2/archetype-catalog.xml`
  (no en `repository/`); si falta, copiarlo tras `install -P archetypes`.
