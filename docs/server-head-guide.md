# Server Head Guide: `HeadBuilder` API

Server-rendered `<head>` tags with Rails `MetaTagBuilder` parity, shared
by both adapters through `inertia-core` (`head` package).

## Collecting tags

```java
inertia.head()
    .title("Dashboard")                 // key: title
    .meta("description", "CRM")         // key: meta-description
    .property("og:title", "Dashboard")  // key: og:title
    .link("canonical", "https://app.example/dashboard") // key: link-canonical
    .canonical("https://app.example/dashboard");        // alias of the above
```

Every tag carries a `headKey`; adding a tag with an existing key replaces
the previous one, so titles, descriptions and canonical links never
duplicate. `remove(headKey)` drops one tag, `clear()` drops all.

## Title templates

`inertia.meta-title-template="%s | My App"` (default `"%s"`, title as-is)
is applied by `title(...)`. The same setting backs the per-request
builder in both adapters.

## Publishing to the frontend

Set `inertia.server-head=true` (default `false`). Collected tags are then
published as the `head` page prop — a list of `{key, tag, attributes}`
maps — for the frontend `Head` component. An explicit `head` prop set by
the application is never overwritten.

`HeadBuilder.toHtml()` renders the same tags as an HTML fragment for SSR
root templates and app-owned layouts; `toPropList()` is the prop payload.

## CSP nonces

The bootstrap needs no nonce by default (v3-pure: `<div id="app">` with no
payload plus a data-only `<script type="application/json" data-page="app">`).
When executable inline scripts require one, register a
`NonceProvider`: Qute templates receive `{cspNonce}`, placeholder
templates may use `__INERTIA_CSP_NONCE__`.
