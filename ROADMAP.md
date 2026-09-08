# Roadmap

Public, verifiable direction for the Inertia.js Java adapters. Items are done
only when CI evidence exists; see [protocol compatibility](docs/protocol-compatibility.md).

## Now (0.x)

- Keep Spring Boot 4.1 / Quarkus 3.39 / Java 21 baseline green: unit suites,
  kitchen-sink demos, Playwright contracts, archetype generation.
- Publish `0.0.1` to Maven Central (namespace `io.github.dg`) with signed
  artifacts and `META-INF/LICENSE`.
- Harden quality gates gradually: project-specific Checkstyle ruleset with
  zero violations, SpotBugs clean, incremental coverage/mutation thresholds.

## Next

- `1.0.0`: stable public API (`Inertia` facade, SPIs, `inertia.*` properties)
  with the full contractual suite green, including native smoke tests.
- Optional: documentation site, Discord channel, Svelte starter kits.

## Non-goals

- Becoming an official Inertia.js project; this is a community effort.
- A production security/authentication blueprint: starters and demos are
  minimal bases, documented as such.
