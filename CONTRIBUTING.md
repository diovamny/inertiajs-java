# Contributing to Inertia.js Java

¡Gracias por tu interés en contribuir! This project is a community effort
to bring Inertia.js v3 to the Java ecosystem (Spring Boot 4.x and Quarkus).

## Code of Conduct

Be respectful and constructive. Harassment or discrimination of any kind
is not tolerated.

## How to contribute

1. **Fork** the repository and create a branch from `main`:
   `git checkout -b feat/my-change`
2. Make your changes following the project conventions.
3. Run the full verification suite before pushing:
   ```powershell
   mvn clean test -T 1C
   mvn clean test -pl examples/spring-demo -Pexamples
   ```
4. Open a Pull Request describing the motivation and the changes.
   Reference related issues when applicable.

## Project layout

| Path | Purpose |
|---|---|
| `quarkus-inertia/` | Inertia.js v3 adapter for Quarkus 3.38.x (Mutiny / Vert.x / Qute / JAX-RS) |
| `spring-inertia/` | Inertia.js v3 adapter for Spring Boot 4.1.x (Spring MVC / AOT / GraalVM) |
| `examples/` | Demos: Quarkus (demo-app, kitchen-sink, pingcrm, pingcrm-react) and Spring Boot (spring-demo) |
| `implementation_plan.md` | Approved master plan with architecture decisions |

## Conventions

- Java 21+, UTF-8 sources, no tabs (4 spaces).
- Keep the public API surface minimal and aligned between both adapters.
- All new behavior must be covered by unit and/or MockMvc/Quarkus
  integration tests.
- Javadoc on public API members; no comments inside method bodies unless
  strictly needed.
- Artifact coordinates:
  `io.github.dg.quarkus.inertia:quarkus-inertia` and
  `io.github.dg.spring.inertia:spring-inertia` (version `0.0.1`).

## Versioning and releases

Releases follow [Semantic Versioning](https://semver.org/). The `release`
Maven profile signs artifacts (GPG) and publishes to Maven Central via the
Sonatype Central Portal:

```powershell
mvn clean package -Prelease -DskipTests
```

## Reporting issues

Use the issue templates: `bug_report.md` (with reproduction steps) and
`feature_request.md`. Include the adapter version, Java version and, for
native builds, the GraalVM version.