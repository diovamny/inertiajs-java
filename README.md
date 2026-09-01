# Inertia.js v3 for Java

Server-side Inertia.js v3 adapters for the Java ecosystem, with GraalVM Native support.

| Artefacto | Framework | JAR |
|---|---|---|
| Quarkus | Quarkus 3.38.x (reactive) | quarkus-inertia-0.0.1.jar |
| Spring | Spring Boot 4.1.x (Spring MVC) | spring-inertia-0.0.1.jar |

---

## Protocol Parity vs. Official Adapters

Both adapters achieve **full Inertia.js v3 protocol compliance**.

| Feature | Laravel | Rails | Spring | Quarkus |
|---------|:-------:|:-----:|:------:|:-------:|
| Page object | YES | YES | YES | YES |
| Partial reloads | YES | YES | YES | YES |
| Version mismatch 409 | YES | YES | YES | YES |
| 303 redirects | YES | YES | YES | YES |
| Shared data | YES | YES | YES | YES |
| InertiaSharedDataContributor SPI | YES | YES | YES | YES |
| Always props | YES | YES | YES | YES |
| Deferred / lazy props | YES | YES | YES | YES |
| Once props | YES | YES | YES | YES |
| Merge / deep-merge / prepend props | YES | YES | YES | YES |
| Optional props | YES | YES | YES | YES |
| matchPropsOn / scrollProps | YES | YES | YES | YES |
| Rescued props | YES | YES | YES | YES |
| encryptHistory / clearHistory | YES | YES | YES | YES |
| preserveFragment | YES | YES | YES | YES |
| Validation errors + error bags | YES | YES | YES | YES |
| Precognition (422) | YES | YES | YES | YES |
| CSRF protection | YES | YES | YES | YES |
| Flash data | YES | YES | YES | YES |
| SSR (Node.js sidecar) | YES | YES | YES | YES |
| viewData (root template injection) | YES | YES | YES | YES |
| Convention component resolution | YES | YES | YES | YES |
| Prefetching | YES | YES | YES | YES |
| Infinite scroll | YES | YES | YES | YES |
| GraalVM Native Image | N/A | N/A | YES | YES |
| Testing DSL (InertiaPage) | YES | YES | YES | YES |

**Test coverage:** 112 tests (Spring) + 247 tests (Quarkus) = **359 total, 0 failures**.

---

## Requirements

- Java 21+
- Maven 3.9+
- Node.js 22+ (demos only)

---

## Quickstart Spring Boot

`xml
<dependency>
    <groupId>io.github.dg.spring.inertia</groupId>
    <artifactId>spring-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
`

application.properties:

`properties
inertia.version-custom=1.0.0
inertia.ssr-enabled=false
inertia.csrf-enabled=true
inertia.convention-routing-enabled=true
inertia.convention-routing-prefix=Pages/
`

Controller:

`java
@GetMapping("/")
public Object index() {
    inertia.viewData("title", "Dashboard | My App");
    return inertia.render("Dashboard", Map.of("stats", Map.of("contacts", 24)));
}
`

Global Shared Data SPI:

`java
@Component
public class AppSharedData implements InertiaSharedDataContributor {
    @Override
    public void contribute(Inertia inertia) {
        inertia.share("auth", Map.of("user", currentUser()));
    }
}
`

---

## Quickstart Quarkus

`xml
<dependency>
    <groupId>io.github.dg.quarkus.inertia</groupId>
    <artifactId>quarkus-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
`

application.properties:

`properties
quarkus.inertia.root-template=index.html
quarkus.inertia.version-custom=1.0.0
quarkus.inertia.convention-routing-enabled=true
quarkus.inertia.convention-routing-prefix=Pages/
`

Resource:

`java
@GET
public Response index() {
    inertia.viewData("title", "Dashboard | My App");
    return inertia.render("Pages/Home", Map.of("users", List.of()));
}
`

---

## Documentation

| Guide | Description |
|-------|-------------|
| docs/testing-guide.md | MockMvc (Spring) and REST-assured (Quarkus) with InertiaPage DSL |
| docs/shared-data-and-props.md | InertiaSharedDataContributor SPI, all prop strategies |
| docs/viewdata-guide.md | Root template data injection, placeholder substitution |
| docs/ssr-setup.md | Node.js sidecar SSR setup, config, fallback |
| docs/conformance-matrix.md | Full protocol compliance test matrix |

---

## Build

`powershell
mvn clean test                        # 359 tests total
mvn clean test -pl spring-inertia     # 112 tests
mvn clean test -pl quarkus-inertia    # 247 tests
mvn clean package -Prelease -DskipTests
`

## License

Apache License 2.0 - see LICENSE.