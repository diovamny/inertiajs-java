# Not Supported

This page documents what the Java adapters intentionally do **not** implement in
`0.0.6`, and why. The scope is v3 only: there is no legacy v1/v2 mode and no
`2.x` compatibility layer.

An unsupported feature is not necessarily a missing feature. Some are deliberate
security or architectural decisions; others are simply outside the server
adapter's responsibility.

## Legacy root template

The v1/v2 pattern put the page object on the mount element:

```html
<!-- v1/v2, not supported -->
<div id="app" data-page="{...}"></div>
```

v3 requires the page object in a separate JSON script and forbids `data-page` on
the mount element. The adapter implements the v3-pure contract only, with no
opt-in. This removes a class of HTML-escaping bugs and reduces the bootstrap by
roughly 59%.

Use `__INERTIA_ROOT__` or the explicit v3 placeholders. See
[Root Template](/guide/root-template).

## Inertia DevTools server protocol

The Inertia DevTools browser extension defines server endpoints and internal
hook introspection (`/__inertia_devtools`, request-correlation headers,
real-time prop inspection). Those endpoints are implemented specifically in the
Laravel and Rails adapters.

The Java adapters do not expose them. The decision is documented in **ADR-007**
and is driven by:

- minimizing the production attack surface in corporate environments;
- avoiding an unauthenticated introspection endpoint that can dump props;
- keeping the runtime free of development-only telemetry.

The extension still works in its basic mode: it inspects page objects at the
network level, which any correct v3 server already emits. What is missing is the
extra server endpoint, not the protocol.

## Frontend behavior

Polling, prefetching, infinite-scroll rendering and UI state are client-side
features of the official `@inertiajs/*` packages. The Java adapter provides the
wire metadata they need (`scrollProps`, merge intent, live props) but does not
implement the browser behavior.

Test these behaviors with Playwright against a real browser; a JVM test can only
assert the metadata.

## Configurable JPA guardrails

Serializing full JPA entities as props is discouraged:

- lazy-loading can trigger hidden N+1 queries during serialization;
- private fields may leak through reflection-based serializers;
- bidirectional relations can recurse or explode payload size.

There is no automatic entity filter. Use DTOs, records or projections. This is a
design choice: a silent filter that drops fields would make behavior harder to
reason about than an explicit DTO.

## Remote SSR sidecars

Remote SSR is supported, but only with explicit validation
(`SsrEndpointPolicy`, fail-fast at startup) plus HTTPS and an allowlist:

```properties
inertia.ssr-url=https://ssr.internal.example.com/render
inertia.ssr-remote-enabled=true
inertia.ssr-allowed-hosts=ssr.internal.example.com
```

The default posture is localhost. A remote sidecar receives every resolved prop
and its HTML is injected without sanitization, so it must be treated as part of
the trusted server boundary. See [SSR](/guide/ssr#trust-boundary).

## Additional gaps

| Area | Status | Notes |
|---|---|---|
| Legacy v1/v2 protocol | Not supported | No opt-in; v3-pure only |
| DevTools server endpoints | Not supported | ADR-007 |
| Server-side frontend state | Out of scope | Client responsibility |
| Automatic JPA entity filtering | Not implemented | Use DTOs |
| PIT mutation gate on Windows | Not blocking locally | Runs on Linux CI |
| Remote SSR without allowlist | Rejected at startup | Security policy |
| `data-page` on the mount div | Rejected | v3 contract |

## What "not supported" means for upgrades

- A feature listed here will not appear in a patch release without a documented
  ADR and release note.
- Do not build a workaround that depends on internal adapter classes; they are
  not a public API.
- If you need one of these features, open an issue with the use case. Scope can
  change, but not silently.

## Honest compatibility claims

The project avoids claiming "100% compatible". Compatibility is tracked as two
separate measurements:

- **C100 contract**: normative TCK cases green on all transports (69/69 in
  `0.0.6`).
- **I100 interop**: cells observed with real official clients (92/92 in
  `0.0.6`).

A contract pass is not interop evidence. The generated matrix lives under
`specs/` in the repository and is regenerated from the YAML suites. If a row is
missing or marked `NO_APLICA`, the reason is recorded there.

## Related pages

- [Protocol](/reference/protocol) — what the adapter does implement.
- [SSR](/guide/ssr) — trust boundary and fallback.
- [Security](/guide/security) — what the adapter does and does not protect.
- [Migration](/reference/migration) — upgrade path and removed APIs.

Next: [Migration](/reference/migration) · [Changelog](/reference/changelog)
