# Protocol

Inertia.js v3 is a small protocol between a server adapter and the official
client. The Java adapters implement the same wire contract as the Laravel and
Rails adapters; the frontend cannot tell which backend it is talking to.

This page is the reference for the bytes on the wire. For the conceptual model,
read [How It Works](/guide/how-it-works).

## Request headers

| Header | Sent when | Meaning |
|---|---|---|
| `X-Inertia: true` | Every visit after the first page load | Client expects JSON, not HTML |
| `X-Inertia-Version` | Every Inertia visit | Asset version the client has loaded |
| `X-Inertia-Partial-Component` | Partial reload | Component the client is currently on |
| `X-Inertia-Partial-Data` | Partial reload with `only` | Comma-separated prop keys to include |
| `X-Inertia-Partial-Except` | Partial reload with `except` | Comma-separated prop keys to exclude |
| `X-Inertia-Reset` | Partial reload | Comma-separated keys whose merge state resets |
| `X-Inertia-Except-Once-Props` | Inertia visit | Once props already present on the client |
| `X-Inertia-Infinite-Scroll-Merge-Intent` | Infinite scroll | `append` or `prepend` |
| `X-Inertia-Error-Bag` | Inertia visit | Named validation error bag |
| `X-Inertia-Precognition` | Precognition request | `true` to validate without executing |
| `X-Inertia-Precognition-Validate-Fields` | Precognition request | Fields to validate |

The adapter treats a request as an Inertia visit only when `X-Inertia: true`
and the method is not a navigation the client handles itself. A partial reload
also requires `X-Inertia-Partial-Component` to match the rendered component;
otherwise it is served as a normal visit.

## Response headers

| Header | Value |
|---|---|
| `X-Inertia` | `true` on JSON page responses |
| `Vary` | `X-Inertia`, `X-Inertia-Version`, partial and once headers as applicable |
| `X-Inertia-Location` | Absolute or same-origin URL on `409` control responses |
| `X-Inertia-Version` | Current asset version on `409` |
| `ETag` / `If-None-Match` | `304` support when `inertia.lazy-etag-enabled=true` |

A JSON page response always carries exactly one `X-Inertia: true`. A `409`
control response never does: the client must perform a full navigation.

## Page object

```json
{
  "component": "Contacts/Index",
  "props": {
    "contacts": [{ "id": 1, "name": "Ada" }],
    "auth": { "user": { "name": "ada" } },
    "errors": {}
  },
  "url": "/contacts?page=2",
  "version": "8f14e45f",
  "encryptHistory": false,
  "clearHistory": false,
  "preserveFragment": false,
  "mergeProps": ["contacts"],
  "deferredProps": { "slow": ["stats"] },
  "onceProps": { "notice": { "prop": "notice", "expiresAt": null } },
  "scrollProps": { "contacts": { "pageName": "page", "currentPage": 2 } },
  "liveProps": {},
  "sharedProps": { "auth": {} },
  "preserveBigIntegers": false
}
```

Required fields are `component`, `props`, `url` and `version`. Optional metadata
fields are omitted when empty, so a simple page stays small.

- `component` — the frontend page component name, resolved by the client.
- `props` — the resolved prop map. Page props win over shared props.
- `url` — the current URL including query string, relative to the app root.
- `version` — asset version used for cache busting.

## Status codes

| Status | Situation |
|---|---|
| `200` | Normal page, JSON or HTML |
| `302` | GET redirect (framework behavior) |
| `303` | Redirect after a state-changing request; CSRF failure redirect |
| `304` | Conditional request matched the ETag |
| `409` | Version mismatch, external redirect on an Inertia visit, or authentication challenge |
| `413` | Serialized page exceeds `inertia.max-page-bytes` |
| `419` | Legacy CSRF failure on non-Inertia Quarkus requests |
| `422` | Validation failure on a Precognition request |
| `500` | Error page (`inertia.error-status`) |

`303` after mutations is mandatory. A `302` after `POST` makes the browser
re-submit the request on refresh; the adapter normalizes redirects created by
the framework.

## Redirects

- Same-origin redirect: `302` for `GET`, `303` for mutations.
- External URL on an Inertia visit: `409 + X-Inertia-Location`, which forces a
  full page load.
- `back()` redirects to the `Referer` when it is same-origin, otherwise to a
  safe fallback.
- `javascript:`, `data:` and CRLF-bearing targets are rejected.

## Partial reloads

The client sends `only` or `except`, never both. The server:

1. Resolves shared props and page props as usual.
2. Filters the top-level keys, honoring dot notation (`auth.user`).
3. Always includes `always` props and validation errors.
4. Resolves optional props only when explicitly selected.
5. Returns merge metadata only for props present in the response.

A partial reload is a normal `200` JSON response. A mismatch in
`X-Inertia-Partial-Component` promotes it to a full visit.

## Deferred, once and merge props

| Feature | Wire metadata |
|---|---|
| Deferred | `deferredProps: { group: [keys] }` |
| Once | `onceProps: { key: { prop, expiresAt } }` plus `X-Inertia-Except-Once-Props` |
| Merge | `mergeProps`, optional `prependProps`, `deepMergeProps`, `matchPropsOn` |
| Scroll | `scrollProps` with page name and current page |
| Live | `liveProps` with channels, events, throttle and initial value |

Deferred props are resolved on a follow-up partial reload. Once props are
tracked by the client through the `Except-Once-Props` header, not by the
session. Merge props are combined client-side; `X-Inertia-Reset` clears the
previous value before merging.

## Version negotiation

The client sends `X-Inertia-Version`. When it differs from the server version:

```http
HTTP/1.1 409 Conflict
X-Inertia-Location: /contacts
X-Inertia-Version: 8f14e45f
```

The client discards the response and performs a full navigation. No page body
is returned, and `X-Inertia: true` is absent by design.

Version strategies: `custom` > `vite-manifest` > `sha256` > startup fallback.
See [Asset Versioning](/guide/asset-versioning).

## HTML bootstrap

The initial HTML response contains the same page object exactly once, inside a
script tag, plus a mount element:

```html
<div id="app">__INERTIA_SSR_BODY__</div>
<script type="application/json" data-page="app">__INERTIA_PAGE_JSON__</script>
```

The v3-pure contract forbids `data-page` on the mount element. The page JSON is
escaped for script context (`<`, `>`, `&`, U+2028, U+2029) so a prop cannot
break out of the script tag. See [Root Template](/guide/root-template).

## BigInt

With `inertia.bigint-enabled=true` or a per-response opt-in, unsafe integers
travel as `{"$bigint":"9007199254740993"}` and the page sets
`preserveBigIntegers: true`. The official client revives them as native
`BigInt`. Safe values remain plain JSON numbers. See
[Responses & BigInt](/guide/responses).

## Conformance

The protocol is pinned by two independent suites:

| Suite | Meaning | Result |
|---|---|---|
| C100 contract | Normative TCK cases green on all transports | 69/69 |
| I100 interop | Cells observed with real official clients | 92/92 |

The generated matrix lives in the repository under `specs/` and is published in
[Not Supported](/reference/not-supported) with the intentional gaps. A contract
pass never counts as interop evidence; both are tracked separately.

## Related pages

- [How It Works](/guide/how-it-works) — request lifecycle and diagram.
- [Partial Reloads](/guide/partial-reloads) — `only`, `except`, reset.
- [Props](/guide/props) — deferred, once, merge and scroll.
- [Live Props](/guide/live-props) — channels, events and throttling.

Next: [Not Supported](/reference/not-supported) · [Migration](/reference/migration)
