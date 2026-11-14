# Responses & BigInt

Every Inertia response is either a **full HTML document** (initial visit) or a
**JSON page object** (XHR visit). The page object is the contract between the
Java adapter and the official client.

## The page object

```json
{
  "component": "Contacts/Index",
  "props": { "contacts": [] },
  "url": "/contacts",
  "version": "8f14e45fceea167a5a36dedd4bea2543",
  "clearHistory": false,
  "encryptHistory": false,
  "preserveFragment": false,
  "deferredProps": { "stats": ["stats"] },
  "mergeProps": ["contacts"],
  "prependProps": [],
  "deepMergeProps": [],
  "matchPropsOn": ["contacts.data.id"],
  "scrollProps": {},
  "liveProps": {},
  "sharedProps": {},
  "rescuedProps": []
}
```

| Field | Meaning |
|---|---|
| `component` | Component name resolved by the client |
| `props` | Evaluated props for this visit |
| `url` | Canonical URL (path + query) |
| `version` | Asset version used for mismatch detection |
| `clearHistory` | Client must clear history state |
| `encryptHistory` | Client must encrypt history state |
| `preserveFragment` | Keep the URL fragment across the visit |
| `deferredProps` | Deferred group → keys metadata |
| `mergeProps` / `prependProps` / `deepMergeProps` | Merge intent per key |
| `matchPropsOn` | Dedup keys for merged collections |
| `scrollProps` | Infinite-scroll metadata |
| `liveProps` | Live channel/event/throttle metadata (0.0.6) |
| `sharedProps` | Shared props snapshot (omitted when empty) |
| `rescuedProps` | Deferred/cached props that failed and were rescued |

Empty optional maps and arrays are omitted to keep the payload small.

## Response headers

| Header | Value |
|---|---|
| `Content-Type` | `text/html` (initial) or `application/json` (XHR) |
| `X-Inertia` | `true` on JSON page responses |
| `Vary` | `X-Inertia`, `X-Inertia-Version`, partial headers |
| `ETag` | Present when `inertia.lazy-etag-enabled=true` |
| `X-Inertia-Location` | Present on `409` control responses |

`GET` + `If-None-Match` returns `304` when the page ETag still matches. A
version mismatch returns `409` and forces a full reload.

## Rendering

::: code-group

```java [Spring Boot]
@GetMapping("/orders/{id}")
public Object show(@PathVariable long id) {
    var order = orders.findById(id);
    return inertia.render("Orders/Show", Map.of("order", order));
}
```

```java [Quarkus]
@GET
@Path("/orders/{id}")
public Uni<Object> show(@PathParam("id") long id) {
    var order = orders.findById(id);
    return inertia.render("Orders/Show", Map.of("order", order));
}
```

:::

## BigInt and 64-bit IDs

JavaScript numbers are IEEE-754 doubles: integers above
`Number.MAX_SAFE_INTEGER` (`2^53 - 1`, i.e. `9007199254740991`) lose precision
when parsed with `JSON.parse`. A Java `long` identifier such as
`9007199254740993` would silently become `9007199254740992` in the browser.

BigInt support is **opt-in**, matching Laravel's `preserveBigIntegers()` and
Rails' `preserve_big_integers`.

### Global configuration

```properties
inertia.bigint-enabled=true
```

With the flag on, every unsafe `long` or `BigInteger` in the page props is
serialized as a marker:

```json
{
  "id": { "$bigint": "9007199254740993" },
  "preserveBigIntegers": true
}
```

The official client revives the marker as a native `BigInt`. Safe values always
stay plain JSON numbers, so existing payloads do not change unnecessarily.

### Per-response control

::: code-group

```java [Spring Boot — facade]
// Before rendering
inertia.preserveBigIntegers(true);
return inertia.render("Orders/Show", Map.of("id", order.id()));

// Fluent, after rendering (re-serializes the response)
return ((InertiaRender) inertia.render("Orders/Show", Map.of("id", order.id())))
    .preserveBigIntegers();

// Opt out for one response while the global flag is on
inertia.preserveBigIntegers(false);
```

```java [Quarkus — facade]
inertia.preserveBigIntegers(true);
return inertia.render("Orders/Show", Map.of("id", order.id()));
```

:::

Typed results opt in through the factory:

```java
return InertiaPageResult.of("Orders/Show", Map.of("id", id), true);
```

Precedence is **request override > page flag > global flag**. `true` forces the
marker even when `inertia.bigint-enabled=false`; `false` keeps plain numbers
even when the global flag is on.

### What counts as unsafe

| Value | Serialization |
|---|---|
| `42` | plain number |
| `9007199254740991` (`2^53 - 1`) | plain number |
| `9007199254740992` and above | `{"$bigint":"..."}` |
| negative values below `-(2^53 - 1)` | `{"$bigint":"..."}` |
| `BigInteger` beyond the safe range | `{"$bigint":"..."}` |

> Use BigInt only when the client actually needs the exact value. IDs used for
> display or opaque references can also be serialized as strings by the DTO.

## Payload limits

```properties
inertia.max-page-bytes=33554432   # 32 MiB, default
# inertia.max-page-bytes=-1       # disable the limit
```

A page larger than the limit fails closed with `413 Payload Too Large` instead
of serving an unbounded response. This protects the server and the browser from
accidental entity graphs or runaway collections.

## Error responses

| Property | Default | Meaning |
|---|---|---|
| `inertia.error-status` | `500` | Status used for error pages |
| `inertia.error-component` | `ErrorPage` | Component rendered on errors |
| `inertia.error-details-enabled` | `false` | Include exception details (dev only) |

On an Inertia visit the error is rendered as a page object with the configured
component and a `status` prop; on a full visit it is rendered into the root
template. Details are never leaked by default.

## ETag and caching

`inertia.lazy-etag-enabled=true` (default) computes an ETag from the serialized
page and honors `If-None-Match`. This is safe because the page object already
contains the version and all evaluated props. Disable it only if your setup
requires unconditional responses.

Next: [BigInt configuration](/reference/configuration) ·
[Live Props](/guide/live-props) · [Partial Reloads](/guide/partial-reloads)
