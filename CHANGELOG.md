# Changelog

## Unreleased

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
