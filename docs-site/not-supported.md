# Not supported

Honest scope for 0.0.5 (see the full
[page](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/docs/NOT_SUPPORTED.md)):

- Explicit `append()` API and multi-message error bags close inside 0.0.5
  (PROTO-053B/054B, PARCIAL at branch start).
- Instant-visit `sharedProps` client behavior closes inside 0.0.5 (PROTO-057B).
- Legacy v1/v2 `<div data-page>` template: unsupported by design (v3-only, no opt-in).
- Quarkus Reactive Routes: stable target in 0.0.5, same contract as the other
  transports (parity + stress + 9-cell E2E in this release).
- No Inertia DevTools module (the extension already interops).
- No full-JPA-entity props (use DTOs).
