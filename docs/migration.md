# Migration

## Versioning policy

- `0.x` releases may change the public API (`Inertia` facade, SPIs, properties).
  Check the [CHANGELOG](../CHANGELOG.md) before upgrading.
- `1.0.0` will mark a stable public API backed by the green contractual suite
  ([protocol compatibility](protocol-compatibility.md)).

## Upgrading the adapters

Artifacts are published to Maven Central as `io.github.dg.spring.inertia`
and `io.github.dg.quarkus.inertia` (group `io.github.dg`). Bump the version
in your `pom.xml`; no code changes are needed for patch releases.

## Upgrading a starter

Regenerate is not required: starters are plain Maven projects. To pick up a
new adapter, update the `inertia-adapter.version` property; to pick up new
starter files (e.g. `Dockerfile` improvements), compare with a fresh
generation and copy what you need.
