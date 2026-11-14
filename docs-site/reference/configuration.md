# Configuration

All settings live under the `inertia` prefix in `application.properties` (or
`application.yml`) for both Spring Boot and Quarkus. Defaults are identical
unless the table says otherwise.

The first table lists the 29 core properties. Framework-specific and advanced
properties are grouped in the sections that follow.

## Core properties

| Property | Default | Spring | Quarkus | Description |
|---|---|---|---|---|
| `inertia.root-template` | `index.html` | ✅ | ✅ | Root HTML template resolved from `templates/` on the classpath. |
| `inertia.template-cache-enabled` | `true` | ✅ | ✅ | Cache the root template in memory; disable in development for live reload. |
| `inertia.ssr-enabled` | `false` | ✅ | ✅ | Render pages through the Node.js SSR sidecar. |
| `inertia.ssr-url` | Spring `http://localhost:13714/render`; Quarkus `http://localhost:13714` | ✅ | ✅ | SSR endpoint. Quarkus appends `/render` when missing. |
| `inertia.ssr-remote-enabled` | `false` | ✅ | ✅ | Allow a non-localhost sidecar. Remote also requires HTTPS and an allowlist. |
| `inertia.ssr-allowed-hosts` | _(empty)_ | ✅ | ✅ | Comma-separated allowlist of remote SSR hosts (host only, case-insensitive). |
| `inertia.ssr-exclude-paths` | _(empty)_ | ✅ | ✅ | Comma-separated paths that always use client-side rendering. |
| `inertia.ssr-connect-timeout` | `5s` | ✅ | ✅ | SSR HTTP connect timeout. |
| `inertia.ssr-read-timeout` | `10s` | ✅ | ✅ | SSR HTTP read timeout. |
| `inertia.version-strategy` | `sha256` | ✅ | ✅ | Asset version strategy: `sha256`, `vite-manifest` or `custom`. |
| `inertia.version-custom` | _(unset)_ | ✅ | ✅ | Fixed version string; wins over the strategy. |
| `inertia.encrypt-history` | `false` | ✅ | ✅ | Ask the client to encrypt history state. |
| `inertia.clear-history` | `false` | ✅ | ➖ | Clear client history on the next visit (Spring only). |
| `inertia.camelize-props` | `false` | ✅ | ✅ | Convert snake_case prop keys to camelCase (`first_name` → `firstName`). |
| `inertia.bigint-enabled` | `false` | ✅ | ✅ | Serialize unsafe 64-bit integers as `{"$bigint":"..."}` markers. |
| `inertia.csrf-enabled` | `true` | ✅ | ✅ | Legacy CSRF flag; maps to `security.mode` when that is unset. |
| `inertia.root-view` | _(unset)_ | ✅ | ✅ | Explicit root view name; defaults to `root-template`. |
| `inertia.flash-keys` | _(empty)_ | ✅ | ✅ | Comma-separated session keys flashed on every page. |
| `inertia.session.enabled` | `true` | ➖ | ✅ | Quarkus only: auto-register the Vert.x `SessionHandler`. |
| `inertia.always-include-errors` | `true` | ✅ | ✅ | Always inject the validation `errors` prop. |
| `inertia.validation.all-errors` | `false` | ✅ | ✅ | `true` emits `Map<field, List<message>>` instead of one message per field. |
| `inertia.error-status` | `500` | ✅ | ✅ | HTTP status used for error pages. |
| `inertia.error-component` | `ErrorPage` | ✅ | ✅ | Component rendered for error pages. |
| `inertia.error-details-enabled` | `false` | ✅ | ✅ | Include exception details in responses; development only. |
| `inertia.max-page-bytes` | `33554432` (32 MiB) | ✅ | ✅ | Maximum serialized page size; larger pages answer `413` (`-1` disables). |
| `inertia.lazy-etag-enabled` | `true` | ✅ | ✅ | ETag / `304 Not Modified` handling. |
| `inertia.use-qute` | `false` | ➖ | ✅ | Quarkus only: render the root template with Qute. |
| `inertia.convention-routing-enabled` | `false` | ✅ | ✅ | Resolve component names by convention. |
| `inertia.convention-routing-prefix` | _(empty)_ | ✅ | ✅ | Prefix prepended to auto-resolved component names. |

Both frameworks support relaxed binding from environment variables: replace
dots and hyphens with underscores and upper-case the name, for example
`inertia.root-template` → `INERTIA_ROOT_TEMPLATE` and
`inertia.security.mode` → `INERTIA_SECURITY_MODE`.

## Version precedence

1. `inertia.version-custom`, when set.
2. `inertia.version-strategy=vite-manifest`, when
   `.vite/manifest.json` is on the classpath.
3. `inertia.version-strategy=sha256`, the default.
4. A startup fallback when none of the above can produce a version.

Use a fixed `version-custom` in tests that assert `409` behavior, so the value
does not change between builds.

## SSR resilience and supervisor

These properties extend the three SSR properties in the core table:

| Property | Default | Description |
|---|---|---|
| `inertia.ssr-breaker-failure-threshold` | `5` | Consecutive failures before the circuit opens. |
| `inertia.ssr-breaker-cooldown` | `30s` | Time before a half-open probe is allowed. |
| `inertia.ssr-cache-enabled` | `false` | Cache rendered output keyed by page hash. |
| `inertia.ssr-cache-ttl` | `15m` | Default cache TTL; overridable per render. |
| `inertia.ssr-supervisor-enabled` | `false` | Let the adapter start and supervise the sidecar. |
| `inertia.ssr-supervisor-command` | `node` | Executable used to launch the sidecar. |
| `inertia.ssr-supervisor-entry` | `dist-ssr/ssr.mjs` | Sidecar entry point. |
| `inertia.ssr-supervisor-workdir` | _(empty)_ | Working directory for the child process. |
| `inertia.ssr-supervisor-max-restarts` | `5` | Restart budget before the supervisor gives up. |

The supervisor default entry is `dist-ssr/ssr.mjs`, while the generated
starters emit `dist-ssr/ssr.js`. Set
`inertia.ssr-supervisor-entry=dist-ssr/ssr.js` when you enable the supervisor
with a generated starter.

Enable the supervisor only when no orchestrator (systemd, Docker, Kubernetes)
already owns the Node process. See [SSR](/guide/ssr).

## Security

Security settings use the `inertia.security` prefix:

| Property | Default | Description |
|---|---|---|
| `inertia.security.mode` | `auto` | `auto`, `framework`, `adapter` or `disabled`. |
| `inertia.security.fail-on-fallback` | `false` | Refuse to boot when `auto` resolves to the adapter. |
| `inertia.security.allow-disabled-in-production` | `false` | Permit `disabled` under the `prod` profile. |
| `inertia.security.login-url` | `/login` | Target of the `409` authentication challenge. |
| `inertia.security.forbidden-component` | `Errors/Forbidden` | Component used for the Inertia `403` page. |
| `inertia.security.csrf-failure-path` | `/` | Fallback when the referer is not same-origin. |
| `inertia.security.csrf-flash-key` | `error` | Flash key for the CSRF message. |
| `inertia.security.csrf-flash-message` | localized | Generic session-expired message. |
| `inertia.security.cookie-same-site` | `Lax` | `SameSite` attribute of the XSRF cookie. |
| `inertia.security.cookie-secure` | `false` | `Secure` attribute of the XSRF cookie. |
| `inertia.security.cookie-path` | `/` | XSRF cookie path. |
| `inertia.security.cookie-domain` | _(empty)_ | XSRF cookie domain. |
| `inertia.security.reactive-csrf-paths` | _(empty)_ | Quarkus Reactive Routes prefixes that need CSRF. |
| `inertia.security.auth-props-enabled` | `false` | Contribute an allowlisted `auth.user` summary. |

One CSRF owner only: when `security.mode=framework`, do not also enable the
adapter filter. See [Security](/guide/security).

## Head and metadata

| Property | Default | Description |
|---|---|---|
| `inertia.server-head` | `false` | Publish collected `<head>` tags as the `head` page prop. |
| `inertia.meta-title-template` | `%s` | Format applied to the page title (`%s` is replaced). |

When `server-head=true`, the collected tags travel in the `head` page prop and
the client `<Head>` component renders them. See
[Title & Meta](/guide/title-and-meta).

## Per-visit overrides

Most flags can also be set from a controller for the current request:

```java
// Spring
inertia.setVersion("abc123");
inertia.setEncryptHistory(true);
inertia.setClearHistory(true);
inertia.flash("success", "Saved.");

// Quarkus
inertia.version("abc123");
inertia.encryptHistory(true);
inertia.clearHistory(true);
inertia.flash("success", "Saved.");
```

Per-visit values win over global configuration but do not leak into the next
request.

## Environment profiles

Spring Boot and Quarkus both support profile-specific files:

```properties
# application.properties
inertia.ssr-enabled=false
inertia.template-cache-enabled=true

# application-dev.properties
inertia.template-cache-enabled=false

# application-prod.properties
inertia.security.mode=framework
inertia.security.fail-on-fallback=true
inertia.ssr-enabled=true
inertia.ssr-url=http://127.0.0.1:13714/render
```

Keep production security explicit. Relying on `auto` is convenient in
development and risky in a deployment where a dependency accidentally changes
the classpath.

## Related pages

- [Server Setup](/guide/server-setup) — wiring and auto-configuration.
- [Asset Versioning](/guide/asset-versioning) — strategies and `409` behavior.
- [SSR](/guide/ssr) — sidecar, breaker, cache and supervisor.
- [Security](/guide/security) — modes, CSRF and framework integration.

Next: [Protocol](/reference/protocol) · [Migration](/reference/migration) ·
[Changelog](/reference/changelog)
