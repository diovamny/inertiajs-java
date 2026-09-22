# Migration

## Versioning policy

- `0.x` releases may change the public API (`Inertia` facade, SPIs, properties).
  Check the [CHANGELOG](../CHANGELOG.md) before upgrading.
- `1.0.0` will mark a stable public API backed by the green contractual suite
  ([protocol compatibility](protocol-compatibility.md)).

## Upgrading the adapters

Artifacts are published to Maven Central as `io.github.diovamny.spring.inertia`
and `io.github.diovamny.quarkus.inertia` (group `io.github.diovamny`). Bump the version
in your `pom.xml`; no code changes are needed for patch releases.

## 0.0.3 → 0.0.4 (breaking)

- **Root template**: delete the `data-page` attribute from
  `<div id="app">`. The page object lives only in
  `<script type="application/json" data-page="app">`
  (`__INERTIA_PAGE_JSON__` placeholder). Legacy `__INERTIA_PAGE__`
  placeholders render empty.
- **`InertiaConfig` (Quarkus) manual implementations**: the new
  `max-page-bytes` method (default 32 MiB) must be implemented
  (`long maxPageBytes() { return 33554432L; }`). CDI-managed configs are
  unaffected.
- **New property** `inertia.max-page-bytes` (default `33554432`, `-1`
  disables): oversized pages now fail with `413` instead of serving
  unbounded payloads.
- **Redirect targets** with control characters or `javascript:`/`data:`/
  `vbscript:`/`file:` schemes are rejected (Spring `400`, Quarkus `400`
  via the error mapper) before any header ships.
- CSRF contract documented as `303 + flash` (behavior unchanged).

## Upgrading a starter

Regenerate is not required: starters are plain Maven projects. To pick up a
new adapter, update the `inertia-adapter.version` property; to pick up new
starter files (e.g. `Dockerfile` improvements), compare with a fresh
generation and copy what you need.
