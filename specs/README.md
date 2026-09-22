# Specs

Normative contract sources for the Inertia.js v3 adapters.

- [`inertia-v3-compliance.yaml`](inertia-v3-compliance.yaml) — **single source
  of truth** for protocol conformance (PLAN v4 §3.5). Every entry carries an
  `id` (`PROTO-NNN`), the exact docs section, covered transports, a status
  (`IMPLEMENTED | TESTED | E2E_VERIFIED | NOT_SUPPORTED | NOT_APPLICABLE`),
  its `test_ref` evidence and notes.
- [`../docs/protocol-compatibility.md`](../docs/protocol-compatibility.md) is
  **generated** from this file by
  [`../scripts/generate-compatibility-matrix.mjs`](../scripts/generate-compatibility-matrix.mjs)
  — never edit the markdown by hand. CI regenerates it and fails on any diff,
  and fails when a row is `IMPLEMENTED` without `test_ref`.
