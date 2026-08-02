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
├── model/         → PageObject, AlwaysProp, DeferredProp
├── protocol/      → Filtros, builders, procesadores (núcleo HTTP)
├── renderer/      → HtmlRenderer, SsrHandler
├── response/      → JsonResponseProcessor
├── security/      → InertiaCsrfFilter
├── spi/           → FlashStore, JsonProvider
├── version/       → VersionProvider, DefaultVersionProvider
├── internal/      → InertiaImpl, JsonbJsonProvider, JacksonJsonProvider
└── util/
```

## Uso

### 1. Añadir dependencia

```xml
<dependency>
    <groupId>com.quarkus.inertia</groupId>
    <artifactId>quarkus-inertia</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 2. Configurar

```properties
inertia.root-template=index.html
inertia.version-strategy=custom
inertia.version-custom=1.0.0
inertia.encrypt-history=false
inertia.camelize-props=false
inertia.precognition-enabled=false
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
- [x] Merge props (append/prepend) — `prependProps`
- [x] Once props con custom key
- [x] Scroll props (infinite scroll merge)
- [x] Shared props
- [x] Rescued props (fallos silenciosos)
- [x] Meta tags via props
- [x] `encryptHistory`/`clearHistory`/`preserveFragment` siempre presentes
- [x] Redirect interno → 303 PUT/PATCH/DELETE, 302 GET
- [x] Redirect externo → 409 + X-Inertia-Location (solo GET+302)
- [x] Fragment redirect → 409 + X-Inertia-Redirect
- [x] Empty response → redirect a referer
- [x] Version mismatch → 409 + X-Inertia-Location + X-Inertia-Version
- [x] Version strategies: sha256, vite-manifest, custom
- [x] Precognition (X-Inertia-Precognition) — 204 éxito / 422 errores
- [x] CSRF token session-based (XSRF-TOKEN → X-CSRF-TOKEN)
- [x] Flash data via FlashStore SPI (sesión Vert.x)
- [x] AlwaysProp (errores sobreviven partial reloads)
- [x] JSON-B primario, Jackson como alternativa
- [x] Vary: X-Inertia header
- [x] Partial-Except-Once-Props, Error-Bag, Scroll-Merge-Intent headers
- [x] camelizeProps (snake_case → camelCase)
- [x] HTML + JSON responses con `@Produces` override
- [x] Native Image ready (@RegisterForReflection)

## Tests

```bash
# Adapter
cd quarkus-inertia
mvn test                    # 63 tests (unitarios + integración)

# Demo App
cd examples/demo-app
mvn test                    # 22 tests (incluye seed de 10k registros)
```

Total: **85 tests** — todos pasan.

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
| `onceProps` | Map | no |
| `scrollProps` | Map | no |
| `sharedProps` | String[] | no |
| `rescuedProps` | Map | no |
| `meta` | Object | no |
| `encryptHistory` | boolean | sí |
| `clearHistory` | boolean | sí |
| `preserveFragment` | boolean | sí |
