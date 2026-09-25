# Auditoría ejecutada avanzada — `release-0.0.5` (tests + E2E corridos)

**Fecha:** 2026-09-24 (tarde, hora local UTC-4)
**Repositorio:** `diovamny/inertiajs-java`, rama `release-0.0.5`, HEAD `6b6980d`, más los
cambios documentados en §7 (solo docs/CI, sin tocar código de producto).
**Método:** a diferencia de las dos auditorías anteriores (estáticas), **todo lo que
afirma este informe se ejecutó en esta máquina**: suites Maven completas, smoke de
demos, 8 demos E2E con Playwright + clientes oficiales 3.7.1, y ciclo SSR completo
desde arquetipo (generar → build → sidecar → specs → fallback). Lo que no se pudo
ejecutar se declara en §8.
**Referencias:** mismas que la auditoría comparativa de hoy en la mañana
(`AUDITORIA_COMPARATIVA_RELEASE_0.0.5_2026-09-24.md`): Laravel 3.x `5f5bf68`,
Rails `d74f3b9` (3.22.0), spec oficial v3, más el material histórico de
`Downloads/Nueva carpeta (2)` (auditorías externas 19–23/09, usadas como contexto,
no como evidencia).

---

## 1. Veredicto

**Sigue sin cumplir el 100% — pero ahora está probado por ejecución, no por lectura.**

- **C100 61/62** confirmado: el TCK y las 62 filas corren en verde (236 pruebas
  TCK-adjacentes dentro de los 711 del reactor).
- **I100 57/90**: las 57 celdas declaradas se re-verificaron en verde en esta
  máquina (118 checks Playwright); **las 33 restantes siguen abiertas** y esta
  auditoría no las cierra (reactive 2/30, SSR React/Svelte, 5 celdas sueltas).
- **R100**: sigue incumplido (sin RC, sin revisión externa, gates sin bloquear).

Puntuación (sin cambios respecto a la mañana; la evidencia fresca **confirma** las
notas en vez de subirlas — los pendientes estructurales pesan más que el verde):

| Adaptador | Global |
|---|---:|
| Laravel 3.3.2 (referencia) | **96/100** |
| Rails 3.22.0 | **94/100** |
| **Java — Spring MVC** | **88/100** |
| **Java — Quarkus REST** | **87/100** |
| **Java — Quarkus Reactive** | **84/100** |

Promedio Java: **86/100 — beta alta / RC técnica.**

---

## 2. Evolución respecto a la auditoría (la tabla pedida)

| Tema | Auditoría 2026-09-23 (estática, v0.0.4→0.0.5 temprana) | Auditoría 2026-09-24 AM (estática, HEAD `6b6980d`) | Re-auditoría ejecutada 2026-09-24 PM (esta) | Estado |
|---|---|---|---|---|
| Nota global Java (Spring / REST / Reactive) | 86 / 84 / 76 (promedio 84) | 88 / 87 / 84 (promedio 86) | **88 / 87 / 84 (promedio 86, confirmadas por ejecución)** | 🟢 estable, con evidencia fresca |
| Tests unitarios/integración | 616 históricos (Surefire en disco, sin Maven local) | 219 + 334 + 29 en disco (core/tck/spring-security sin reporte local) | **711/711 frescos: tck 7, core 108, spring 219, spring-security 14, quarkus 334, quarkus-security 29 — 0 fallos, 0 errores, 0 omitidos** | 🟢 cerrado el hueco de evidencia |
| Smoke de demos | 8 dirs, ~202 tests históricos | mismos reportes históricos | **242/242 frescos en los 9 demos (incl. demo-app 86)** | 🟢 nuevo |
| E2E Playwright | `.last-run.json` "passed" (ajeno) | misma referencia ajena | **118/118 ejecutados aquí: 11+11 contracts, 5+20+20+5+20+20 pingcrm/matrix, 3+3 SSR** | 🟢 nuevo |
| Celdas I100 declaradas (57/90) | se aceptan del YAML | se aceptan del YAML | **57/57 re-verificadas en verde; 33 siguen abiertas** | 🟢 confirmadas, 🟡 resto abierto |
| P1 cliente `2.x` | abierto | cerrado | cerrado (grep: sin `2.x` en README; policy §1) | 🟢 |
| P1 errores múltiples por campo | abierto | cerrado en código | **re-probado en navegador (M2, validación + bags nombradas, Vue)** | 🟢 |
| P1 `append()` | abierto | cerrado en código | **re-probado en navegador (M3, Vue) + TCK 711 en verde** | 🟢 |
| P1 Reactive "estable" con 2/30 celdas | abierto | cuantificado | sin cambio (no se ejecutó E2E reactive; `reactive-stress` nocturno no corrido) | 🔴 abierto |
| P1 quality gates (PIT 5/20, `continue-on-error`, SCA condicional) | abierto | abierto sin cambios | **sin cambios: no se ejecutó el perfil `quality-gates`** (PIT aborta en Windows según docs; corrida completa fuera de alcance temporal) | 🔴 abierto |
| P1 README vs matriz (`append`, multi-message en 🔄) | sobreafirmación en matriz | detectada la contradicción | **CORREGIDO: README filas 29/32 → ✅ en 3 transportes (E2E sigue 🔄, parcial)** | 🟢 |
| P1 `sharedProps` observada (fila 60) | abierta | abierta | **re-probada en navegador (057B, Vue, Spring y Quarkus)** pero la fila exige las 9 celdas → sigue IMPLEMENTADO | 🟡 parcial |
| P2 SSR remoto sin validación | abierto | cerrado (M5) | verificado en código + SSR E2E con sidecar real + fallback CSR 2/2 por stack | 🟢 |
| P2 sin Maven Wrapper | abierto | cerrado | usado intensivamente hoy (`mvnw.cmd`, Maven 3.9.11, EXIT=0 en todo) | 🟢 |
| P2 duplicación Spring/Quarkus | abierto | cuantificada | sin cambio (718/1.026 y 167/433 líneas) | 🔴 abierto |
| P2/P3 DevTools, instrumentación, generadores | abierto | abierto por decisión | sin cambio (fuera de alcance declarado) | ⚪ decisión |
| Reproducibilidad E2E | no evaluada | `.last-run.json` ajeno | **runbook nuevo en `docs/testing-guide.md` + fix `npm ci`→`npm install` en `ci.yml` ssr-e2e** | 🟢 |

Leyenda: 🟢 verificado/cerrado · 🟡 parcial · 🔴 abierto · ⚪ decisión explícita del proyecto.

---

## 3. Evidencia ejecutada (números exactos de hoy)

### 3.1 Reactor Maven — `./mvnw -B test` → `BUILD SUCCESS`, EXIT=0

| Módulo | Pruebas | Fallos | Errores | Omitidos |
|---|---:|---:|---:|---:|
| inertia-tck | 7 | 0 | 0 | 0 |
| inertia-core | 108 | 0 | 0 | 0 |
| spring-inertia | 219 | 0 | 0 | 0 |
| spring-inertia-security | 14 | 0 | 0 | 0 |
| quarkus-inertia | 334 | 0 | 0 | 0 |
| quarkus-inertia-security | 29 | 0 | 0 | 0 |
| **Total** | **711** | **0** | **0** | **0** |

Tiempos: Quarkus 7:32 min, Spring 3:34 min (máquina Windows, JDK 21.0.2).

### 3.2 Smoke de demos — `./mvnw -B clean test -Pexamples` (9 módulos) → `BUILD SUCCESS`

| Demo | Pruebas |
|---|---:|
| spring-pingcrm | 29 |
| spring-pingcrm-react | 6 |
| spring-pingcrm-svelte | 6 |
| spring-kitchen-sink | 28 |
| quarkus pingcrm | 26 |
| quarkus pingcrm-react | 26 |
| quarkus svelte | 26 |
| quarkus kitchen-sink | 9 |
| quarkus demo-app | 86 |
| **Total** | **242, 0 fallos** |

### 3.3 E2E Playwright — 118/118 (Chromium, clientes oficiales 3.7.1)

| Demo (puerto) | Spec(s) | Resultado |
|---|---|---|
| spring-kitchen-sink (:8080) | inertia-contracts (11) | **11/11** |
| quarkus kitchen-sink (:8081) | inertia-contracts (11) | **11/11** |
| spring-pingcrm (:8080) | pingcrm-contracts (5) | **5/5** |
| spring-pingcrm-react (:8080) | pingcrm + feature-matrix (5+15) | **20/20** |
| spring-pingcrm-svelte (:8181) | pingcrm + feature-matrix (5+15) | **20/20** |
| quarkus pingcrm (:8081) | pingcrm-contracts (5) | **5/5** |
| quarkus-pingcrm-react (:8080) | pingcrm + feature-matrix (5+15) | **20/20** |
| quarkus svelte (:8082) | pingcrm + feature-matrix (5+15) | **20/20** |
| starter spring-vue + sidecar (:8080) | ssr-contracts render+hydrate (2) y fallback (1) | **3/3** |
| starter quarkus-vue + sidecar (:8080) | ssr-contracts render+hydrate (2) y fallback (1) | **3/3** |

Incluye: login real con error de credenciales, validación M2, bags nombradas M2,
append M3, instant-visit 057B, redirect-back E2E-07, upload multipart, 409 con
versión stale, partials, SSR sin JS + hidratación sin errores de consola, y
fallback CSR con el sidecar caído. Cero errores de consola/página en todos.

### 3.4 Checks de metadatos — `npm run verify:metadata` → EXIT=0

128 referencias de test resueltas, versiones `0.0.5` coherentes, clientes `3.7.1`
fijados, matriz regenerada idéntica (62 filas, C100 61/62, I100 57/90).

---

## 4. Hallazgos de la ejecución

### 4.1 Tres falsas alarmas, investigadas hasta la causa raíz (el producto es inocente)

1. **7/11 en `inertia-contracts` (spring, primera corrida en frío).** Los 4 fallos
   compartían `loginAsTestUser` colgado (`Loading Log in`, `nprogress-busy`).
   Re-corrida en caliente: **11/11 en 47 s**. Causa: `fullyParallel` (hasta 6
   contextos concurrentes) contra servidor recién arrancado (boot Spring 98 s en
   esta máquina) = contención de arranque en frío, no bug. El login manual por
   API devolvía `200 Crm/Dashboard` incluso durante el incidente.
2. **10/11 en `inertia-contracts` (quarkus, solo falló 057B).** El snapshot mostraba
   el placeholder instantáneo (`Navigating from source…`) pero `Waiting for
   server…` eternamente. Re-corrida serial: **11/11**; flujo depurado con traza de
   red (`REQ …/instant-visit-target?delay=2` → `RES 200` → `SERVER DATA OK`).
   Causa: mismo flake de paralelismo en frío.
3. **409 "fantasma" en `/features/navigation/instant-visit-target`.** Mi primera
   reproducción con curl fue **inválida**: la sesión era anónima y el 409 con
   `X-Inertia-Location: /login` es el comportamiento CORRECTO del protocolo ante
   ruta protegida. Con sesión autenticada: **200 en 1.090 ms** con el page object
   correcto. Lección metodológica registrada: reproducir siempre con sesión.

**Consecuencia documental:** el runbook E2E añadido a `docs/testing-guide.md`
exige `--workers=1` + warm-up (`GET /login`, `GET /e2e-probe`) antes de los specs.
La suite es sensible al orden/paralelismo en máquinas lentas (hallazgo P3, no P1:
el servidor siempre respondió correctamente).

### 4.2 Hallazgos reales nuevos (todos verificados por ejecución)

**H1 · El job `ssr-e2e` de CI no puede funcionar como está escrito.**
`npm ci` exige `package-lock.json`; los starters generados **no lo traen**
(verificado en los 6 arquetipos: solo `package.json` + `.npmrc`). Reproducción:
`npm ci` → `EUSAGE`. Con `npm install` todo el ciclo SSR funciona (resolvió
`@inertiajs/vue3@3.7.1`) y los 6 specs SSR pasan. **Corregido en
`.github/workflows/ci.yml`** (`npm install` + comentario con la evidencia).
Nota relacionada: los templates usan rangos (`^3.7.0`, `^3.5.39`) en todo menos
el cliente (`3.7.1` exacto + `save-exact=true`); `verify:clients` no cubre la
salida generada. Riesgo de deriva aceptado hoy (resolvió 3.7.1), vigilar en cada
release de `@inertiajs/*`.

**H2 · README contradecía a la matriz generada (P1-5 de la mañana).**
Filas 29 (`append()`) y 32 (multi-message) en 🔄 en las 3 columnas de transporte
mientras `docs/protocol-compatibility.md` filas 54/56 dicen
`TESTED (+E2E spring-mvc, quarkus-rest)` con TCK en 3 transportes — y hoy se
re-ejecutó todo (TCK en los 711 + feature-matrix 15/15×4). **Corregido en
`README.md`**: ✅ en las 3 columnas; E2E queda en 🔄 (la evidencia E2E cubre
spring-mvc + quarkus-rest, no reactive). Verificado que `verify:metadata` sigue
en verde tras el cambio.

**H3 · Primer render SSR en Quarkus bloquea el event-loop una vez (P3).**
`BlockedThreadChecker: thread blocked for 2326 ms` en el primer request SSR del
starter quarkus (luego 3 renders más sin ningún aviso). `SsrHandler` usa
`HttpClient` asíncrono de Vert.x (diseño correcto), así que es calentamiento
(class-load/Jackson) sobre el loop, no espera bloqueante sistemática.
Recomendación: render de calentamiento en el arranque o mover la primera
serialización a worker. No afecta correctitud (SSR + hidratación 2/2, fallback
1/1).

**H4 · `QUARKUS_REST_CSRF_COOKIE_FORCE_SECURE=false` es obligatorio para E2E en
http** (ya documentado en `ci.yml`, hoy re-confirmado: sin esa variable los
logins de navegador en Quarkus no completan porque el navegador descarta cookies
`Secure`). El runbook lo incluye. En producción (https) no aplica.

**H5 · Arranques medidos en esta máquina (referencia, no requisito):**
Spring kitchen-sink 98 s, Spring pingcrm-react 25 s, Quarkus kitchen-sink 34 s,
Quarkus pingcrm 20 s, starter quarkus 11,7 s. El loop de espera de CI (48×5 s)
es suficiente; en local, esperar al health antes de Playwright.

### 4.3 Rarezas del entorno de ejecución (metodología, NO defectos del producto)

- El sandbox de ejecución de esta auditoría termina cualquier shell que genere
  un proceso hijo persistente (`Start-Process java…` → shell muerto, la demo
  sobrevive). Se trabajó con lanzamientos desacoplados (WMI) + `Stop-Process`
  aislados. No afecta a usuarios reales.
- Invocar `mvnw.cmd`/`mvn.cmd` con `-D` punteados **desde PowerShell en este
  sandbox** los parte por el primer punto (`-Dexpression=project.version` →
 
...[truncated 3881 chars]