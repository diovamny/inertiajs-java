# Configuration Reference

All settings live under the `inertia` prefix in `application.properties`
(Spring Boot and Quarkus alike). The last column shows which adapter supports
each property.

| Property | Default | Spring | Quarkus | Description |
|---|---|---|---|---|
| `inertia.root-template` | `index.html` | ✅ | ✅ | Root HTML template resolved from the classpath (`templates/`). |
| `inertia.template-cache-enabled` | `true` | ✅ | ✅ | Cache the root template in memory (disable in dev for live reload). |
| `inertia.ssr-enabled` | `false` | ✅ | ✅ | Render pages through the Node.js SSR sidecar (see [ssr](ssr-setup.md)). |
| `inertia.ssr-url` | Spring: `http://localhost:13714/render` · Quarkus: `http://localhost:13714` | ✅ | ✅ | SSR endpoint. |
| `inertia.ssr-exclude-paths` | _(empty)_ | ✅ | ✅ | Comma-separated paths excluded from SSR (fall back to client rendering). |
| `inertia.ssr-connect-timeout` | `5s` | ✅ | ✅ | SSR HTTP connect timeout. |
| `inertia.ssr-read-timeout` | `10s` | ✅ | ✅ | SSR HTTP read timeout. |
| `inertia.version-strategy` | `sha256` | ✅ | ✅ | Asset version: `sha256`, `vite-manifest`, or fixed via `version-custom`. Precedence: `version-custom` > `vite-manifest` > `sha256` > fallback. |
| `inertia.version-custom` | _(unset)_ | ✅ | ✅ | Fixed version string; wins over the strategy when set. |
| `inertia.encrypt-history` | `false` | ✅ | ✅ | The client must encrypt history state. |
| `inertia.clear-history` | `false` | ✅ | ➖ | Clear the client history on the next visit (Spring only). |
| `inertia.camelize-props` | `false` | ✅ | ✅ | Convert snake_case prop keys to camelCase (`first_name` → `firstName`). |
| `inertia.csrf-enabled` | `true` | ✅ | ✅ | XSRF-TOKEN cookie synchronization (+ `419` on invalid mutating visits). |
| `inertia.root-view` | _(unset)_ | ✅ | ✅ | Explicit view name for the root view; defaults to `root-template`. |
| `inertia.flash-keys` | _(empty)_ | ✅ | ✅ | Comma-separated session keys flashed with every page. |
| `inertia.always-include-errors` | `true` | ✅ | ✅ | Always inject validation `errors` into props (Laravel-compatible). |
| `inertia.error-status` | `500` | ✅ | ✅ | HTTP status used for error pages. |
| `inertia.error-component` | `ErrorPage` | ✅ | ✅ | Component rendered for error pages. |
| `inertia.error-details-enabled` | `false` | ✅ | ✅ | Include exception details in error responses (development only). |
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

## Props model

Shared, optional, deferred, once, merge and scroll props are covered in
[shared data and props](shared-data-and-props.md); root-template data in
[view data](viewdata-guide.md).
