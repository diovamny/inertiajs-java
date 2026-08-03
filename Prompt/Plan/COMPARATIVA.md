# Tabla comparativa — quarkus-inertia vs inertia-laravel vs inertia-rails

**Leyenda:** ✓ cumple · ✗ no cumple · ◐ parcial · – no existe/no aplica

**Versiones contrastadas:** quarkus-inertia 0.0.1-dev · inertia-laravel 3.x · inertia-rails 3.x

Fuentes: changelog oficial de `inertia-laravel` (3.x), guía de configuración de
`inertia-rails` (inertia-rails.dev), código fuente de ambos (`Middleware.php`,
`renderer.rb`, `props_resolver.rb`, `PropCacheable`) y código del adapter.

## A. Protocolo HTTP y PageObject

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| PageObject v3 completo (component/props/url/version) | ✓ | ✓ | ✓ |
| Dot-notation: expansión de props con `.` en la clave | ✓ | ✓ | ✓ |
| Partial reloads: only/except con prefijos (`data.path`) | ✓ | ✓ | ✓ |
| `X-Inertia-Reset` | ✓ | ✓ | ✓ |
| `X-Inertia-Except-Once-Props` | ✓ | ✓ | ✓ |
| `X-Inertia-Error-Bag` | ✓ | ✓ | ◐ |
| `X-Inertia-Infinite-Scroll-Merge-Intent` (append/prepend) | ✓ | ✓ | ✓ |
| `Vary: X-Inertia` | ✓ | ✓ | ✓ |
| Version mismatch → 409 + `X-Inertia-Location` (+ versión) | ✓ | ✓ | ✓ |
| 302→303 selectivo (solo PUT/PATCH/DELETE con 302 previo) | ✓ | ✓ | – |
| Redirect vacío/empty response → redirect a referer | ✓ | ✓ | – |
| ETag/304 condicional (lazy) | ✓ | – | – |

## B. Props

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| Shared props globales (`share`/`always`) | ✓ | ✓ | ✓ |
| Always props (sobreviven partial reloads) | ✓ | ✓ | ✓ |
| `errors` siempre presente (aunque vacío) | ✓ | ✓ | ◐ (config, default futuro `true`) |
| Deferred props con grupos (`deferredProps`) | ✓ | ✓ | ✓ |
| Optional props (solo si el partial las pide) | ✓ | ✓ | ✓ |
| Once props (custom key, `expiresAt`) | ✓ | ✓ | ✓ |
| Composición encadenada (once+defer, once+merge) | ✗ (APIs separadas) | ✓ | ◐ |
| Merge props: append | ✓ | ✓ | ✓ |
| Merge props: prepend | ✓ | ✓ | ✓ |
| Merge props: deep | ✓ | ✓ | ✓ |
| Merge props: `matchOn` (`matchPropsOn`) | ✓ | ✓ | – |
| Merge multi-target encadenado (`append([a,b])->prepend('x')`) | ✗ (por clave) | ✓ | – |
| Scroll props (infinite scroll + wrapper) | ✓ | ✓ | ✓ |
| Scroll prop diferido (lazy) | ✗ | ✓ | ✓ |
| Rescued props (errores silenciosos de deferred) | ✓ | ✓ | ✓ |
| Cached props server-side (con TTL) | ✓ (namespace `inertia_rails/`) | – | ✓ (`cache_prop` + `cache_store`) |
| Instance props (`use_inertia_instance_props`) | ✓ (`shareInstanceProps`) | – | ✓ |
| Meta props (`meta`, `pageMeta`/`pageTitle` al template) | ✓ | ✓ | ✓ |
| Server head prop serializado (`head`/`server_head`) | ◐ (solo template) | ◐ (`@inertiaHead`) | ✓ |
| Metadata `sharedProps` (exposición de keys) | ✓ | ◐ | ✓ |

## C. Respuestas y redirecciones

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| `render(component, props)` / props tipadas | ✓ | ✓ | ✓ |
| `redirect(url)` / `back()` | ✓ | ✓ | ✓ |
| `back(status, headers, fallback)` completo | ✓ | ✓ | ✓ |
| Headers custom en respuesta/redirect (`header`/`headers`) | ✓ | ✓ | ◐ (app-level) |
| Redirect externo → 409 `X-Inertia-Location` automático | ✓ (sin toggle) | ◐ (manual `location()`) | ✓ (default `true`) |
| Redirect full-page (`full_page`/`redirect(url, true)`) | ✓ | ✓ | ✓ |
| `setRootView` / layout por request | ✓ | ✓ | ✓ |
| Root DOM id configurable | ◐ (vía template) | ✓ (`@inertia('id')`) | ✓ (`root_dom_id`) |
| Página inicial en `<script type="application/json">` | ✓ | ✓ | ✓ |

## D. Versiones y assets

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| Versión custom + override runtime | ✓ | ✓ | ✓ |
| Estrategia Vite manifest | ✓ | ✓ | ◐ (ViteRuby, app-level) |
| Estrategia SHA-256 del build | ✓ | – | – |

## E. SSR

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| SSR HTTP (`ssrUrl` + `/render`, fallback CSR) | ✓ | ✓ | ✓ |
| SSR head (`{ssrHead}` / `@inertiaHead`) | ✓ | ✓ | ✓ |
| Exclusión SSR por ruta (`withoutSsr`/`disableSsr`) | ✓ | ◐ (solo `disableSsr`) | ✓ (lambda por controller) |
| Cache de respuestas SSR | ✗ | – | ✓ (`ssr_cache`) |

## F. Validación

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| Error de validación → back + `errors` flash | ✓ | ✓ | ✓ |
| Precognition header-based (204/422) | ✓ | ✓ (paquete first-party) | ✓ |
| Precognition: bloqueo de escrituras | ✗ | – | ✓ (`precognition_prevent_writes`) |
| Múltiples errores por campo (arrays) | ✗ (1 mensaje/campo) | ✓ | – |

## G. Seguridad

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| CSRF (`X-XSRF-TOKEN`, 419 en mismatch) | ✓ | ✓ (framework) | ✓ |
| Refresh de cookie XSRF lazy (CDN/ETag) | ✗ (siempre refresca) | ◐ | ✓ (`:always`/`:lazy`) |

## H. Testing

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| Helper de aserciones fluidas | ✓ (`InertiaPage`) | ✓ (`AssertableInertia`) | ✓ (matchers RSpec/Minitest) |
| Aserciones de flash/errores | ✓ | ✓ (`assertInertiaFlash`) | ✓ |
| Helper para partial requests | ✓ (headers manuales) | ✓ (test helper v2.0.3) | ✓ |

## I. Ecosistema y conveniencia

| Capacidad | quarkus-inertia | inertia-laravel | inertia-rails |
|---|---|---|---|
| Flash data con allowlist (`flash-keys`) | ✓ | ✓ | ✓ (`flash_keys`) |
| API `getFlash`/`pullFlash` | ✓ | – | – |
| `camelizeProps` / `prop_transformer` | ◐ (solo camelize) | – | ✓ (arbitrario) |
| Deep merge de shared data | ✗ | – | ✓ (`deep_merge_shared_data`) |
| Transformación de componente / `component_path_resolver` | ✓ (SPI) | – | ✓ |
| `UrlResolver` / custom URL resolver | ✓ (SPI) | ✓ | – |
| Prefetch (`Purpose: prefetch`) | ✓ | ✓ | ✓ |
| History: `encryptHistory` | ✓ | ✓ | ✓ |
| `clearHistory` / `preserveFragment` | ✓ | ✓ | ✓ |
| Generadores/installers | – | ◐ (artisan SSR) | ✓ (scaffold CRUD) |
| Compatible Native Image | ✓ | – | – |

## Resumen

De ~50 capacidades contrastadas, el adapter tiene **43 ✓**, **4 ◐** (server
head, merge multi-target, camelizeProps, root DOM id) y **8 ✗**:

1. Composición encadenada de props (once+defer, once+merge)
2. Merge multi-target en una sola llamada
3. Scroll prop diferido (lazy)
4. Múltiples errores por campo (arrays)
5. Precognition: bloqueo de escrituras
6. Refresh de cookie XSRF lazy (CDN/ETag)
7. Deep merge de shared data
8. Cache de respuestas SSR

Las ✗ son mayoritariamente features añadidas por los adaptadores oficiales en
2025-2026, fuera del objetivo original del plan de implementación.
