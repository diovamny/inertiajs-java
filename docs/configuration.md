# Configuration Reference

All settings live under the `inertia` prefix in `application.properties`
(Spring Boot and Quarkus alike). The last column shows which adapter supports
each property.

| Property | Default | Spring | Quarkus | Description |
|---|---|---|---|---|
| `inertia.root-template` | `index.html` | ✅ | ✅ | Root HTML template resolved from the classpath (`templates/`). |
| `inertia.template-cache-enabled` | `true` | ✅ | ✅ | Cache the root template in memory (disable in dev for live reload). |
| `inertia.ssr-enabled` | `false` | ✅ | ✅ | Render pages through the Node.js SSR sidecar (see [ssr](ssr-setup.md)). |
| `inertia.ssr-url` | Spring: `http://localhost:13714/render` · Quarkus: `http://localhost:13714` | ✅ | ✅ | SSR endpoint (validated at startup by `SsrEndpointPolicy`; see [ssr](ssr-setup.md#trust-boundary)). |
| `inertia.ssr-remote-enabled` | `false` | ✅ | ✅ | Allow a remote (non-localhost) sidecar. Remote additionally requires `https` + `ssr-allowed-hosts`. |
| `inertia.ssr-allowed-hosts` | _(empty)_ | ✅ | ✅ | Comma-separated allowlist of remote SSR hosts (host only, case-insensitive). |
| `inertia.ssr-exclude-paths` | _(empty)_ | ✅ | ✅ | Comma-separated paths excluded from SSR (fall back to client rendering). |
| `inertia.ssr-connect-timeout` | `5s` | ✅ | ✅ | SSR HTTP connect timeout. |
| `inertia.ssr-read-timeout` | `10s` | ✅ | ✅ | SSR HTTP read timeout. |
| `inertia.version-strategy` | `sha256` | ✅ | ✅ | Asset version: `sha256`, `vite-manifest`, or fixed via `version-custom`. Precedence: `version-custom` > `vite-manifest` > `sha256` > fallback. |
| `inertia.version-custom` | _(unset)_ | ✅ | ✅ | Fixed version string; wins over the strategy when set. |
| `inertia.encrypt-history` | `false` | ✅ | ✅ | The client must encrypt history state. |
| `inertia.clear-history` | `false` | ✅ | ➖ | Clear the client history on the next visit (Spring only). |
| `inertia.camelize-props` | `false` | ✅ | ✅ | Convert snake_case prop keys to camelCase (`first_name` → `firstName`). |
| `inertia.bigint-enabled` | `false` | ✅ | ✅ | Serialize unsafe 64-bit integers (and `BigInteger` values beyond 2^53 − 1) as `{"$bigint":"..."}` markers plus `preserveBigIntegers: true` on the page. Can be overridden per response with `inertia.preserveBigIntegers(true/false)`. |
| `inertia.csrf-enabled` | `true` | ✅ | ✅ | XSRF-TOKEN cookie synchronization. Invalid Inertia visits fail with `303` + `Location` + flash (same default in Spring and Quarkus); non-Inertia requests pass through in the Spring adapter and keep legacy `419` in the Quarkus reactive pre-handler. |
| `inertia.root-view` | _(unset)_ | ✅ | ✅ | Explicit view name for the root view; defaults to `root-template`. |
| `inertia.flash-keys` | _(empty)_ | ✅ | ✅ | Comma-separated session keys flashed with every page. |
| `inertia.session.enabled` | `true` | ➖ | ✅ | Quarkus only: auto-register a Vert.x `SessionHandler` for flash and session-based CSRF. Set `false` (WARN at startup) when the application registers its own `SessionHandler`. Spring requires nothing: the HTTP session is available out of the box. |
| `inertia.always-include-errors` | `true` | ✅ | ✅ | Always inject validation `errors` into props (Laravel-compatible). |
| `inertia.validation.all-errors` | `false` | ✅ | ✅ | Multi-message errors: `false` = legacy `Map<field,message>`; `true` = `Map<field,List<message>>` ordered arrays (Inertia `withAllErrors` parity) in flash, bags and Precognition 422. Explicit `withValidationErrors`/`withErrorMessages` always emit arrays. |
| `inertia.error-status` | `500` | ✅ | ✅ | HTTP status used for error pages. |
| `inertia.error-component` | `ErrorPage` | ✅ | ✅ | Component rendered for error pages. |
| `inertia.error-details-enabled` | `false` | ✅ | ✅ | Include exception details in error responses (development only). |
| `inertia.max-page-bytes` | `33554432` (32 MiB) | ✅ | ✅ | Max serialized page size; larger pages fail closed with `413` (`-1` disables). |
| `inertia.lazy-etag-enabled` | `true` | ✅ | ✅ | ETag / `304` handling. |
| `inertia.use-qute` | `false` | ➖ | ✅ | Quarkus only: render the root template with Qute (needs `quarkus-qute`). |
| `inertia.convention-routing-enabled` | `false` | ✅ | ✅ | Auto-resolve component names by convention. |
| `inertia.convention-routing-prefix` | _(empty)_ | ✅ | ✅ | Prefix prepended to auto-resolved component names. |

Example:

```properties
inertia.root-template=index.html
inertia.version-custom=1.0.0
inertia.ssr-enabled=false
inertia.csrf-enabled=true
inertia.convention-routing-enabled=true
inertia.convention-routing-prefix=Pages/
```

## Per-visit API

Most flags can also be set per request from a controller:

```java
// Spring
inertia.setVersion("abc123");
inertia.setEncryptHistory(true);
inertia.setClearHistory(true);
inertia.flash("success", "Saved.");

// Quarkus (same names on the reactive facade)
inertia.setVersion("abc123");
inertia.setEncryptHistory(true);
inertia.flash("success", "Saved.");
```

## BigInt & large numbers

JSON numbers beyond `Number.MAX_SAFE_INTEGER` (2^53 − 1) lose precision in
JavaScript. With `inertia.bigint-enabled=true` every unsafe `long`/`BigInteger`
travels as a `{"$bigint":"..."}` marker and the page carries
`preserveBigIntegers: true`, so the official clients revive it as a native
`BigInt`. Safe values always stay plain numbers.

You can enable it per response instead of globally (Laravel
`preserveBigIntegers()` / Rails `preserve_big_integers` parity); precedence is
request override > page flag > global flag:

```java
// Spring — before rendering
inertia.preserveBigIntegers(true);

// Spring — fluent, after rendering (re-serializes the response)
return ((InertiaRender) inertia.render("Orders/Show", Map.of("id", order.id())))
    .preserveBigIntegers();

// Opt out for a single response while the global flag is on
inertia.preserveBigIntegers(false);

// Quarkus (reactive facade)
inertia.preserveBigIntegers(true);
return inertia.render("Orders/Show", Map.of("id", order.id()));
```

Typed results opt in through the factory:

```java
return InertiaPageResult.of("Orders/Show", Map.of("id", id), true);
```

## Props model

Shared, optional, deferred, once, merge and scroll props are covered in
[shared data and props](shared-data-and-props.md); root-template data in
[view data](viewdata-guide.md).
