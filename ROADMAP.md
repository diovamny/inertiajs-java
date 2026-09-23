# Roadmap

Public, verifiable direction for the Inertia.js Java adapters. Items are done
only when CI evidence exists; see [protocol compatibility](docs/protocol-compatibility.md)
and [compatibility policy](docs/COMPATIBILITY_POLICY.md).

## Now (0.0.5 — release branch `release-0.0.5`)

- v3-only scope (no `2.x` client, no legacy `<div data-page>` mode): Spring Boot 4.1 /
  Quarkus 3.39 / Java 21 baseline green on **3 stable transports** (Spring MVC,
  Quarkus REST, Quarkus Reactive Routes): unit suites, TCK on 3 stacks,
  kitchen-sink demos, Playwright 9-cell contracts, archetype generation.
- Publish `0.0.5` to Maven Central (namespace `io.github.diovamny`) with signed
  artifacts and `META-INF/LICENSE`. Version single source: `version.properties`
  (`project.version=0.0.5`); `node scripts/check-versions.mjs` fails the build
  on any mismatch (pom, README, CHANGELOG, ROADMAP, archetypes, examples).
- P100-01 (M0): compatibility policy, frozen snapshot
  `specs/inertia-v3-baseline-2026-09-23.yaml`, honest matrix (PROTO-053B/054B/057B
  as PARCIAL/IMPLEMENTADO, Reactive as stable target).
- P100-02/03 (M1): Maven Wrapper, locked versions, clean-clone CI
  Windows/Linux, traceable evidence + generated E2E matrix.
- P100-04/05 (M2): `ValidationErrors` multi-message on 3 transports + TCK/E2E.
- P100-06/07 (M3): `MergePlan` explicit `append()` + nested routes + TCK/E2E.
- P100-10/11 (M5): `SsrEndpointPolicy` fail-fast + controlled sidecar + docs.
- P100-17/18 (M7): Reactive parity (full TCK, `reactive-stress`, 9-cell E2E).
- P100-08/09 (M4): official Vue/React/Svelte fixtures, 9-cell E2E green.
- P100-12–16 (M6+M8): minimal core extraction, blocking PIT/SCA fail-closed
  (Dependency-Check 12.1.0), SBOM per release, external review, RC.
- Harden quality gates: project-specific Checkstyle ruleset with
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
- Inertia v2 client support or a legacy bootstrap mode (v3-only product).
