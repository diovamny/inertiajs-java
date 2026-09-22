# Roadmap

Public, verifiable direction for the Inertia.js Java adapters. Items are done
only when CI evidence exists; see [protocol compatibility](docs/protocol-compatibility.md).

## Now (0.0.4 — release branch `release-0.0.4`)

- Keep Spring Boot 4.1 / Quarkus 3.39 / Java 21 baseline green: unit suites,
  kitchen-sink demos, Playwright contracts, archetype generation.
- Publish `0.0.4` to Maven Central (namespace `io.github.diovamny`) with signed
  artifacts and `META-INF/LICENSE`. Version single source: `version.properties`
  (`project.version=0.0.4`); `node scripts/check-versions.mjs` fails the build
  on any mismatch (pom, README, CHANGELOG, ROADMAP, archetypes, examples).
- Fase 0 (PLAN_MEJORA v4): CSRF `303 + flash` canónico en Spring y Quarkus,
  plantilla raíz v3 pura (sin `data-page` en `<div>`), matriz corregida (fila 38),
  módulo reactivo en estabilización.
- Harden quality gates gradually: project-specific Checkstyle ruleset with
  zero violations, SpotBugs clean, incremental coverage/mutation thresholds.

## Next

- `1.0.0`: stable public API (`Inertia` facade, SPIs, `inertia.*` properties)
  with the full contractual suite green, including native smoke tests.
- Done in `0.0.4`: documentation site (`docs-site/`, VitePress, Pages
  workflow), Svelte starter kits.
- Optional: Discord channel.

## Non-goals

- Becoming an official Inertia.js project; this is a community effort.
- A production security/authentication blueprint: starters and demos are
  minimal bases, documented as such.
