# Configuration Reference

All settings live under the `inertia` prefix in `application.properties`
(Spring Boot and Quarkus alike).

| Property | Default | Description |
|---|---|---|
| `inertia.root-template` | `index.html` | Root HTML template resolved from the classpath (`templates/`). |
| `inertia.template-cache-enabled` | `true` | Cache the root template in memory (disable in dev for live reload). |
| `inertia.ssr-enabled` | `false` | Render pages through the Node.js SSR sidecar (see [ssr](ssr-setup.md)). |
| `inertia.ssr-url` | `http://localhost:13714/render` | SSR endpoint. |
| `inertia.ssr-connect-timeout` / `inertia.ssr-read-timeout` | `5s` / `10s` | SSR HTTP timeouts. |
| `inertia.version-strategy` | `sha256` | Asset version: `sha256`, `vite-manifest` or fixed via `version-custom`. |
| `inertia.version-custom` | _(unset)_ | Fixed version string; wins over the strategy when set. |
| `inertia.encrypt-history` / `inertia.clear-history` | `false` | History encryption / clearing for the visit. |
| `inertia.camelize-props` | `false` | Convert camelCase prop keys to snake_case. |
| `inertia.csrf-enabled` | `true` | XSRF-TOKEN cookie synchronization. |
| `inertia.always-include-errors` | `true` | Inject validation errors into props. |
| `inertia.error-status` / `inertia.error-component` | `500` / `ErrorPage` | Error page rendering. |
| `inertia.lazy-etag-enabled` | `true` | ETag / 304 handling. |
| `inertia.convention-routing-enabled` | `false` | Auto-resolve component names by convention. |
| `inertia.use-qute` | `false` | Quarkus only: render the root template with Qute. |

## Per-visit API

Most flags can also be set per request from a controller:

```java
// Spring
inertia.setVersion("abc123");
inertia.setEncryptHistory(true);
inertia.flash("success", "Saved.");

// Quarkus (same names on the reactive facade)
inertia.setVersion("abc123");
```

## Props model

Shared, optional, deferred, once, merge and scroll props are covered in
[shared data and props](shared-data-and-props.md); root-template data in
[view data](viewdata-guide.md).
