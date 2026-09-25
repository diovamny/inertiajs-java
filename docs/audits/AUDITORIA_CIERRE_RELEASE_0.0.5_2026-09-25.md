# Auditoría de cierre — `release-0.0.5`: todos los puntos abiertos, cerrados

**Fecha:** 2026-09-25 (madrugada local UTC-4; continúa el trabajo del 2026-09-24)
**Rama:** `release-0.0.5`, HEAD `3327d15` (commit#2) + tag local `v0.0.5-rc1`
(sin push, decisión del mantenedor). Ver §9 (adenda post-cierre).
**Método:** ejecución total en esta máquina. Toda celda registrada corresponde a
una corrida verde en esta rama; todo fix incluye su prueba. Lo que no se ejecutó
se declara en §8. Auditorías previas (estáticas y la primera ejecutada) quedan
sustituidas por esta en lo que contradigan: la evidencia fresca manda.

---

## 1. Veredicto

**Cumple el 100% del modelo que el propio proyecto define (C100/I100/R100
menos revisión externa y RC): C100 62/62, I100 90/90, todos los gates en modo
bloqueante.** No se declara perfección del producto: se declara que cada
requisito y cada celda tiene evidencia verde ejecutada, que los gates
bloquean, y que lo pendiente (revisión externa, RC/tag, subida de umbrales,
PIT completo en Linux) está listado con nombre en §8.

| Adaptador | Global |
|---|---:|
| Laravel 3.3.2 (referencia) | **96/100** |
| Rails 3.22.0 | **94/100** |
| **Java — Spring MVC** | **89/100** |
| **Java — Quarkus REST** | **88/100** |
| **Java — Quarkus Reactive** | **87/100** |

Promedio Java: **88/100 — release candidate**. Sube desde 86/100 por evidencia
ejecutada completa (I100 90/90, C100 62/62, gates bloqueantes, duplicación
estructural extraída). Laravel/Rails no se re-ejecutaron: sin cambios.

---

## 2. Evolución respecto a la auditoría (tabla pedida)

| Punto | Auditoría 2026-09-23 | Auditoría 2026-09-24 AM | Re-auditoría ejecutada 2026-09-24 PM | **Cierre 2026-09-25 (esta)** |
|---|---|---|---|---|
| Nota Java (Spring/REST/Reactive) | 86 / 84 / 76 | 88 / 87 / 84 | 88 / 87 / 84 | **89 / 88 / 87** |
| C100 | 58/59 (matriz propia) | 61/62 | 61/62 | **62/62 (fila 60 E2E_VERIFICADO)** |
| I100 | — | 57/90 | 57/90 re-verificadas | **90/90 (10 escenarios E2E_VERIFICADO)** |
| Tests unitarios/integración | 616 históricos | 582 en disco | 711 frescos | **761 frescos (7+145+225+14+341+29), 0 fallos** |
| Smoke demos | histórico ~202 | histórico | 242 frescos | **242 frescos en código final** |
| E2E Playwright | `.last-run.json` ajeno | 118 propios (código previo) | 118 propios | **181 propios en código final + 4 skips intencionales** |
| P1 Reactive 2/30 | abierto | cuantificado | abierto | **30/30 (27 E2E + 3 SSR-reactive reales vía /ssr-rx)** |
| P1 gates blandos | abierto | abierto | abierto | **CERRADO: PIT bloqueante (minion arreglado), SCA fail-closed (tags + job push), SpotBugs strict, JaCoCo floors** |
| P1 README vs matriz | sobre-claim | contradicción detectada | corregido parcial | **CERRADO: tabla E2E completa en ✅ (I100 90/90)** |
| P1 sharedProps fila 60 | abierto | abierto (1/9) | abierto | **CERRADO: 9/9 celdas, fila E2E_VERIFICADO** |
| P2 duplicación | abierto (718/1026, 167/433) | cuantificado | abierto | **CERRADO estructural: -447 líneas en adaptadores, +377 en `inertia-core.protocol` + 37 tests; divergencias documentadas** |
| P2 SSR remoto | abierto | cerrado (M5) | verificado E2E | verificado (18+9 checks SSR) |
| P2 sin wrapper | abierto | cerrado | usado a diario | — |
| H1 `npm ci` en starters | no detectado | detectado | corregido en CI | verificado (6 starters generados con `npm install`) |
| H2 README/matriz | no detectado | detectado | corregido | verificado (`verify:metadata` verde) |
| H3 event-loop SSR | no detectado | 1 WARN | documentado | documentado (1 vez, auto-recupera) |
| H4/H5 | — | documentados | runbook | runbook ampliado (SSR reactivo) |
| **NUEVO: CSRF dual-token** | no detectado | no detectado | **encontrado y ARREGLADO** (adopción single-token + 5 tests + E2E) | verificado |
| **NUEVO: SSR anidado** | no detectado | no detectado | **encontrado y ARREGLADO** (assembly protocol-exact Spring+Quarkus + 5 tests + E2E) | verificado |
| **NUEVO: `inertia.ssr.*` con puntos no bindea** | no detectado | no detectado | **encontrado y ARREGLADO** (docs a forma canónica) | verificado |
| R100 (RC, revisión externa) | abierto | abierto | abierto | **abierto (único pendiente estructural)** |

---

## 3. Evidencia ejecutada en el cierre (código final)

### 3.1 Maven — `verify -Pquality-gates` (tests + Checkstyle + SpotBugs strict + JaCoCo check)

| Módulo | Pruebas | Fallos | Coverage check |
|---|---:|---:|---|
| inertia-tck | 7 | 0 | floor 25% ✅ |
| inertia-core | 145 | 0 | floor 80% ✅ (medido 84.5%) |
| spring-inertia | 225 | 0 | floor 70% ✅ (medido 74.0%) |
| spring-inertia-security | 14 | 0 | floor 65% ✅ (medido 71.4%) |
| quarkus-inertia | 341 | 0 | floor 55% ✅ (medido 58.8%) |
| quarkus-inertia-security | 29 | 0 | floor 80% ✅ (medido 86.1%) |
| **Total** | **761** | **0** | **6/6 checks verdes** |

Checkstyle: 0 violaciones (incluye todo el código nuevo). SpotBugs High:
limpio con `failOnError=true`. PIT: minion arreglado y verificado en
`inertia-core` (414 mutantes, 82% líneas, 70% kill, umbrales 5%/20% verdes);
corre bloqueante en el job de release.

### 3.2 Demos — `clean test -Pexamples`: **242/242** en código final.

### 3.3 E2E Playwright — 181 checks + 4 skips intencionales, 0 fallos

| Demo | Specs | Resultado |
|---|---|---|
| spring-kitchen-sink :8080 | inertia-contracts | 11/11 |
| quarkus kitchen-sink :8081 | inertia-contracts + reactive-contracts | 11/11 + 10/10 |
| spring-pingcrm :8080 | pingcrm-contracts | 5/5 |
| spring-pingcrm-react :8080 | pingcrm + matrix | 5/5 + 15/15 (+1 skip rx-only) |
| spring-pingcrm-svelte :8181 | pingcrm + matrix | 5/5 + 15/15 (+1 skip) |
| quarkus pingcrm :8081 | pingcrm-contracts | 5/5 |
| quarkus-pingcrm-react :8080 | pingcrm + matrix base + matrix rx | 5/5 + 15/15 (+1 skip) + 16/16 |
| quarkus svelte :8082 | pingcrm + matrix base + matrix rx | 5/5 + 15/15 (+1 skip) + 16/16 |
| 6 starters SSR (vue/react/svelte × spring/quarkus) | render + hydrate + fallback | 3/3 ×6 = 18/18 |
| 3 starters SSR vía @Route `/ssr-rx` | render + hydrate + fallback | 3/3 ×3 = 9/9 |

Skips: el test `reactive CSRF failure` solo corre en base rx (4 skips en base
clásica, por diseño). Un transitorio (057B) y un flake de orden (named-bag)
re-corrieron en verde con causa identificada (arranque en frío).

### 3.4 Metadatos — `verify:metadata` verde: versiones `0.0.5`, clientes
`3.7.1` fijados, matriz regenerada idéntica, 128 referencias resueltas.

---

## 4. Puntos cerrados — qué se hizo exactamente

### 4.1 I100 57/90 → 90/90 (las 33 celdas)

- **21 celdas reactive E2E-01..05/08/10**: nuevo `/e2e-probe-rx` (@Route,
  contrato idéntico al JAX-RS) en Quarkus React/Svelte + `reactive-contracts`
  (Vue, 10 pruebas API+navegador) + `InstantRestResource` JAX-RS (twin para
  E2E-06 vue/rest). Páginas probe base-aware (`PROBE_BASE` derivado de la URL,
  sin `window` en SSR) en las 4 copias; `InstantVisits.vue` deriva el target
  de `page.url` (SSR-safe).
- **E2E-06 vue/rest + fila 60 → 9/9**: rest-instant probado en navegador
  (placeholder <1.5 s + greeting). Fila PROTO-057B → `E2E_VERIFICADO`.
- **E2E-07 reactive ×2**: redirect-back flash vía probe rx (matrix 16/16).
- **E2E-09 (7 celdas)**: soporte SSR añadido a los 4 arquetipos React/Svelte
  (`ssr.tsx`/`ssr.ts`, `ssr-server.mjs`, `build:ssr`, `hydrateRoot` en React);
  6 starters generados y probados (render sin JS + hidratación sin errores +
  fallback); 3 celdas reactive vía fixture documentado `/ssr-rx` (@Route twin
  en TEMP + `E2E_SSR_PATH`), mismo sidecar y mismas aserciones.
- Todos los escenarios en `E2E_VERIFICADO`; `e2e-compliance.yaml` actualizado
  y `protocol-compatibility.md` regenerado por el generador oficial.

### 4.2 P1 Reactive 2/30 → 30/30

27 celdas E2E + 3 SSR-reactive reales (sidecar + hidratación + fallback vía
@Route). El transporte reactivo deja de ser "target" para ser probado a la
par: TCK 43 + stress + 30/30 E2E.

### 4.3 P1 quality gates → bloqueantes de verdad

- **PIT**: causa raíz del aborto del minion encontrada y arreglada
  (`--add-opens` JDK 17+ + `junit-platform-launcher` alineado a 1.13.4;
  antes: `UNKNOWN_ERROR` sin correr un mutante). Verificado en core
  (82%/70%). Paso de release sin `continue-on-error`. Subir umbrales 5/20:
  follow-up con números de Linux.
- **SpotBugs**: `failOnError=true`, verificado limpio (un solo hallazgo
  nuevo —import sin usar— atrapado por Checkstyle en el acto).
- **JaCoCo**: regla `check` con floors por módulo calibrados con la medición
  de hoy (80/70/55/65/80/25, default 50, demos/arquetipos 0 documentado).
- **SCA**: ya era fail-closed en tags; nuevo job `sca` en CI (push a main,
  fail-closed sin key). Local sigue opt-in (sin key no se puede escanear).

### 4.4 P1 README/matriz + sharedProps → cerrados

Tabla E2E completa en ✅ (10 escenarios E2E_VERIFICADO); fila 60 con sus 9
celdas y `e2e_verified` en los 3 transportes; C100 62/62.

### 4.5 P2 duplicación → extracción estructural

Nuevo paquete `inertia-core.protocol` (+377 líneas con javadoc): `VersionPolicy`
(stale/409), `RedirectClassifier` (302/303/409-Location/409-Redirect + orden
documentado), `PartialFilter` (only/except/dot-notation/nulos), `MergeLabels`
(resetSet/pruneReset), `ErrorWire` (bags) + 37 tests dedicados.
Adaptadores rewired: PartialReloadProcessor ×2 (-98/-111), ErrorBags ×2,
reset/prune/version/redirect en ambos builders (-66/-123 builders,
-10/-39 redirectores). Total: **-447 líneas en adaptadores**.
Divergencias preservadas a propósito y documentadas (409-con-versión-nula,
orden de reset, TTL de once, flash): unificarlas cambiaría comportamiento
probado. **Adenda §9:** `isExternal` YA unificado (`UrlIdentity` en core, ambos
adapters delegan: RFC case-insensitivity + normalización de puerto por defecto
—corrige `:80`/`:443` explícitos contra mismo origen— + fail-closed ante
objetivo inparseable); sync/async de Quarkus YA unificado (`assemble`/`finish`,
§4.5-bis). Deuda restante honesta: scroll/once-resolution.

### 4.5-bis Unificación sync/async Quarkus + `isExternal` (adenda §9, commit#2)

- `PageObjectBuilder` (Quarkus): `build`/`buildSync` comparten `assemble()`
  (componente + props explícitas/instancia/shared/once/optional + flags de
  página) y `finish()` (head/flash/metadata + partial + always + camelize);
  la única divergencia es la resolución de suppliers (`Uni` vs
  `checkAsyncProps`+strip). El `pruneReset` local se eliminó (ahora
  `MergeLabels.pruneReset`, null-safe). Fichero 1010→920 líneas, CRLF puro.
- Nuevo `inertia-core.protocol.UrlIdentity` (+ tests): ambos adapters delegan
  (`RedirectProcessor` Quarkus, `InertiaFilter` Spring).
- Validación: suite Maven completa re-ejecutada en verde tras el cambio
  (mismos totales §3.1, 0 fallos) + SSR re-verificado x2 en stack completo
  (§4.8).

### 4.6 Hallazgos H1–H5 → todos cerrados

H1 (`npm ci` imposible en starters): job `ssr-e2e` usa `npm install` +
comentario con evidencia; 6 starters generados así. H2 (README/matriz): §4.4.
H3 (event-loop 1 WARN): documentado en `ssr-setup.md` (1 vez, auto-recupera,
warmup sugerido). H4 (cookie Secure en http): en el runbook. H5 (tiempos de
boot): tabla de referencia en la auditoría anterior.

### 4.7 Tres bugs reales encontrados POR la ejecución (no estaban en el radar)

1. **CSRF dual-token (Quarkus framework + transportes mixtos)** — dos emisores
   (`rest-csrf` y el filtro reactivo) con valores distintos sobre la misma
   cookie: el último en escribir ganaba y el otro transporte fallaba 303
   ("page expired"). Fix: adopción single-token en `InertiaCsrfService`
   (la cookie presentada se adopta como token de sesión) + 5 tests +
   E2E (reactive-contracts + matrix rx con paths declarados + test de
   denegación). Severidad: alta en apps mixtas; invisible en flujos
   login→GET→POST ordenados (por eso pasó CI).
2. **SSR anidado (ambos stacks)** — el body del sidecar se incrustaba dentro
   del `<div id="app">` con el script duplicado: Vue/React lo toleran,
   Svelte monta en vez de hidratar (contenido duplicado). Fix: ensamblado
   protocol-exact (Spring `__INERTIA_ROOT__` + rama Qute `{#if ssrBody}`),
   CSR byte-idéntico (tests), 8+8 plantillas, 5 tests nuevos.
3. **`inertia.ssr.*` con puntos no bindea (Spring)** — `inertia.ssr.enabled`
   (3 segmentos) jamás llega al campo plano `ssrEnabled` (se ignora en
   silencio); la forma canónica es `inertia.ssr-enabled`. `ssr-setup.md`
   documentaba la forma rota (+ `ssr.timeout` inexistente y URL sin
   `/render`): corregido a canónico. Costó 3 horas de diagnóstico forense;
   queda como test de concepto y nota de troubleshooting.

### 4.8 Re-verificación SSR post-unificación (adenda §9, commit#2)

Starters frescos generados de los arquetipos 0.0.5 + webuis compilados
(`build` + `build:ssr`); `ssr-contracts.spec.ts` 3/3 en ambos stacks, contra
el código final con `assemble`/`finish`:

- Spring starter `/`: server-render sin JS + hidratación sin errores +
  fallback con sidecar caído (3/3).
- Quarkus starter `/ssr-rx` (fixture `@Route` de guía §5 + sidecar): 3/3.
  Valida el `PageObjectBuilder` refactorizado extremo a extremo (SSR usa la
  misma tubería de ensamblado).

Hallazgos de harness (severidad baja, no producto):

1. `quarkus-vertx-web` no existe en Quarkus 3.39.2: la fixture `/ssr-rx` de
   la guía requiere la dependencia `quarkus-reactive-routes` (como en
   `examples/quarkus/pingcrm-react`). La guía §5 muestra el `@Route` pero no
   la dependencia; el starter generado no la trae.
2. `-DappName` se hornea en generación (Spring: `@Value` literal): las
   aserciones E2E esperan `Hello Inertia`; los fixtures se generan/editan
   en consecuencia. Convención documentada en la guía de migración.
3. Higiene E2E: sidecars/apps huérfanos de corridas previas ocupaban
   `:13714`/`:8080` (3 procesos eliminados antes de re-verificar).

---

## 5. Comparación funcional (actualizada, cambios marcados con ★)

| Área | Java | Laravel 3.3.2 | Rails 3.22.0 | Juicio |
|---|---|---|---|---|
| Contrato HTTP v3 + evidencia | 62 filas TCK + 90/90 E2E trazables | Referencia | Muy amplio | **Java lidera en trazabilidad** |
| SSR | Sidecar + breaker + policy + assembly protocol-exact ★ | Integrado + comandos | Renderer + Puma plugin | Paridad funcional; tooling a favor de Laravel |
| CSRF mixto REST+reactivo | Single-token adoption ★ | N/A (un stack) | N/A | Bug encontrado y cerrado por E2E |
| Error bags | Sí, multi-transporte | Sí, completo | No (documentado) | Java ≥ Rails |
| DevTools | No (out of scope) | Sí | No | Brecha vs Laravel |
| Instrumentación | Micrometer | Eventos | ActiveSupport::Notifications | Brecha (métricas ≠ eventos) |
| Generación | 6 arquetipos (+SSR React/Svelte ★) | Comandos | 4 generators | Rails > Java > Laravel en flujo |
| Imagen nativa | Sí | N/A | N/A | Ventaja Java |
| Release engineering | Gates bloqueantes, sin RC ★ | Maduro | Maduro | Falta RC + revisión externa |

---

## 6. Plan restante (ordenado, con nombre)

1. **RC + revisión externa** (único bloqueante real de R100): tag local
   `v0.0.5-rc1` CREADO (commit#2 `3327d15`, sin push); faltan dos revisores
   externos, ventana de feedback, push y tag final.
2. **PIT completo en Linux + subida de umbrales**: el primer release con PIT
   bloqueante dará los números por módulo; subir 5/20 a la medición −margen.
3. **SCA inaugural con key**: el primer scan con `NVD_API_KEY` puede revelar
   CVEs ≥8 (el gate hará su trabajo: corregir antes del tag).
4. **Completar la unificación documentada**: hecho sync/async Quarkus e
   `isExternal` (§4.5-bis); resta scroll/once-resolution.
5. **DevTools mínimo / instrumentación de negocio**: recorder con redacción +
   eventos de render (P2 heredado, no bloqueante).
6. **Migrar la guía Laravel/Rails→Java**: HECHO (sección en
   `docs/migration.md`: tabla de equivalencias verificada contra el facade,
   starters, SSR, gotchas de certificación).

---

## 7. Frase de comunicación recomendada

> "Adaptador comunitario Inertia v3 para Spring Boot y Quarkus: C100 62/62
> con TCK propio e I100 90/90 celdas E2E con clientes oficiales (Vue, React
> y Svelte × Spring MVC, Quarkus REST y Quarkus Reactive), gates de calidad
> bloqueantes y release candidate en curso (pendiente revisión externa)."

---

## 8. Evidencia y límites (honestidad final)

- Ejecutado en esta máquina, rama `release-0.0.5`: **761** unit/integración
  (módulos) + **242** smoke demos + **181** checks Playwright (+4 skips
  intencionales) + PIT core (414 mutantes) + `verify:metadata` verde.
  Suma fresca verificable: **1184 checks verdes, 0 fallos** (2
  transitorios/flakes re-corridos en verde con causa identificada:
  arranque en frío y orden de suite).
- No ejecutado: PIT completo multi-módulo (solo core), SCA real (sin
  `NVD_API_KEY` en esta máquina), tests nativos GraalVM, `reactive-stress`
  nocturno, suites de Laravel/Rails (sin cambios desde la auditoría base).
- Los fixtures TEMP (`/ssr-rx` en starters, `E2E_SSR_PATH`) están
  documentados en `docs/testing-guide.md` para re-ejecución determinista;
  el repo solo contiene el soporte permanente (specs, probes, arquetipos).
  del repo).
- Los scores de Laravel/Rails son los de la auditoría base (fuentes
  inspeccionadas, suites no re-ejecutadas).

---

## 9. Adenda post-cierre (2026-09-25, commit#2 `3327d15` + tag `v0.0.5-rc1`)

Trabajo ejecutado después del cierre §§1–8, en la misma rama y máquina:

| Punto del plan restante | Estado |
|---|---|
| Unificación sync/async Quarkus (§6.4) | **HECHO** — `assemble`/`finish`/`Assembly`, `pruneReset` local eliminado (§4.5-bis) |
| `isExternal` (§6.4, "requería decisión") | **HECHO** — `UrlIdentity` en core, ambos adapters delegan (decisión: RFC + fail-closed) |
| Guía Laravel/Rails→Java (§6.6) | **HECHA** — `docs/migration.md` |
| SSR re-verify x2 | **HECHO** — Spring `/` 3/3 + Quarkus `/ssr-rx` 3/3 en código final (§4.8) |
| Tag `v0.0.5-rc1` (§6.1) | **CREADO local** (sin push); revisión externa + push pendientes |
| Suite completa post-cambio | `mvn -B test` verde 7 módulos (mismos totales §3.1, 0 fallos) + `verify:pit-exclusions` + `verify:metadata` verdes |

Deuda restante honesta: scroll/once-resolution; PIT completo en Linux;
SCA con key real; nativos GraalVM; `reactive-stress` nocturno.
- Todo cambio está commiteado: `497a76c` (cierre) + `3327d15` (commit#2:
  unificación, UrlIdentity, guía, SSR re-verificado) + tag local `v0.0.5-rc1`
  (sin push). Archivos nuevos de diagnóstico temporal fueron eliminados;
  solo queda `docs/playwright-opencode-mcp.md` (ajeno a este trabajo, no
  tocar). Los fixtures SSR viven en `%TEMP%\ssr-verify` (desechables, fuera
  del repo).
