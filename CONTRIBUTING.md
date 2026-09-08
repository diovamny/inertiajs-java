# Contributing to Inertia.js Java

Thank you for your interest in contributing! This project is a community effort
to bring Inertia.js v3 to the Java ecosystem (Spring Boot 4.x and Quarkus).
Please write issues, pull requests and changelog entries in English.

## Code of Conduct

This project follows the [Code of Conduct](CODE_OF_CONDUCT.md). By participating
you agree to abide by it. Report unacceptable behavior to diovamny@gmail.com.

## How to contribute

1. **Fork** the repository and create a branch from `main`:
   `git checkout -b feat/my-change`
2. Make your changes following the conventions below.
3. Run the verification suite before pushing:
   ```bash
   mvn clean test -T 1C
   node scripts/check-conformance-matrix.mjs
   ```
4. Open a Pull Request using the template. Reference related issues.

## Project layout

| Path | Purpose |
|---|---|
| `quarkus-inertia/` | Inertia.js v3 adapter for Quarkus 3.39.x (Mutiny / Vert.x / Qute / JAX-RS) |
| `spring-inertia/` | Inertia.js v3 adapter for Spring Boot 4.1.x (Spring MVC / AOT / GraalVM) |
| `archetypes/` | Maven archetypes: Spring/Quarkus × Vue 3/React 19 starter kits |
| `examples/` | Demo apps (kitchen-sink protocol testbeds, PingCRM ports) |
| `e2e/` | Playwright browser contract suite (shared Spring/Quarkus cases) |
| `docs/` | User guides; `docs/protocol-compatibility.md` is CI-verified |

## Conventions

- Java 21+, UTF-8 sources, 4 spaces, no tabs.
- Keep the public API surface minimal and **aligned between both adapters**:
  every protocol behavior needs the same contract test on each side.
- New behavior must be covered by unit and/or MockMvc/Quarkus integration tests.
- Javadoc on public API members.
- Artifact coordinates: `io.github.dg.quarkus.inertia:quarkus-inertia` and
  `io.github.dg.spring.inertia:spring-inertia` (version `0.0.1`).

## Versioning and releases

Releases follow [Semantic Versioning](https://semver.org/): `0.x` while the API
evolves, `1.0.0` for a stable API with a green contractual suite. The `release`
Maven profile signs artifacts (GPG) and publishes to Maven Central:

```bash
mvn clean deploy -Prelease -DskipTests
```

Document user-facing changes in `CHANGELOG.md` (Keep a Changelog, English).

## Reporting issues

Use the issue templates (`bug_report.md` with reproduction steps,
`feature_request.md`). Include the adapter version and the Java version.
**Never report security vulnerabilities in public issues** — see
[.github/SECURITY.md](.github/SECURITY.md).
