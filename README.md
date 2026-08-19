# Inertia.js v3 for Java

[![CI](https://github.com/dg/inertia-java/actions/workflows/ci.yml/badge.svg)](https://github.com/dg/inertia-java/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://adoptium.net)

Server-side Inertia.js v3 adapters for the Java ecosystem, with GraalVM
Native support. Two adapters, one API design:

| Artefacto | Coordenadas Maven | Framework | JAR |
|---|---|---|---|
| Quarkus | `io.github.dg.quarkus.inertia:quarkus-inertia:0.0.1` | Quarkus 3.38.x (reactive) | `quarkus-inertia-0.0.1.jar` |
| Spring | `io.github.dg.spring.inertia:spring-inertia:0.0.1` | Spring Boot 4.1.x (Spring MVC) | `spring-inertia-0.0.1.jar` |

> **English:** Server-side Inertia.js v3 adapters for Java (Quarkus 3.38
> and Spring Boot 4.1), Java 21+, with full GraalVM Native support.

## Requisitos / Requirements

- Java 21+
- Maven 3.9+
- Node.js 22+ (solo para las demos Quarkus / only for Quarkus demos)

## Features (paridad v3)

- Render de páginas JSON/HTML, partial reloads (`X-Inertia-Partial-Data/Except`), version mismatch `409`.
- Props v3: `always`, `shared`, `deferred` (grupos), `once`, `merge`/`prepend`/`deepMerge` con `matchPropsOn`, `optional`, `cached`, `rawJson`, scroll props, rescue props.
- Redirects estilo Laravel: `redirect`, `back(fallback)`, `location` (full page), encadenado con `.with(...)` / `.withErrors(...)` / `.withInput(...)`.
- Flash data por sesión, shared data por request, ETag lazy (`304`), headers custom.
- Validación: `@Valid` / `ConstraintViolationException` → flash de errors + redirect back; **Precognition** → `422` con errores de campo.
- CSRF con cookie `XSRF-TOKEN` (`419` en mismatch), comparación en tiempo constante.
- SSR vía servidor externo (`ssr-url`), con exclusión por path/request.
- GraalVM Native: hints AOT automáticos en ambos adaptadores.
- Helpers de testing: `InertiaPage` (Quarkus) e `InertiaPage`/`InertiaResultMatchers` (Spring).

---

## Quickstart Spring Boot

### 1. Dependencia

```xml
<dependency>
    <groupId>io.github.dg.spring.inertia</groupId>
    <artifactId>spring-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. Configuración (`application.properties`)

```properties
inertia.version-custom=1.0.0
inertia.ssr-enabled=false
inertia.csrf-enabled=true
```

### 3. Controlador

```java
@Controller
public class DashboardController {

    private final Inertia inertia;

    public DashboardController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/")
    public Object index() {
        inertia.deferred("dashboard", "monthlyStats", this::monthlyStats);
        inertia.once("welcome", "Hola");
        return inertia.render("Dashboard", Map.of(
            "stats", Map.of("contacts", 24),
            "welcome", inertia.shared("welcome")
        ));
    }
}
```

### 4. Formulario con validación y Precognition

```java
@PostMapping("/contacts")
public Object store(@Valid ContactForm form) {
    contacts.save(new Contact(contacts.nextId(), form.name(), form.email(), form.phone()));
    inertia.flash("success", "Contacto creado.");
    return inertia.redirect("/contacts");
}
```

Los errores de `@Valid` se flashean y redirigen de vuelta (prop `errors` en el
siguiente render); con `X-Inertia-Precognition: true` el servidor responde
`422` con `{errors: {campo: mensaje}}`.

Demo completa: [`examples/spring/spring-pingcrm`](examples/spring/spring-pingcrm) (Vue 3).

---

## Quickstart Quarkus

### 1. Dependencia

```xml
<dependency>
    <groupId>io.github.dg.quarkus.inertia</groupId>
    <artifactId>quarkus-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. Configuración (`application.properties`)

```properties
inertia.root-template=index.html
inertia.version-strategy=custom
inertia.version-custom=1.0.0
inertia.csrf.enabled=true
```

### 3. Recurso JAX-RS

```java
@Path("/")
public class DashboardResource {

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> index() {
        return inertia.render("Pages/Home", Map.of("users", List.of()));
    }
}
```

Demos Quarkus: [`examples/quarkus/kitchen-sink`](examples/quarkus/kitchen-sink) (showcase integral),
[`examples/quarkus/demo-app`](examples/quarkus/demo-app),
[`examples/quarkus/pingcrm`](examples/quarkus/pingcrm),
[`examples/quarkus/pingcrm-react`](examples/quarkus/pingcrm-react).

Demos Spring: [`examples/spring/spring-pingcrm`](examples/spring/spring-pingcrm),
[`examples/spring/spring-kitchen-sink`](examples/spring/spring-kitchen-sink) (port
del showcase Quarkus).

---

## Build y verificación

```powershell
# Suite completa de ambos adaptadores
mvn clean test -T 1C

# Módulos por separado
mvn clean test -pl spring-inertia
mvn clean test -pl quarkus-inertia

# Demo Spring (pingcrm)
mvn clean test -pl examples/spring/spring-pingcrm -Pexamples

# Demo Spring (kitchen-sink)
mvn clean test -pl examples/spring/spring-kitchen-sink -Pexamples

# JARs de release (sources + javadoc)
mvn clean package -Prelease -DskipTests
```

## Licencia

Apache License 2.0 — ver [`LICENSE`](LICENSE).

## Contribuciones

Ver [`CONTRIBUTING.md`](CONTRIBUTING.md) y el
[`CHANGELOG.md`](CHANGELOG.md). El plan maestro de arquitectura vive en
[`implementation_plan.md`](implementation_plan.md).