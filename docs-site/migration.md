# Migration

## 0.0.x → 0.0.4

- **Breaking:** the root template `<div id="app">` no longer carries
  `data-page`. Delete the attribute; the page object lives only in
  `<script type="application/json" data-page="app">`.
- CSRF contract is now documented as `303 + flash` (code behavior
  unchanged).
- New property `inertia.max-page-bytes` (default 32 MiB).
- New files: `version.properties`, `specs/inertia-v3-compliance.yaml`,
  `docs/NOT_SUPPORTED.md`.

## 0.0.x → 1.0

Public API freeze plus Revapi/japicmp binary checks land with the 1.0
candidate. Track `ROADMAP.md`.
