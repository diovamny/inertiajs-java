# Auditoría comparativa — Inertia.js Java `release-0.0.5` vs adaptadores oficiales

**Fecha:** 2026-09-24
**Repositorio auditado:** `diovamny/inertiajs-java`, rama `release-0.0.5`, HEAD `6b6980d` (2026-09-24), árbol limpio.
**Referencias locales:**
- `inertiajs/inertia-laravel` rama `3.x`, HEAD `5f5bf68` (v3.3.2, 2026-09-02).
- `inertiajs/inertia-rails` rama `master`, HEAD `d74f3b9` (v3.22.0, 2026-08-15).
**Estándar:** [documentación y protocolo Inertia v3](https://inertiajs.com/docs/v3/core-concepts/the-protocol),
contrastado con las implementaciones locales de Laravel y Rails y con el cliente oficial `@inertiajs/*` **3.7.1**.
**Método:** auditoría estática de los tres checkouts + ejecución de los chequeos de metadatos del propio repo +
uso de la evidencia de pruebas ya existente (Surefire, Playwright). No se modificó ninguna fuente; no se ejecutaron suites completas.

---

## 1. Veredicto

### ¿Cumple el 100% de inertiajs.com?

**No.** Y esta vez la respuesta es más matizada que en la auditoría de 2026-09-23: **la brecha ya no está en el protocolo, está en la evidencia de interop y en los quality gates.**

- **Protocolo:** el 98% de la superficie v3 está implementada y con prueba de contrato. C100 = **61/62** (la única fila `IMPLEMENTADO` es la 60, comportamiento de `sharedProps` observado por cliente oficial). Se verificó en esta auditoría que las cabeceras modernas existen en los dos adaptadores: `X-Inertia-Redirect`, `X-Inertia-Reset`, `X-Inertia-Except-Once-Props`, `X-Inertia-Infinite-Scroll-Merge-Intent`, `Purpose: prefetch` y `rescuedProps` (59 referencias en main).
- **Interop (I100):** **57/90 celdas = 63,3%**, y **0 de 10 escenarios** están en `E2E_VERIFICADO` (los 10 son `PARCIAL`).
- **Release (R100):** no cumplido — sin RC, sin revisión externa, y los gates de calidad no bloquean (ver §6, críticas P1-3 y P1-4).

El desglose de I100 por transporte, calculado directamente de `specs/e2e-compliance.yaml` en esta auditoría, es el hallazgo más importante del informe:

| Transporte | Celdas E2E verificadas | % |
|---|---:|---:|
| spring-mvc | 28/30 | 93,3% |
| quarkus-rest | 27/30 | 90,0% |
| **quarkus-reactive** | **2/30** | **6,7%** |
| **Total** | **57/90** | **63,3%** |

**Quarkus Reactive Routes se declara "stable target" en [docs/COMPATIBILITY_POLICY.md](../COMPATIBILITY_POLICY.md) §2 con el 6,7% de su matriz de interop verificada.** Esa es una sobreafirmación, aunque esté documentada.

### Frase de comunicación recomendada (para README/prensa)

> "Implementa y prueba con TCK el protocolo Inertia v3 completo en Spring MVC, Quarkus REST y Quarkus Reactive (C100 61/62). La interoperabilidad con clientes oficiales está verificada en 57 de 90 celdas E2E: 93% en Spring MVC, 90% en Quarkus REST, pero solo 6,7% en Quarkus Reactive. No se declara compatibilidad total: faltan celdas E2E (SSR React/Svelte, reactive), la validación de `sharedProps` por cliente, DevTools, RC y revisión externa."

### Puntuación comparativa

Escala 0–100 por dimensión. Pesos: Protocolo 25 · Paridad funcional 25 · Pruebas y evidencia 20 · Seguridad 10 · Ecosistema y madurez 10 · Doc y comunicación 10.

| Adaptador | Protocolo v3 | Paridad | Pruebas/evidencia | Seguridad | Ecosistema | Doc/comunicación | **Global** |
|---|---:|---:|---:|---:|---:|---:|---:|
| Laravel 3.3.2 (referencia) | 98 | 97 | 93 | 94 | 97 | 96 | **96/100** |
| Rails 3.22.0 | 95 | 95 | 92 | 93 | 94 | 95 | **94/100** |
| **Java — Spring MVC** | 95 | 88 | 82 | 90 | 82 | 90 | **88/100** |
| **Java — Quarkus REST** | 94 | 87 | 82 | 89 | 79 | 90 | **87/100** |
| **Java — Quarkus Reactive** | 90 | 86 | 72 | 88 | 78 | 87 | **84/100** |

Promedio Java: **86/100** (era 84/100 en la auditoría de 2026-09-23). Lectura correcta: **beta alta / RC técnica**, no "compatible al 100%".

---

## 2. Evidencia recogida en esta auditoría

Ejecutado en el checkout, sin modificar nada:

```
node scripts/check-conformance-matrix.mjs -> "Matrix validation OK: 128 test references resolved"
node scripts/check-versions.mjs           -> "Version check OK: all sources agree on 0.0.5"
npm run verify:metadata                   -> 6 arquetipos + 9 demos @inertiajs/* 3.7.1, matriz 62 filas,
                                             C100 61/62, I100 57/90, OK
```

Evidencia de pruebas existente en disco (no ejecutada por esta auditoría; se informa como histórica):

| Módulo | Clases de test | Últimos Surefire | Pruebas | Fallos/Errores |
|---|---:|---|---:|---:|
| spring-inertia | 47 | 2026-09-24 02:53 | **219** | 0 / 0 |
| quarkus-inertia | 55 | 2026-09-24 02:55 | **334** | 0 / 0 |
| quarkus-inertia-security | 4 | 2026-09-24 00:15 | **29** | 0 / 0 |
| inertia-core | 15 | — | sin reporte local | — |
| spring-inertia-security | 4 | — | sin reporte local | — |
| inertia-tck | 1 | — | sin reporte local | — |

Playwright: `e2e/test-results/.last-run.json` → `"status": "passed", "failedTests": []` (2026-09-24 02:56).
Specs: `inertia-contracts`, `pingcrm-contracts`, `feature-matrix` (15 casos × 4 demos = 60/60), `ssr-contracts`.

Comparativa de volumen de pruebas (archivos fuente):

| Proyecto | Fuente | Tests |
|---|---:|---:|
| inertia-laravel 3.x | 75 PHP en `src/` | 62 archivos PHP en `tests/` |
| inertia-rails 3.22 | 59 Ruby en `lib/` | 94 archivos en `spec/` |
| inertiajs-java 0.0.5 | 186 Java en main (6 módulos) | 149 archivos Java de test |

---

## 3. Pros — qué está bien hecho (con evidencia)

1. **Cobertura de protocolo amplia, moderna y verificable por fila.** `docs/protocol-compatibility.md` (generado, 62 filas) enlaza cada requisito con su test real; `node scripts/check-conformance-matrix.mjs` resuelve **128** referencias a clases de test. Ninguno de los dos adaptadores oficiales publica una matriz fila-a-fila trazable a tests ejecutables. Esto es una **ventaja real sobre Laravel y Rails** en gobernanza de claims.
2. **`append()` con rutas anidadas ya existe y está probado.** `inertia-core/.../model/MergePlan.java` (`Builder.append(String path)`), `MergeableBuilder.java`, fachadas `Inertia.mergeable()` (Spring `api/Inertia.java:404`, Quarkus `api/InertiaProps.java:301`), TCK en 3 transportes y ADR-004 (`docs/adr/004-merge-plan-append.md`). Cerró la crítica P2 #1 de la auditoría anterior.
3. **Errores múltiples por campo ya existen.** `inertia-core/.../model/ValidationErrors.java` (`toWireMap(boolean allErrors)`), flag `inertia.validation.all-errors` (default `false` = formato legado), aplicado en flash, error bags y Precognition en ambos adaptadores, con tests `ValidationAllErrorsIntegrationTest` / `ValidationAllErrorsQuarkusTest` y ADR-003. Cerró la crítica P1 #2 de la auditoría anterior. Nota: **Rails tampoco lo expone de forma explícita** (usa `ActiveModel::Errors#to_hash`), así que la paridad aquí es buena.
4. **Postura defensiva de seguridad superior a la de las referencias.** `SafeJsonEncoder` escapa `<`, `>`, `&`, `/`, U+2028 y U+2029 (Laravel emite `json_encode()` sin flags HEX en `src/Directive.php:24` y confía solo en el escape `/` por defecto de PHP; es conforme al protocolo, pero con menos defensa en profundidad). Añaden CSRF/XSRF, límites de tamaño de página, validación de destinos de redirect, `Vary`, y **`SsrEndpointPolicy` fail-fast en startup** (`inertia-core/.../ssr/SsrEndpointPolicy.java` + `SsrEndpointPolicyTest`) — esto cierra la crítica P2 #7 de SSRF de la auditoría anterior. Laravel delega eso en la config de `ssr.url`; Rails en `configuration.rb`.
5. **Resiliencia SSR completa:** circuit breaker, health check, cache, clasificador de fallos y fallback no fatal (`inertia-core/.../ssr/*`, `SsrFailureClassifier`, `SsrHealth`, `SsrCircuitBreaker`, `SsrResponseCache`). El contrato de error de SSR con `browserApi`/`sourceLocation` está parcialmente cubierto (clasificación server-side), declarado en `NOT_SUPPORTED.md`.
6. **Reproducibilidad corregida.** Ahora existen `mvnw`/`mvnw.cmd` + `.mvn/wrapper` (Maven 3.9.11, verificado con `.\mvnw.cmd -v` → Java 21.0.2). Cerró la crítica P2 #8 de la auditoría anterior.
7. **Política de compatibilidad escrita y honesta.** `docs/COMPATIBILITY_POLICY.md` define C100/I100/R100, una escalera de evidencia donde una prueba hecha a mano **nunca** cuenta para I100, snapshot congelado de la spec (`specs/inertia-v3-baseline-2026-09-23.yaml`) y el proceso de actualización. El README **no contiene la cadena "100%"** (verificado con grep) y advierte "Row counts are not a certification". Es más riguroso que el discurso de muchos adaptadores comunitarios.
8. **Fix real de framework en Fase E** (no cosmético): `back().withErrors(...)` perdía `X-Inertia-Error-Bag`; solución con helpers `support.ErrorBags` en ambos stacks + `ErrorBagsTest` 6/6 Spring y 3/3 Quarkus, documentado en `docs/migration.md`. Detectado por traza de red, no por suposición.
9. **Ventajas diferenciadoras frente a Laravel/Rails:** imagen nativa GraalVM (los dos referentes son N/A), dos stacks (Servlet y REST/Reactive) con el mismo contrato, TCK propio, 6 arquetipos Maven, 9 demos E2E con los 3 clientes oficiales, métricas Micrometer (`metrics/InertiaMetrics.java` en ambos), DSL de tests server-side (`testing/InertiaPage.java`, 955 líneas, con `sharedProps()` y matchers).
10. **Declaración de alcance negativo sin ambigüedad:** `docs/NOT_SUPPORTED.md` lista con tracking ID lo que no soporta (plantilla legada v1/v2, DevTools, guardrails JPA, `browserApi` en fallos SSR). Rails lo hace también muy bien (documenta que error bags son "Laravel-specific"); Laravel documenta menos lo que *no* hace.

---

## 4. Contras y críticas — la lista completa

### P1 — Bloqueantes para cualquier afirmación de calidad

**P1-1 · Quarkus Reactive se declara estable con el 6,7% de interop verificada.**
`COMPATIBILITY_POLICY.md` §2 dice "Reactive graduates stable inside 0.0.5" y `NOT_SUPPORTED.md` repite "stable target in 0.0.5", pero la matriz dice `vue/quarkus-reactive` 8 celdas sin verificar, `react/quarkus-reactive` 10/10 sin verificar, `svelte/quarkus-reactive` 10/10 sin verificar. Solo **2 de 30** celdas reactive están verificadas con un cliente oficial en navegador. Tener TCK 43/43 y `ReactiveStressTest` no es interop: la propia política (§3) lo dice — "A hand-made HTTP request counts for C100, never for I100".
**Corrección:** o completar las 28 celdas reactive, o degradar el lenguaje a "stable contract, interop in progress" en README, policy y NOT_SUPPORTED. Hoy la política y la evidencia se contradicen.

**P1-2 · Ningún escenario E2E está cerrado.** Los 10 escenarios de `specs/e2e-compliance.yaml` son `status: PARCIAL`. El badge "I100 57/90" describe celdas sueltas, no escenarios completos. `E2E-09` (SSR) tiene **2/9**: React y Svelte no tienen ninguna celda SSR (ni Spring ni Quarkus); falta la celda `vue/quarkus-reactive`. Un escenario PARCIAL puede ocultar un fallo sistemático en una combinación cliente×transporte.
**Corrección:** cerrar E2E-09 completo y al menos los escenarios 01–05 antes de RC; publicar el estado por escenario, no solo el conteo de celdas.

**P1-3 · Los quality gates no bloquean (esta es la crítica más seria de proceso).**
Verificado en disco en esta auditoría:
- `pom.xml:241-242` → `mutationThreshold=5`, `coverageThreshold=20` (umbrales de PILOTAJE; una cobertura del 20% no protege nada).
- `pom.xml:56,58` → `skipDependencyCheck=true` y `skipMutationAnalysis=true` **por defecto**.
- `.github/workflows/release.yml:51` → PIT corre con **`continue-on-error: true`** (un gate que no puede fallar no es un gate).
- `.github/workflows/ci.yml:62` → CI normal ejecuta con `-DskipDependencyCheck=true` (sin SCA en PR ni en main).
- `pom.xml:286` → SpotBugs `failOnError=false` (los hallazgos High no detienen el build).
- JaCoCo está configurado como `report` **sin reglas `check`** (no hay línea base de cobertura).
- SCA depende de `NVD_API_KEY` y solo corre en tags (`release.yml:134`); sin clave, el tag continúa sin análisis de vulnerabilidades.
Esto ya estaba en la auditoría de 2026-09-23 y **no ha cambiado**. Mientras siga así, R100 es inalcanzable por definición y cualquier mensaje de "release engineering" es prematuro.
**Corrección:** umbrales por módulo crecientes (p.ej. 60% cobertura real en `inertia-core`), PIT bloqueante en Linux, SCA en PR con credencial rotada y **fail-closed** si no puede ejecutarse, SpotBugs `failOnError=true`, `jacoco-check` con línea base congelada.

**P1-4 · Falta lo que hace un release: RC + revisión externa + zero P0/P1 abiertos.** El propio `CHANGELOG.md` lo admite: "Pre-tag gates left: M4b E2E cells, external review, native smoke on tag CI". Sin revisión externa, todas las afirmaciones de esta auditoría son autorevisión.
**Corrección:** publicar `0.0.5-rc1`, pedir revisión a al menos dos revisores externos (idealmente mantenedores de adaptadores de otros lenguajes) y solo entonces tag.

**P1-5 · Inconsistencia documental interna README vs matriz generada.** Violación directa de `COMPATIBILITY_POLICY.md` §6 ("coherent docs", requisito de R100). Verificado en esta auditoría:
- `README.md:29` dice 🔄 para "Explicit append() + nested routes" **en las tres columnas de transporte**, pero `docs/protocol-compatibility.md` fila 56 dice `TESTED (+E2E spring-mvc, quarkus-rest)` y `NOT_SUPPORTED.md` dice "TCK en 3 transportes y E2E Vue/Spring".
- `README.md:32` dice 🔄 para "Multiple messages per field" en todo, pero la fila 54 dice `TESTED (+E2E spring-mvc, quarkus-rest)`.
- `README.md:34` pone la columna E2E de "SSR con sidecar" en 🔄 mientras `specs/e2e-compliance.yaml` ya tiene 2 celdas SSR verificadas.
El README está **más conservador** que la matriz — es el sentido "menos peligroso", pero una fuente que se contradice con la otra desacredita el mecanismo de evidencia entero. Ambos textos son generables desde los YAML: faltó regenerar el README.
**Corrección:** generar la tabla del README desde `specs/inertia-v3-compliance.yaml` (mismo pipeline que `docs/protocol-compatibility.md`) y añadir un check en CI que falle si README y matriz discrepan.

**P1-6 · `sharedProps` de instant visits sigue sin validarse con cliente (C100 61/62).** Fila 60 `IMPLEMENTADO`. Laravel lo resuelve con `expose_shared_prop_keys` (`src/PropsResolver.php:193`, emisión en `:242`) y Rails con `expose_shared_prop_keys` (`lib/inertia_rails/renderer.rb:37`, emisión en `:159`); ambos llevan releases con ese comportamiento en producción. En Java el campo se serializa (`PageObject.java:50`, `SharedDataRegistry.java`) pero solo hay evidencia "Vue-observed" parcial.
**Corrección:** un solo escenario Playwright que demuestre persistencia/colisión/exclusión de `sharedProps` en los 3 transportes cierra la última fila de C100.

### P2 — Restan paridad, mantenibilidad y adopción

**P2-7 · DevTools: excluido "on purpose".** Laravel trae `src/DevTools/` completo (recorder, redacción de datos, repositorio de entradas, endpoints HTTP con gate) que es la herramienta que la comunidad más valora para depurar visitas. En Java está fuera de alcance (`NOT_SUPPORTED.md`) con el argumento de que la extensión de navegador ya interopera. Es un argumento parcialmente válido, pero la extensión no da redacción de datos propios de tu app ni historial de peticiones del servidor: **es una brecha real de experiencia diaria** frente al adaptador de referencia.
**Corrección:** mínimo viable — un recorder de peticiones Inertia con redacción y endpoint de inspección, o integración declarada con la extensión oficial más allá de "habla v3".

**P2-8 · Duplicación de lógica de protocolo entre frameworks (riesgo de deriva medible).**
`protocol/PageObjectBuilder.java` → **718 líneas** (Spring) vs **1.026 líneas** (Quarkus); `protocol/RedirectProcessor.java` → **167** vs **433**. Dos implementaciones del mismo contrato, con tamaños que ya divergen ×1,4 y ×2,6. Cada cambio upstream (p.ej. una cabecera nueva de v3) debe tocarse dos veces y probarse dos veces; es la causa estructural de que reactive y JAX-RS se desincronicen.
**Corrección:** extraer a `inertia-core` las decisiones de protocolo (metadata, clasificación de redirect, versionado, merge/reset) y dejar en los adaptadores solo request/response, sesión y serialización. El TCK ya existe para demostrar equivalencia.

**P2-9 · Sin instrumentación de alto nivel.** Rails expone 3 eventos `ActiveSupport::Notifications` (`render.inertia_rails`, `resolve_props.inertia_rails`, `ssr.inertia_rails`) y Laravel emite eventos de framework (`SsrRenderFailed`, etc.). Java solo tiene Micrometer (`InertiaMetrics.java` ×2), que es métricas, no trazas de negocio: no permite suscribirse a "qué componente se renderizó, con qué props, en cuánto ms" desde el código de la aplicación.
**Corrección:** un API de eventos/observabilidad (`InertiaEventPublisher` o MicroProfile Observability/`ApplicationEventPublisher`) con redacción de props.

**P2-10 · Generadores y flujo cotidiano menos ricos.** Rails tiene 4 generadores (`install`, `controller`, `scaffold`, `scaffold_controller`) + familias de templates React/Vue/Svelte/Tailwind + `inertia_rails:install:*`. Laravel tiene `make:inertia-middleware`, comandos SSR (`start/stop/check-ssr`) y macros de testing. Java tiene 6 arquetipos Maven (buen punto de partida de proyecto) pero **no hay generación dentro de un proyecto existente** (equivalente a `inertia:controller`) ni comandos de gestión de SSR (el ciclo de vida SSR es por config/lifecycle beans).
**Corrección:** `mvn inertia:new-page` / `inertia:ssr:start|stop|check` vía plugin Maven o Quarkus/Spring CLI.

**P2-11 · Evidencia local incompleta en 3 de 6 módulos.** `inertia-core` (15 clases de test), `spring-inertia-security` (4) e `inertia-tck` (1) tienen `test-classes` compilados pero **no hay `target/surefire-reports` locales**, es decir, en esta máquina no quedó constancia de su última ejecución verde. Quien clone el repo y mire el árbol ve 582 tests verificados (spring+quarkus+quarkus-security) y 20 clases de test sin prueba de ejecución. El TCK es justo el módulo que sostiene el C100 61/62.
**Corrección:** ejecutar `./mvnw -B verify -Pquality-gates` completo en un checkout limpio antes del tag y publicar los reportes como artefactos de CI (no como archivos locales).

**P2-12 · Cobertura de superficie "de framework", no de aplicación real.** Los demos son PingCRM/kitchen-sink/probe. No hay un ejemplo de **aplicación empresarial Java** con JPA real, paginación, seguridad y validación compleja. `NOT_SUPPORTED.md` admite que no existe guardrail contra serializar entidades JPA completas (lazy-load/cascadas), que es **el** error clásico de un usuario Java novel con Inertia. Laravel no lo necesita (DTOs/arrays por cultura); Java sí.
**Corrección:** ejemplo "real" con DTOs + validación + error bags + paginación con infinite scroll, y una guía "props que nunca deben ser entidades JPA".

**P2-13 · Error bags: soporte correcto pero menos profundo que Laravel.** Laravel maneja múltiples bags con renombrado del `default` según header (`src/Middleware.php:239-263`). Java soporta el header (fix Fase E) — pero conviene verificar y documentar el caso de varias bags simultáneas + `withErrors` a una bag nombrada, que es el uso real en formularios múltiples. No encontré un test E2E de dos bags concurrentes.
**Corrección:** caso E2E con dos bags y navegación entre formularios.

**P2-14 · Un solo mantenedor / proyecto comunitario sin governance de releases.** `README.md:11` lo dice con honestidad ("not officially maintained by the Inertia.js team"). Laravel y Rails tienen equipos, historial de releases (Laravel: 3.3.2/3.3.1/3.3.0 en 2 meses; Rails: 3.22.0/3.21.2/3.21.1 en 2 meses) y revisión por pares. Esto no es un defecto de código, pero explica parte de la diferencia de "Ecosistema y madurez" y debe pesar en quién decide adoptarlo en producción.

### P3 — Menores, pero que suman

**P3-15 · Plantilla legada v1/v2 rechazada sin opt-in.** Es coherente con el cliente 3.7.1 fijado (verificado: el cliente oficial solo consulta `script[data-page][type=application/json]`), y es honesto en `NOT_SUPPORTED.md`. Pero Rails, por defecto, emite todavía `<div data-page>` (`use_script_element_for_initial_page: false` en `configuration.rb:62`) y lo levanta a script con un flag documentado en su upgrade guide. Es decir: **el adaptador oficial de Rails es hoy más permisivo que el tuyo con el bootstrap v3.** Cuando alguien migre una app existente a Java, no habrá opción de compatibilidad.
**Corrección:** no hace falta cambiar de postura (v3-pure es defendible), pero sí documentar explícitamente en la guía de migración que no hay puente desde `data-page`.

**P3-16 · PIT inutilizable en Windows.** Documentado en `NOT_SUPPORTED.md` (el minion aborta localmente). Aceptable como contingencia, pero el 5%/20% de umbral hace que ni en Linux aporte señal hoy. Iría con los umbrales de P1-3.

**P3-17 · JaCoCo sin `check`.** Sin línea base, "cobertura" es un número decorativo en los informes. Ver P1-3.

**P3-18 · Falta un comparador de "migración desde Laravel/Rails".** `docs/migration.md` cubre 0.0.4→0.0.5. Un desarrollador Rails/Laravel que llegue a Java necesita la tabla equivalencia (`Inertia::share` → `share()`, `$this->middleware(Inertia::class)` → filtro/config, `prop_mergeable` → `mergeable()`). Es el mayor freno de adopción y no existe.

**P3-19 · Sin paquete de soporte de tooling.** Laravel tiene DevTools + integración Vite nativa; Rails tiene guías Vite/Tailwind propias. Java depende de que el arquetipo acierte en la combinación.

**P3-20 · El README's feature table no distingue "soportado" de "probado en CI" por columna.** La leyenda dice ✅ = Tested in CI, pero varias filas ✅ no dicen en qué workflow. Con la política C100/I100 existente, la tabla podría ser más explícita (añadir columnas C100/I100).

---

## 5. Comparación funcional resumida

| Área | Java (Spring/Quarkus) | Laravel 3.3.2 | Rails 3.22.0 | Juicio |
|---|---|---|---|---|
| Contrato HTTP v3 y page object | 62 filas con test enlazado, C100 61/62 | Referencia oficial | Muy amplio | **Java lidera en trazabilidad de evidencia** |
| Cabeceras modernas (prefetch, reset, except-once, scroll intent, X-Inertia-Redirect) | Verificado en código (ambos stacks) | Completo | Completo | Paridad |
| Bootstrap inicial | Solo `<script application/json>` (v3-pure) | `<script>` siempre (`Directive.php:24`) | `<div data-page>` por defecto, script con flag | Rails es el más permisivo; Java el más estricto |
| Escape JSON en HTML | `< > & / U+2028/9` (`SafeJsonEncoder`) | `json_encode()` sin flags HEX (solo `/` por defecto PHP) | `tag.div` + `to_json` | **Java con más defensa en profundidad** |
| `append()`/rutas anidadas | `MergePlan`/`MergeableBuilder` + TCK 3 transportes | `MergesProps.php` (flags `$append`, `$appendsAtPaths`) | `prop_mergeable.rb` (bool/String/Array/Hash) | Paridad de capacidad; ergonomia Java aún por validar en E2E profundo |
| Errores múltiples por campo | `ValidationErrors` + flag (default false) | `$withAllErrors` en middleware | `ActiveModel::Errors#to_hash` | Paridad |
| Error bags | Sí (fix Fase E), E2E parcial | Sí, completo | **No** (documentado: es feature de Laravel) | Java por encima de Rails |
| `sharedProps` / instant visits | Campo sí, cliente 2/30 reactive + fila 60 abierta | Maduro | Maduro | **Brecha Java** |
| SSR | Sidecar + breaker + health + `SsrEndpointPolicy` fail-fast | Integrado + comandos + eventos | Renderer + **plugin Puma** + detección Vite | Java sólido en resiliencia, flojo en tooling |
| DevTools | No (out of scope) | **Sí (completo)** | No | **Brecha frente a Laravel** |
| Instrumentación | Micrometer | Eventos de framework | `ActiveSupport::Notifications` (3 eventos) | Brecha Java (métricas ≠ eventos) |
| Generación/scaffolding | 6 arquetipos | `make:inertia-middleware` + comandos | 4 generators + templates React/Vue/Svelte/Tailwind | Rails > Laravel > Java en flujo interno |
| Testing | DSL `InertiaPage` + matchers + TCK propio | `assertInertia` + `AssertableInertia` | RSpec matchers (15+) + Minitest | Paridad; Java añade TCK multi-transporte |
| Imagen nativa (GraalVM) | **Sí** | N/A | N/A | **Ventaja Java** |
| Madurez de release | 0.0.5 sin RC | 3.3.2 con historial | 3.22.0 con historial | Brecha Java |

---

## 6. Evolución respecto a la auditoría de 2026-09-23

| Crítica anterior | Estado hoy |
|---|---|
| P1: README anunciaba cliente `2.x` | **Cerrado.** README solo declara `3.7.1 (pinned)`; `COMPATIBILITY_POLICY.md` §1 prohíbe fila 2.x. |
| P1: errores múltiples por campo inexistentes | **Cerrado** en código (M2, ADR-003). Queda E2E fuera de Vue/Spring. |
| P1: Reactive "estable" sin evidencia | **Abierto y cuantificado** (2/30 celdas) → hoy P1-1. |
| P1: quality gates blandos (PIT 5/20, SCA condicional) | **Abierto, sin cambios** → hoy P1-3. |
| P2: `append()` inexistente | **Cerrado** (M3, ADR-004, TCK 3 transportes). |
| P2: instant visits sin E2E | **Abierto** → hoy P1-6. |
| P2: duplicación Spring/Quarkus | **Abierto** (718/1.026 y 167/433 líneas) → hoy P2-8. |
| P2: SSR remoto sin validación | **Cerrado** (M5, `SsrEndpointPolicy` + test). |
| P2: sin Maven Wrapper | **Cerrado** (M1, `mvnw` 3.9.11 verificado). |
| P3: DevTools / ecosistema | **Abierto por decisión explícita** → hoy P2-7. |

---

## 7. Plan recomendado, en orden

1. **Alinear claims con evidencia (1 día).** Regenerar la tabla del README desde el YAML y añadir check de consistencia en CI; decidir Reactive: o 28 celdas E2E o lenguaje "interop in progress". Esto es P1-1 y P1-5.
2. **Cerrar I100 mínimo para RC (la ruta crítica).** E2E-09 SSR React/Svelte + celdas reactive de los escenarios 01–05 + fila 60 (`sharedProps`). Hoy: 57/90 → objetivo ≥ 85/90 con 0 escenarios PARCIAL en 01–05.
3. **Convertir los gates en gates (antes de tag).** PIT bloqueante en Linux con umbral real, SCA en PR fail-closed, SpotBugs `failOnError=true`, `jacoco-check` con línea base. Sin esto, R100 no se puede declarar ni en broma.
4. **RC + revisión externa + SBOM verificable.** Publicar `0.0.5-rc1`, dos revisores externos, artefactos firmados y reportes de CI como evidencia descargable (cierra P1-4 y P2-11).
5. **Extraer protocolo a `inertia-core`.** PageObjectBuilder/RedirectProcessor compartidos; adaptadores delgados. Reduce el riesgo estructural de que reactive vuelva a divergir (P2-8).
6. **Plan de experiencia:** DevTools mínimo viable o recorder con redacción; comandos SSR; generador de páginas; guía de migración desde Laravel/Rails; ejemplo empresarial con DTOs (P2-7, P2-10, P2-12, P3-18).

---

## 8. Evidencia y límites de esta auditoría

- Los tres checkouts se inspeccionaron **sin modificar sus fuentes**. El árbol de `inertiajs` estaba limpio al inicio y al final (solo se añadió este archivo en `docs/audits/`).
- Se ejecutaron `check-conformance-matrix.mjs`, `check-versions.mjs` y `verify:metadata` con salida OK (transcrita en §2).
- **No** se ejecutaron suites Maven ni Playwright por decisión de alcance; los números de tests y el estado E2E provienen de los reportes y artefactos existentes en disco (fechados 2026-09-24) y de los YAML de especificación. `inertia-core`, `inertia-tck` y `spring-inertia-security` **no tienen reportes locales**, por lo que su verde no está probado en esta máquina.
- Los puntajes de Laravel y Rails derivan de la lectura de sus fuentes locales; no son una certificación de que esos proyectos estén libres de defectos.
- Las afirmaciones sobre el protocolo se contrastaron contra [the-protocol](https://inertiajs.com/docs/v3/core-concepts/the-protocol), incluida la advertencia de escape de `/` dentro de `<script>`, el contrato de `409` con `X-Inertia-Location`/`X-Inertia-Redirect`, y el contrato SSR (`/render`, `/health`, `/shutdown`).
- Las notas no son un benchmark de rendimiento ni una auditoría de seguridad certificable; ponderan evidencia de código, pruebas, documentación y operabilidad disponible en los tres checkouts.
