# Governance

How decisions are made, how the roadmap advances, and what new
contributors must satisfy. This document covers the whole
`inertiajs-java` ecosystem (`inertia-core`, both adapters, the security
integrations, the TCK, demos and archetypes).

## Roles

- **Maintainer(s):** own the roadmap, review pull requests and cut
  releases. Today: `@diovamny`.
- **Contributors:** anyone opening issues or pull requests. No CLA is
  required; contributions land under the repository MIT license.
- **Security contact:** see `.github/SECURITY.md` for private reporting.
  Vulnerabilities are fixed on a private branch, released, and only then
  disclosed.

## Decision making

- Small, well-scoped changes (docs, tests, bug fixes with regression
  coverage) need one maintainer approval.
- Public API changes (new packages, signature changes, new modules,
  protocol behavior) need an issue describing the Laravel/Rails parity
  reference first, then one maintainer approval. Breaking changes require
  a minor-version bump proposal and a migration note in `CHANGELOG.md`.
- Disagreements are resolved by the maintainers with a written rationale
  in the issue. Silence never blocks: a proposal without objections for
  14 days may proceed.

## Roadmap

- The normative roadmap is `implementation_plan.md` (gaps G-01…G-21 by
  phase). A gap counts as closed only when: implementation exists in
  **both** adapters (or in `inertia-core`), executable tests cover it,
  the TCK covers the wire behavior where applicable, and the
  conformance matrix plus docs reference the covering tests.
- New gaps need: severity, official-adapter reference (Laravel/Rails
  version + behavior), and acceptance tests before implementation.

## Contribution requirements

1. Java 21, Maven wrapper builds; `mvn clean test -T 1C` green.
2. New public API carries javadoc; new behavior carries tests in the
   owning module (unit) and, for wire behavior, TCK YAML cases.
3. No secrets in the tree (HMAC keys arrive via environment).
4. Static analysis clean on library modules
   (`maven-checkstyle-plugin:check` with `config/checkstyle`).
5. Examples stay green; archetype changes must still generate and build
   (`-Parchetypes` plus a local `archetype:generate` smoke test for new
   starters).

## Releases

- Versions follow SemVer; pre-1.0 breaking changes bump the minor part.
- Releases ship from tags (`v*`) through `.github/workflows/release.yml`:
  full suite, GPG-signed artifacts with sources and javadoc, CycloneDX
  SBOM, SLSA provenance, publish via the Central Portal
  (`central-publishing-maven-plugin`), GitHub release with artifacts.
- The release manager verifies the Central Portal deployment and the
  `CHANGELOG.md` entry before announcing.
