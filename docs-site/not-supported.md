# Not supported

Honest scope for 0.0.5 (see the full
[page](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/docs/NOT_SUPPORTED.md)):

- Explicit `append()` API (PROTO-054B, `TESTED`) and multi-message error bags
  (PROTO-053B, `TESTED`): closed on 3 transports + E2E Vue/Spring; remaining
  E2E cells land in M4b.
- Instant-visit `sharedProps` client behavior (PROTO-057B): observed on
  Vue/Spring (M4a); full E2E in M4b.
- Legacy v1/v2 `<div data-page>` template: unsupported by design (v3-only, no opt-in).
- Quarkus Reactive Routes: graduates stable inside 0.0.5 — TCK 43/43 +
  stress green; graduation completes with the Fase F fixes + full E2E (see the
  [reactive parity](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/docs/reactive-parity.md) matrix).
- No Inertia DevTools module (the extension already interops).
- No full-JPA-entity props (use DTOs).
