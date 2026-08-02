# Quarkus Inertia.js v3 Adapter

Adaptador Inertia.js v3 para Quarkus Reactivo (Mutiny + CDI + Vert.x).

## Requisitos

- Java 21+
- Maven 3.9+
- Node.js 22+ (solo para demo-app)

## Arquitectura

```
com.quarkus.inertia
├── api/           → Inertia (interfaz pública)
├── config/        → InertiaConfig (ConfigMapping)
├── model/         → PageObject, AlwaysProp, DeferredProp, OnceProp
├── protocol/      → Filtros, builders, procesadores (núcleo HTTP)
├── renderer/      → HtmlRenderer, SsrHandler
├── response/      → JsonResponseProcessor
├── security/      → InertiaCsrfFilter
├── spi/           → FlashStore, JsonProvider
├── version/       → VersionProvider, DefaultVersionProvider
└── internal/      → InertiaImpl, JsonbJsonProvider, JacksonJsonProvider
```

## Uso

### 1. Añadir dependencia

```xml
<dependency>
    <groupId>com.quarkus.inertia</groupId>
    <artifactId>quarkus-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. Configurar

```properties
inertia.root-template=index.html
inertia.version-strategy=custom
inertia.version-custom=1.0.0
inertia.encrypt-history=false
inertia.camelize-props=false
inertia.csrf.enabled=true
```

### 3. Usar en recursos JAX-RS

```java
@Inject
Inertia inertia;

@GET
public Uni<Object> index() {
    return inertia.render("Pages/Home", Map.of("users", List.of()));
}

@POST
public Uni<Object> store(@Valid @BeanParam Form form) {
    // ... guardar ...
    inertia.flash("success", "Creado");
    return inertia.redirect("/items");
}
```

### 4. Template HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>App</title>
</head>
<body>
    <div id="app"></div>
    <script type="application/json" data-page="app">{dataPage}</script>
    <script src="/assets/app.js"></script>
</body>
</html>
```

### 5. Frontend (Vue + Inertia)

```js
import { createInertiaApp } from '@inertiajs/vue3'
import { createApp, h } from 'vue'

createInertiaApp({
  resolve: name => {
    const pages = import.meta.glob('./pages/**/*.vue', { eager: true })
    return pages[`./pages/${name}.vue`]
  },
  setup({ el, App, props, plugin }) {
    createApp({ render: () => h(App, props) })
      .use(plugin)
      .mount(el)
  },
})
```

## Características

- [x] PageObject con todos los campos Inertia v3 (16 campos)
- [x] Partial reloads (X-Inertia-Partial-Component/Data/Except, X-Inertia-Reset)
- [x] Deferred props con grupos (Map<String, List<String>>)
- [x] Optional props (`optional(...)` — solo se resuelven si el partial reload las pide explícitamente)
- [x] Merge props — append (`mergeProps`), prepend (`prependProps`), deep (`deepMergeProps`), `matchPropsOn`
- [x] Once props con custom key y expiración (`X-Inertia-Except-Once-Props`)
- [x] Scroll props + `X-Inertia-Infinite-Scroll-Merge-Intent` (append/prepend)
- [x] Shared props
- [x] Rescued props (fallos silenciosos)
- [x] Meta del page (`inertia.meta(...)`) renderizable en el root template (`pageMeta`/`pageTitle`)
- [x] `encryptHistory`/`clearHistory`/`preserveFragment` (omitidos si `false`)
- [x] Redirect interno → 303 PUT/PATCH/DELETE, 302 GET
- [x] Redirect externo → 409 + X-Inertia-Location (solo GET+302)
- [x] Fragment redirect → 409 + X-Inertia-Redirect
- [x] Empty response → redirect a referer
- [x] Version mismatch → 409 + X-Inertia-Location + X-Inertia-Version (flash preservado)
- [x] Version strategies: sha256, vite-manifest, custom
- [x] Precognition header-based (`Precognition`, `Precognition-Validate-Only`) — 204 éxito / 422 errores
- [x] Prefetch (`Purpose: prefetch` / `X-Inertia-Prefetch`)
- [x] CSRF on-by-default (`inertia.csrf.enabled`, default `true`) — 419 en mismatch
- [x] Flash data via FlashStore SPI (sesión Vert.x)
- [x] AlwaysProp (errores sobreviven partial reloads)
- [x] JSON-B primario, Jackson como alternativa
- [x] Vary: X-Inertia / Precognition headers
- [x] Error-Bag y Scroll-Merge-Intent headers
- [x] camelizeProps (snake_case → camelCase)
- [x] Root template configurable (`inertia.root-template`) + `setRootView(name)` por request
- [x] Versión runtime (`version(...)`) sobre la estrategia configurada
- [x] SSR por ruta: `withoutSsr(paths)` / `disableSsr()` + `inertia.ssr-exclude-paths`
- [x] HTML + JSON responses
- [x] Native Image ready (@RegisterForReflection)

## Tests

```bash
# Adapter
mvn test                    # 79 tests (unitarios + integración)

# Demo App
cd examples/demo-app
mvn test                    # 83 tests (incluye seed de 10k registros)
```

Total: **162 tests** — todos pasan.

## Demo App

La demo-app incluye:

- **Person CRUD** — Lista paginada, búsqueda, crear/editar/eliminar
- **Employee DataTable** — Lazy loading con PrimeVue DataTable, filtros, ordenación
- **Seed data** — 10k persons + 10k employees via Java Faker (seed 42)
- **Validación** — Hibernate Validator + Precognition
- **Flash messages** — Success/error vía Inertia flash
- **PrimeVue 4** — Aura theme, Toast, Confirmation

Para ejecutar:

```bash
cd examples/demo-app
mvn quarkus:dev
# Abrir http://localhost:8080/persons
#      http://localhost:8080/employees
```

## Protocolo Inertia v3

El adaptador implementa el protocolo Inertia v3 según la especificación y validado contra los adaptadores oficiales (Laravel, Rails, Phoenix). El PageObject incluye todos los campos requeridos:

| Campo | Tipo | Siempre presente |
|-------|------|------------------|
| `component` | String | sí |
| `props` | Object | sí |
| `url` | String | sí |
| `version` | String | sí |
| `deferredProps` | Map | no |
| `mergeProps` | String[] | no |
| `prependProps` | String[] | no |
| `deepMergeProps` | String[] | no |
| `matchPropsOn` | String[] | no |
| `onceProps` | Map | no |
| `scrollProps` | Map | no |
| `sharedProps` | String[] | no |
| `rescuedProps` | String[] | no |
| `meta` | Object | no |
| `encryptHistory` | boolean | no (omitido si `false`) |
| `clearHistory` | boolean | no (omitido si `false`) |
| `preserveFragment` | boolean | no (omitido si `false`) |

## Equivalencias con los adaptadores oficiales

| quarkus-inertia | inertia-laravel | inertia-rails |
|-----------------|-----------------|---------------|
| `render(component, props)` | `Inertia::render()` | `render inertia: {...}` |
| `redirect(url)` / `back()` | `redirect()` / `back()` | `redirect_to` / `redirect_back` |
| `back(fallback)` / `back(status, fallback)` | `back(status, headers, fallback)` | `redirect_back` |
| `location(url)` (409 + `X-Inertia-Location`) | `Inertia::location()` | `inertia_location()` |
| `version(version)` / `getVersion()` | `Inertia::version()` / `getVersion()` | `inertia_version` |
| `setRootView(name)` | `Inertia::setRootView()` | `inertia_layout` |
| `share(key, value)` / `share(map)` | `Inertia::share()` | `inertia_share` |
| `getShared()` / `flushShared()` | `Inertia::getShared()` / `flushShared()` | — |
| `always(key, value)` | `Inertia::always()` | `always_prop` |
| `deferred(group, name, resolver)` | `Inertia::defer(cb, group)` | `defer` |
| `optional(key, resolver)` | `Inertia::optional()` | `optional_prop` |
| `once(key, value[, customKey])` | `Inertia::once()` / `shareOnce()` | `once_prop` |
| `merge(key, value[, deep])` / `prepend(key, value)` | `Inertia::merge()` / `prepend()` | `merge_prop` |
| `scroll(key, metadata)` | `Inertia::scroll()` | `scroll_prop` |
| `rescue(key)` | `Inertia::rescue()` | — |
| `meta(key, value)` | props `meta` | `inertia_meta_tags` |
| `flash(key, value)` | `Inertia::flash()` | `inertia_flash` |
| `withoutSsr(paths)` / `disableSsr()` | `Inertia::withoutSsr()` / `disableSsr()` | — |
| `encryptHistory/clearHistory/preserveFragment` | igual | igual |

Diferencias de ergonomía (intencionales): las props no se pueden pasar como
callables dentro del mapa de props (estilo Laravel/Rails); en su lugar se
registran explícitamente con `deferred`/`optional`/`once`/`merge` antes del
`render`. SSR global vía configuración (`inertia.ssr-enabled`) sin exclusión
por ruta.
