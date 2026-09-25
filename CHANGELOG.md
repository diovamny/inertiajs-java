# Changelog

## 0.0.5 (release-0.0.5, in progress)

- M0 (P100-01): v3-only policy (`docs/COMPATIBILITY_POLICY.md`, frozen snapshot
  `specs/inertia-v3-baseline-2026-09-23.yaml`); honest matrix (PROTO-053 split
  bags vs multi-message, PROTO-054 wire vs explicit `append()`, PROTO-057
  serialization vs instant-visit behavior; Reactive as stable target, no
  experimental label at the end of the release); README/ROADMAP/NOT_SUPPORTED
  without `2.x` or `58/59`-as-certification claims; version `0.0.5` single source.
- M1 (P100-02/03): Maven Wrapper 3.9.11 (`mvnw`/`mvnw.cmd`), Enforcer
  (Java 21+/Maven 3.9+), Node `24.21.0` + npm `11.17.0` (`engines`,
  `.nvmrc`), `specs/e2e-compliance.yaml`, clean-checkout CI
  (ubuntu+windows), `npm run verify:metadata`.
- M2 (P100-04/05): core `ValidationErrors` + `withValidationErrors`/
  `withErrorMessages` (erasure-safe, ADR-003) + `inertia.validation.all-errors`
  (default `false`); TCK multi-message on 3 transports; `japicmp` MINOR.
- M3 (P100-06/07): core `MergePlan` + `MergeableBuilder` +
  `inertia.mergeable()...value()`/`applyMergePlan` (ADR-004); TCK
  `merge-nested` + child reset on 3 transports; legacy `merge()` unchanged.
- M5 (P100-10/11): core `SsrEndpointPolicy` + startup fail-fast in both
  adapters (ADR-005) + no-redirect HTTP clients + sidecar-controlled tests.
- M7 (P100-17/18): Reactive graduated to stable — 43-case TCK parity,
  `ReactiveStressTest` (8×12 concurrent), G-17-class isolation fix
  (`InertiaContextLocals` + registries + writer), `docs/reactive-parity.md`,
  nightly `reactive-stress.yml`.
- M4a (P100-08/09, part 1): Playwright + official Vue 3 client 10/10 on
  Spring (validation, bags, merge-append, instant-visit PROTO-057B first
  client proof, upload, 409, partials); matrix `60/62 verified, 8 E2E`.
  M4b (same branch, before tag): Quarkus-Vue login cells (demo Secure-cookie
  + post-login anomaly ticketed), React/Svelte fixtures, Reactive/SSR cells.
- M6-lite (P100-12/13): no moves; frozen core-decision inventory (ADR-006).
- M8 (P100-14/15/16, part 1): `verify -Pquality-gates` green (0 Checkstyle,
  SpotBugs High clean, full suites); `japicmp` 0.0.4→0.0.5 MINOR ×3;
  pitest 1.17.4 (minion still aborts on this station — report-only +
  ticket, 3 local repros); SCA fail-closed on tags (Dependency-Check
  12.1.0); SBOM aggregate post-deploy (existing step). Pre-tag gates left:
  M4b E2E cells, external review, native smoke on tag CI.
- Docs/CI (executed audit 2026-09-24): README feature table aligned with the
  generated matrix (`append()` and multi-message rows now Tested on the 3
  transports, E2E still in progress); `ssr-e2e` job uses `npm install`
  (generated starters ship no lockfile, `npm ci` fails EUSAGE); new
  Playwright runbook appended to `docs/testing-guide.md` (demos/ports,
  `--workers=1` + warm-up, SSR starter flow). Full fresh evidence: 711 unit
  + integration tests, 242 demo smoke tests, 118 Playwright checks, all
  green — see `docs/audits/AUDITORIA_EJECUTADA_AVANZADA_RELEASE_0.0.5_2026-09-24.md`.
- Closure (executed audit 2026-09-25, all points closed): C100 62/62, I100
  90/90 (all E2E scenarios E2E_VERIFICADO, incl. 28 reactive cells and 6+3
  SSR cells). New E2E fixtures: `/e2e-probe-rx` Reactive Routes probes
  (Quarkus React/Svelte), `reactive-contracts.spec.ts` + JAX-RS rest-instant
  twin (Vue), SSR support in all 4 React/Svelte archetypes (+`hydrateRoot`),
  `E2E_SSR_PATH` in ssr-contracts. Fixes found by execution: reactive CSRF
  single-token adoption (dual-issuer 303s), protocol-exact SSR assembly (no
  nested `#app`), Spring `inertia.ssr.*` dotted keys do not bind (docs now
  canonical hyphenated). Quality gates blocking: PIT minion fixed (add-opens
  + aligned junit-platform-launcher; core 82%/70%), SpotBugs failOnError,
  per-module JaCoCo floors, SCA fail-closed (release tags + CI push job).
  Protocol decisions extracted to `inertia-core.protocol` (VersionPolicy,
  RedirectClassifier, PartialFilter, MergeLabels, ErrorWire; -447 lines in
  adapters, +36 core tests); documented divergences kept per adapter.
  Full evidence: 761 unit/integration + 242 demo smoke + 181 Playwright
  checks green on final code (4 intentional skips of the reactive-only CSRF
  test on the classic base) — see
  `docs/audits/AUDITORIA_CIERRE_RELEASE_0.0.5_2026-09-25.md`.

## 0.0.4

- Fase 0 (PLAN_MEJORA v4, H1–H5): versión única `0.0.4` con fuente central
  `version.properties` y `node scripts/check-versions.mjs` en CI; CSRF canónico
  `303 + flash` en visitas Inertia en Spring y Quarkus (fila 38 de la matriz
  corregida: `419` solo fuera de Inertia en Quarkus / pass-through en Spring);
  plantilla raíz v3 pura — `<div id="app">` sin `data-page`, page object solo en
  `<script type="application/json" data-page="app">` (ver nota de bytes abajo);
  `ROADMAP.md` regenerado desde el release actual; `docs/ssr-setup.md` sin
  ejemplo `laravel-vite-plugin`.
- Plantilla raíz v3: el payload JSON viaja una sola vez (antes dos veces:
  atributo `data-page` escapado + script). Medición del bootstrap
  (`<div id="app">` + `<script data-page="app">`, página de ejemplo de 299 B):
  antes 930 B → ahora 378 B, ahorro de 552 B (**59%**); el ahorro crece con el
  tamaño del page object porque se elimina la copia completa del atributo.
  Breaking change anunciado: el atributo `data-page` heredado (v1/v2) no se
  soporta ni como opción.
- Módulo reactivo Quarkus en estabilización: regresión de concurrencia G-17
  permanente en CI; README/docs lo marcan como estabilización en curso hasta un
  ciclo sin regresiones.
- Fase 1 (PLAN_MEJORA v4, H5): `specs/inertia-v3-compliance.yaml` como fuente
  única (59 requisitos PROTO-001–059 con status y test_ref) y
  `docs/protocol-compatibility.md` generado por
  `scripts/generate-compatibility-matrix.mjs` (gate en CI: regenera y falla
  ante cualquier diff; falla con IMPLEMENTED sin test_ref). Métrica:
  **Protocol 57/59 verified (5 con E2E Playwright)**. Filas nuevas: error bags,
  append/matchOn, infinite-scroll intent, once key/fresh/expiry, sharedProps,
  error estructurado SSR, bootstrap v3.
- Fase 2 (PLAN_MEJORA v4, H6–H13): TCK ejecutable ampliado a 39 casos
  (Spring/JAX-RS) y 21 (Reactive): once con key/expiry/combos, merge moderno +
  reset, infinite scroll con intent, error bags con nombre, uploads multipart
  (nuevo DSL `multipart` con CRLF), sharedProps; `BootstrapHtmlContractTest`
  en ambos adapters (8 fixtures §5); `SsrFailureClassifier` en core con
  logging en ambos handlers; fixes de paridad (supresión once eager en
  Quarkus, purge de vencidos en Spring, `always` vs `share` en partials).
  Matriz: **58/59 verified**.
- Fase 4 (PLAN_MEJORA v4, H14/H16/H18): `docs/NOT_SUPPORTED.md`; tabla
  Supported/Tested + matriz de versiones en README; sitio VitePress
  `docs-site/` (21 páginas) con deploy a Pages (`docs.yml`).
- Fase 5 (RC 0.0.4): perfil `apicheck` (japicmp 0.0.3→0.0.4: core MINOR,
  spring MINOR, quarkus MAJOR por `maxPageBytes` y ctor interno, ambos
  anunciados en `docs/migration.md`); RFC/deprecación en CONTRIBUTING;
  revisión externa en GOVERNANCE.
- Fase 3 (PLAN_MEJORA v4, H15/H20): release bloqueante con quality gates
  (`mvn clean verify -Pquality-gates`, PIT en modo informe — el minion de
  pitest 1.16.3 aborta en JDK 21, ver `release.yml` —, OWASP condicional a
  `NVD_API_KEY`); guards compartidos `RedirectTargets` (CRLF/schemes →
  400/400 sin emitir cabecera) y `PageSizeGuard` (`inertia.max-page-bytes`,
  32 MiB, → 413) con suites de seguridad en ambos adapters; frontera de
  confianza SSR documentada; `native-tests.yml` en tags `v*`; módulo JMH
  `benchmarks/` (531k/103k/171 ops/s en small/medium/large).

## 0.0.3

- Security, framework-first: `spring-inertia-security` and
  `quarkus-inertia-security` bridges (401 → 409 challenges, 403 Inertia
  pages, native CSRF ownership, fail-fast guardrails); hand-written
  `AuthFilter`s removed from every demo.
- Executable TCK (`inertia-tck`): normative YAML suite certified against
  Spring MVC, Quarkus JAX-RS and reactive routes.
- New `inertia-core` module: protocol model, pure SPIs, `VaryHeaders`,
  server head builder, sealed `InertiaResult`, testing and SSR resilience
  primitives shared by both adapters.
- Protocol parity: fluent `flash()` on renders/redirects, `clearHistory`
  config, XSRF `always|lazy` refresh policy, concurrent-isolation suites,
  CSP nonce SPI, precognition write guard, resilient SSR (breaker,
  supervisor, health) with MD5 response cache, enriched testing DSL
  (`where`/`has`/`missing`/`dumpDiff`), Micrometer metrics.
- New Maven archetypes: `inertia-spring-svelte-archetype` and
  `inertia-quarkus-svelte-archetype` (Svelte 5 starters).
- Release engineering: CycloneDX SBOM, SLSA provenance, signed artifacts
  via the Central Portal pipeline.

## 0.0.2

- GroupId and Java packages renamed `io.github.dg.*` → `io.github.diovamny.*`
  (namespace owned by the publisher; required by Maven Central).
- Quarkus upgraded 3.38.0 → 3.39.2 across modules, examples, archetypes and CI.
- New Maven archetypes published: `inertia-spring-vue-archetype`,
  `inertia-spring-react-archetype`, `inertia-quarkus-vue-archetype`,
  `inertia-quarkus-react-archetype` (Vue 3 / React 19 starters with native `Dockerfile`).
- Starter fixes backported where applicable: Quarkus statics served from
  `META-INF/resources`, Vite `outDir`/`app.*` entry, `emptyOutDir: false`;
  demo frontend builds bound to `generate-resources`.
- Adapter fixes: partial reloads keep explicit null props; Spring native
  `RuntimeHints` completed (`INTROSPECT` modernization, `ScrollProp`,
  `@ImportRuntimeHints`); deprecated APIs migrated (JSpecify, Jackson 3,
  Vert.x `authority()`, 422 naming).
- CI: contract E2E suite, archetype matrix, starter native builds with HTTP
  smoke, project Checkstyle gate at zero violations.

## Unreleased

### Nuevo: adaptador Spring Boot (`spring-inertia`)

- **`io.github.diovamny.spring.inertia:spring-inertia:0.0.1`**: adaptador Inertia.js v3
  para Spring Boot 4.1.x / Spring Framework 7 (síncrono, Spring MVC, AOT y
  GraalVM Native con `RuntimeHintsRegistrar`).
- API en paridad con el adaptador Quarkus: `Inertia` (render/redirect/back/
  location/flash/share/always/deferred/once/merge/optional/cached/rawJson,
  `MergeRule`), `InertiaResponse`, `InertiaRedirect` encadenado estilo Laravel
  (`with`, `withErrors`, `withInput`).
- Auto-configuration (`inertia.*`, 15 propiedades), SPI extensible
  (`FlashStore`, `JsonProvider`, `ErrorMapper`, `ComponentTransformer`,
  `UrlResolver`), validación con `@Valid` + Precognition (422 / flash de errors),
  CSRF con cookie `XSRF-TOKEN`, ETag lazy, SSR vía `SsrClient`, version mismatch
  409, merge props server-side en partial reloads, testing helpers
  (`InertiaPage`, `InertiaResultMatchers`).
- Paquete Quarkus renombrado a `io.github.diovamny.quarkus.inertia.*` (sin cambios
  funcionales).
- **Nuevo demo**: `examples/spring/spring-pingcrm` (Spring Boot 4.1 + Vue 3) —
  port completo de PingCRM (organizaciones, contactos, usuarios con foto,
  dashboard, reportes, autenticación por sesión PBKDF2, seed con datos fake,
  Flyway, soft deletes, imágenes con resize) sobre `spring-inertia`. 25 tests
  de integración MockMvc.
- **Nuevo demo**: `examples/spring/spring-kitchen-sink` (Spring Boot 4.1 +
  Vue 3) — port 1:1 del showcase Quarkus `examples/quarkus/kitchen-sink`
  (auth por sesión, Precognition, data loading: deferred/partial/scroll/
  polling/merge/once/optional props, formularios + file uploads, estado,
  HTTP API, layout/nav/redes, CRM con paginación) sobre `spring-inertia`.
  23 tests de integración MockMvc, H2 in-memory.
- **Demos reestructurados**: los demos Quarkus viven ahora en
  `examples/quarkus/` (demo-app, kitchen-sink, pingcrm, pingcrm-react) y los
  de Spring en `examples/spring/`; `examples/spring-demo` (básico) fue
  eliminado en favor de `spring-pingcrm`.
- **Fix boot del cliente v3**: `HtmlRenderer` ahora emite además el JSON de la
  página en un `<script type="application/json" data-page="app">` (placeholder
  `__INERTIA_PAGE_JSON__`), que es el formato que `@inertiajs/core` v3 lee al
  arrancar; sin él el cliente lanzaba `Cannot read properties of null
  (reading 'component')` y la pantalla quedaba en blanco. La plantilla raíz
  de spring-inertia y la del demo incluyen el nuevo script tag (el atributo
  `data-page` clásico se mantiene por compatibilidad).
- **Fix `errors` siempre presente**: el builder de página de spring-inertia
  ahora inyecta `props.errors` (mapa vacío si no hay errores) en cada render
  cuando `inertia.always-include-errors=true` (paridad con Laravel y con
  quarkus-inertia); antes la clave faltaba y componentes como
  `FlashMessages.vue` reventaban con `Cannot convert undefined or null to
  object` al hacer `Object.keys(props.errors)`. La clave sobrevive además a
  los partial reloads (como los always props).
- **Fix 404 silencioso**: las rutas y recursos estáticos inexistentes
  (`NoResourceFoundException`, p. ej. el probe de Chrome
  `/.well-known/appspecific/com.chrome.devtools.json`) ya no se convierten en
  un `UnhandledException` con stack trace: las visitas normales reciben un
  404 sin cuerpo y las visitas Inertia un page object JSON (application/json)
  con status 404 y el componente de error configurado; un `ErrorMapper`
  registrado sigue teniendo prioridad.

### Corregido: cookie `XSRF-TOKEN` descartada en contenedores reales

- `InertiaCsrfFilter` añadía la cookie `XSRF-TOKEN` **después** de ejecutar el
  chain de filtros; los contenedores reales (Tomcat) descartan cabeceras de una
  respuesta ya commiteada y el frontend nunca recibía el token. Ahora la cookie
  se añade **antes** de `filterChain.doFilter` (MockMvc no commitea respuestas,
  por eso el test previo no lo detectaba).
- Nuevo test de regresión `InertiaCsrfFilterTest` que verifica la cookie
  presente en el momento de ejecutarse el chain. Suite completa: 230 tests
  Quarkus + 75 Spring + 7 demo, todos verdes.

## 0.0.2 (2026-08-03)

### Páginas con status HTTP y páginas de error Inertia

- **`render(component, props, status)`** (y overloads `render(component, status)`,
  `render(enum, ...)`): permite renderizar una página con un código HTTP explícito
  (403/404/500, etc.) tanto en la respuesta JSON de requests Inertia como en el
  HTML de requests regulares (paridad con `Inertia::render(..., status)`).
- **Errores como página Inertia**: `InertiaExceptionMapper` y
  `ErrorResponseFactory` ahora producen una página Inertia
  (`{component, props: {status, message}, url, version}`) con el status real del
  error para requests Inertia — `WebApplicationException` (403/404) usa su
  status; excepciones sin manejar usan `inertia.error-status` (default `500`).
  Antes se devolvía un payload `530`/plano. Los requests no-Inertia mantienen el
  comportamiento previo (500 plano / passthrough).
- **Nueva config**: `inertia.error-status` (default `500`; para el comportamiento
  `530` anterior configurar `inertia.error-status=530`) e
  `inertia.error-component` (default `ErrorPage`).
- **`inertia.always-include-errors` cableada**: ahora controla si `errors` se
  incluye siempre (default `true`, comportamiento previo) o solo cuando hay
  errores. Antes estaba declarada pero no se leía.
- **`inertia.root-view` cableada**: usado como template por defecto del
  documento HTML cuando no hay override por request (default: `root-template`).
  Antes estaba declarada pero no se leía.
- **`ssr-bundle` eliminada**: propiedad declarada pero nunca consumida; se
  elimina de `InertiaConfig`.
- **Versión `sha256` content-based**: la estrategia `sha256` por defecto ahora
  hashea el contenido de `META-INF/resources` (cambia al reconstruir el
  frontend) en lugar de un timestamp de arranque. Sin webroot, mantiene el
  fallback anterior (hash por arranque).

### Redirect encadenado estilo Laravel

- **`InertiaRedirect`** (antes `BackRedirect`): todas las variantes de `back()`
  **y `redirect()`** devuelven ahora un `InertiaRedirect`, que implementa
  `Uni<Object>` por delegación (source-compatible con el código existente) y
  admite encadenado estilo Laravel:
  - `back().with("success", "Registro actualizado correctamente.")` /
    `redirect(url).with("success", ...)` — equivalente a
    `Redirect::back()->with(...)` / `Redirect::to(url)->with(...)`.
  - `.withErrors(Map.of("email", "invalid"))` — equivalente a
    `->withErrors([...])`.
  - `.withInput(Map.of(...))` — equivalente a `->withInput()`.
  - `flash(key, value)` / `flash(map)` como alias de `with(...)`.
  - El flash se escribe en el `FlashStore` en el momento del encadenado (mismo
    request que el redirect), así que `props.success` / `props.errors` llegan al
    frontend tras el 303.
- **Renombrado**: `BackRedirect` → `InertiaRedirect` (API nueva de este ciclo,
  sin usos externos; los tests y el demo se actualizaron).

## 0.0.2 (2026-08-03)

Refinamiento de paridad con los adaptadores oficiales (validado contra
`inertia-laravel` Middleware/ResponseFactory y `inertia-rails`
props_resolver/PropCacheable).

### Protocolo HTTP

- **303 selectivo**: la conversión `302 → 303` solo aplica a PUT/PATCH/DELETE y
  solo cuando el status previo es exactamente 302 (antes: cualquier no-GET con
  status 302/303). Extraído `normalizeRedirectStatus(method, status)` estático
  en `InertiaResponseFilter` (paridad con `Middleware.php:168-170`).
- **`errors` siempre presente**: el page object siempre incluye `errors`
  (paridad con el Middleware de inertia-laravel que hace
  `'errors' => Inertia::always(...)`); los errores flasheados se envuelven en
  `AlwaysProp` y sobreviven a partial reloads.
- **Headers custom por request**: `header(name, value)` / `headers(map)` en la
  interfaz `Inertia`, almacenados en context local Vert.x y aplicados por
  `InertiaResponseFilter` a toda respuesta (JSON, redirects y 409).
- **`back(status, headers)` y `back(status, headers, fallback)`**: firmas
  completas equivalentes a `ResponseFactory::back` de Laravel (referer →
  fallback → `/`); headers también en conflictos 409.

### Props

- **Dot-notation**: `PartialReloadProcessor` filtra con semántica de prefijo
  (`data.path` incluye `data.path.sub`, `except` con prefijo excluye
  subpropiedades) y `PageObjectBuilder` expande claves con `.` a mapas
  anidados (paridad `expand_dot_notation` de inertia-rails).
- **`shareInstanceProps(instance)`**: equivalente de
  `use_inertia_instance_props` — los getters del bean pasan a ser props cuando
  `render()` no recibe props manuales (igual que `view_assigns` de Rails).
- **Cached props con namespace**: las claves de `CachedPropStore` se prefijan
  con `inertia_rails/...` (paridad `PropCacheable#derive_cache_key`).

### Validación

- Flujo de validación (no precognition) verificado end-to-end:
  `ConstraintViolationException` en peticiones Inertia → `302` back + `errors`
  flash; precognition sigue devolviendo `422`.

### Testing helper

- Nuevas aserciones en `InertiaPage`: `assertUrl`, `assertVersion`,
  `assertProp(key, value)`, `assertNoDeferredProps`, `assertNoOnceProps`.

### Tests

- Adapter: **179** (unitarios + integración). Demo-app: **86** — total **265**,
  todos pasan.

## 0.0.1 (2026-08-02)

Primera release del adaptador `quarkus-inertia`, en paridad con el protocolo
Inertia.js v3 (validado contra `inertia-laravel` / `inertia-rails`).

### Protocolo v3

- `PageObject` completo: `deferredProps`, `mergeProps`, `prependProps`,
  `deepMergeProps`, `matchPropsOn`, `onceProps`, `scrollProps`, `sharedProps`,
  `rescuedProps`, `meta`, `encryptHistory`, `clearHistory`, `preserveFragment`.
- **Once props**: API `once(key, value[, customKey, expiresAt])` con
  `X-Inertia-Except-Once-Props` — el servidor omite resolver lo que el cliente
  ya tiene pero mantiene la metadata.
- **Merge props**: `merge(key, value[, deep])`, `prepend(key, value)` y
  `matchPropsOn` (`key.path`); las keys de `X-Inertia-Reset` se excluyen de la
  metadata de merge.
- **Deferred props**: `deferred(group, supplier)` — se resuelven solo en
  partial reloads; la metadata `deferredProps` se emite en la visita inicial.
- **Infinite scroll**: `scroll(key, metadata)` emite `scrollProps` con
  `previousPage/nextPage/currentPage/pageName` y respeta
  `X-Inertia-Infinite-Scroll-Merge-Intent` (append por defecto, `prepend` si el
  header lo indica).
- **Rescued props**: `rescue(key)` → `rescuedProps` (lista, formato v3).
- **Meta**: `meta(key, value)` / `meta(map)` — expuesto al root template como
  `pageMeta` y `pageTitle`.
- **Optional props**: `optional(key, resolver)` — nunca se resuelven en la
  carga inicial y solo se evalúan cuando un partial reload las pide
  explícitamente (`X-Inertia-Partial-Data`).
- **`back()` ergonómico**: `back(fallback)` y `back(status, fallback)`
  (equivalente al `back(status, headers, fallback)` de Laravel).
- **Inspección de shared data**: `getShared()` y `flushShared()`.
- **`getShared(key, default)`**: lectura puntual con fallback (equivalente a
  `Inertia::getShared($key, $default)`).
- **`merge(key, value, deep, matchOn...)`**: registra `matchPropsOn`
  (`key.campo`) junto con merge/deep-merge (equivalente a
  `Inertia::merge()->matchOn()`).
- **Versión runtime**: `version(version)` (override persistente sobre la
  estrategia configurada — `sha256`/`vite-manifest`/`custom`).
- **Root view por request**: `setRootView(name)` — prioridad sobre
  `inertia.root-template` para el render HTML.
- **SSR condicional**: `withoutSsr(paths)` / `disableSsr()` por request y
  `inertia.ssr-exclude-paths` global (matcher exacto o `*` wildcard).
- **Partial reloads**: `X-Inertia-Partial-Data/Except/Component`, `X-Inertia-Reset`,
  `X-Inertia-Error-Bag`.
- **Serialización limpia**: metadata vacía y flags `false`
  (`encryptHistory`, `clearHistory`, `preserveFragment`) se omiten del JSON.

### Precognition & prefetch

- Header-based (`Precognition`, `Precognition-Validate-Only`) en lugar de
  config flag; eliminada `inertia.precognition-enabled`.
- Éxito → `204` + `Precognition-Success: true`; errores → `422` con el page
  object; `Vary: Precognition`.
- Prefetch detectado via `Purpose: prefetch` / `X-Inertia-Prefetch`.

### CSRF

- Validación de `X-XSRF-TOKEN` contra la sesión Vert.x en mutaciones
  (no GET/HEAD/OPTIONS) → `419` en mismatch; comparación en tiempo constante.
- Cookie `XSRF-TOKEN` emitida por respuesta (`Path=/; SameSite=Lax`, sin
  `HttpOnly`, legible por el cliente Inertia).
- Property `inertia.csrf.enabled` (default `true`).

### Render & versiones

- `inertia.root-template` configurable (antes `index.html` hardcoded).
- Version mismatch → `409` + `X-Inertia-Location` + `X-Inertia-Version`; el
  flash data se conserva para el full reload posterior.

### Otros

- Eliminado `VersionMismatchHandler` (doble fuente de verdad, el
  `ResponseProcessor` ya lo manejaba).
- Tests: 79 en el adaptador (incluye CSRF, precognition, once props) y 83 en
  la demo-app (incluye infinite scroll, flash sobre 409, CSRF end-to-end).

### Paridad completa (features avanzadas)

- **Cached props**: `cache(key, resolver)` / `cache(key, ttl, resolver)`,
  `optional(key, resolver, cacheKey[, ttl])` y `deferred(group, name, resolver, cacheKey[, ttl])`
  respaldadas por `CachedPropStore` `@ApplicationScoped` con TTL opcional.
- **Flash API**: `getFlash(key, default)` / `pullFlash(key, default)` sobre el
  FlashStore SPI; allowlist con `inertia.flash-keys` (la descarga solo emite las
  keys configuradas) y `inertia.always-include-errors` para forzar `errors` siempre.
- **Component/URL hooks**: SPIs `ComponentTransformer` y `UrlResolver`
  (inyectables con `@Inject Instance<>`) y `render(Enum)` → `enum.name()`.
- **Full-page redirect**: `redirect(url, fullPage)` — en peticiones Inertia
  responde `409` + `X-Inertia-Location`; fuera de Inertia, 302 normal.
- **Error handling**: `handleErrorUsing(ErrorMapper)` por request y
  `ErrorResponseFactory` (`@ApplicationScoped`) — `530` + payload
  `{error: {message, exception}}`; `InertiaExceptionMapper` (`@Provider`)
  centraliza errores no controlados (500 en no-Inertia).
- **SSR de verdad**: `SsrHandler.render(page)` hace POST a `ssrUrl` + `/render`
  (con `RequestOptions.setAbsoluteURI`), `HtmlRenderer` usa el response en el
  root template (`{ssrBody}`/`{ssrHead}`) con fallback automático a CSR, y soporta
  exclusión por request/global con `withoutSsr`/`inertia.ssr-exclude-paths`.
- **ETag condicional**: `InertiaResponseFilter` computa un ETag SHA-256 de la
  representación y devuelve `304` en GET con `If-None-Match` (habilitable con
  `inertia.lazy-etag-enabled`, default `true`).
- **Testing helper**: `com.quarkus.inertia.testing.InertiaPage` — deserializa el
  page JSON y ofrece aserciones fluidas (`assertComponent`, `assertHasProps`
  parcial, `assertHasExactProps`, `assertNoProp`, `assertDeferredProps(…InGroup)`,
  `assertOnceProps`, `assertScrollProps`, `assertMeta`, familias merge/match).
- Tests finales: **150 en el adaptador** y **86 en la demo-app** (helper
  `InertiaPage` end-to-end incluido).
