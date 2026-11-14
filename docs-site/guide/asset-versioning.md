# Asset Versioning

Every page object carries a `version`. The client sends it back as
`X-Inertia-Version`; when the server's version differs, the response is
`409 Conflict` with `X-Inertia-Location`, and the client performs a full page
load to pick up fresh JavaScript and CSS.

This is what prevents a long-lived tab from running old components against a
new backend contract.

## Strategies

| Strategy | Source | Notes |
|---|---|---|
| `version-custom` | Fixed string | Wins whenever set |
| `vite-manifest` | `.vite/manifest.json` | Hash of the build manifest |
| `sha256` | Static assets on the classpath | Content hash, default |
| fallback | Application version | Used when nothing can be hashed |

Precedence is exactly: `version-custom` > `vite-manifest` > `sha256` >
fallback.

```properties
# Default: content hash of static assets
inertia.version-strategy=sha256

# Vite manifest hash (recommended for Vite builds)
inertia.version-strategy=vite-manifest

# Fixed version (release tag, build number, ...)
inertia.version-custom=2026.11.13-1
```

::: warning Hyphenated keys
Use `inertia.version-strategy`, not `inertia.version.strategy`. The dotted form
does not bind and is silently ignored.
:::

## Vite manifest

With `inertia.version-strategy=vite-manifest`, the adapter reads
`.vite/manifest.json` from the classpath and hashes it. The manifest must be
emitted by the build:

```ts
// vite.config.ts
export default defineConfig({
  build: {
    manifest: '.vite/manifest.json',
    outDir: '../main/resources/static',
    emptyOutDir: false,
  },
})
```

If the manifest is missing, startup or the first page render fails with a clear
message; the adapter does not silently fall back to an empty version.

## Per-request override

```java
// Spring
inertia.setVersion(release.version());
return inertia.render("Dashboard", Map.of());

// Quarkus
inertia.version(release.version());
return inertia.render("Dashboard", Map.of());
```

This is useful for canary deployments, tenant-specific bundles or a maintenance
window. The value is serialized as-is; keep it short and stable for a given
build.

## What the client does

```text
GET /contacts
X-Inertia: true
X-Inertia-Version: 8f14e45f...

    version matches     → 200 + page object
    version differs     → 409 + X-Inertia-Location: /contacts
```

On `409` the client performs `window.location` navigation to the location. The
new document loads the current assets and the current server code. Flash data
queued by the redirect survives the full reload, so the user still sees the
success message.

A `409` control response:

- does **not** carry `X-Inertia: true`,
- does **not** carry a page body,
- always carries `X-Inertia-Location`,
- is never cached.

## Why not always reload?

A full reload discards client state and is slower. The version check is
conditional: unchanged deployments keep the SPA flow, and only stale tabs pay
the reload cost. That is why the version should change when assets change and
stay stable otherwise.

## Deployment guidance

- Compute the version at build time and bake it into the artifact.
- Keep the manifest and the static assets in the same artifact.
- Do not use the server start time: every restart would force every tab to
  reload.
- In a rolling deployment, old and new instances can serve different versions.
  A request that lands on a new instance returns `409` and the client reloads
  once; this is expected and self-healing.
- Behind a CDN, make sure `index.html` is not cached longer than the assets it
  references. The version check cannot help if the HTML itself is stale.

## Flash and version mismatch

Flash data is stored in the session before the redirect. When the subsequent
visit detects a version mismatch, the adapter still returns `409`; the full
reload lands on the target page and drains the flash exactly once. There is no
duplicate message and no lost message.

## Custom providers

Both adapters expose a `VersionProvider` SPI. Register a bean when the version
must come from an external system:

```java
// Spring Boot
@Component
public class DeploymentVersionProvider implements VersionProvider {
    @Override
    public String version() {
        return deployment.current().version();
    }
}
```

```java
// Quarkus
@ApplicationScoped
public class DeploymentVersionProvider implements VersionProvider {
    @Override
    public String getVersion() {
        return deployment.current().version();
    }
}
```

The default providers remain available; a custom bean simply takes precedence
in the adapter wiring.

## Testing versioning

```java
// Current version → normal page
mvc.perform(get("/contacts")
        .header("X-Inertia", "true")
        .header("X-Inertia-Version", currentVersion()))
    .andExpect(status().isOk());

// Stale version → 409 + location
mvc.perform(get("/contacts")
        .header("X-Inertia", "true")
        .header("X-Inertia-Version", "stale"))
    .andExpect(status().isConflict())
    .andExpect(header().string("X-Inertia-Location", "/contacts"));
```

The TCK suite `05-version.yaml` covers both branches on Spring MVC, Quarkus
REST and Quarkus Reactive, including the reactive path prefix.

## Checklist

- [ ] `version-strategy` matches the build pipeline (`vite-manifest` for Vite).
- [ ] The manifest ships inside the application artifact.
- [ ] The version changes when assets change and not on every restart.
- [ ] `409` responses are never cached.
- [ ] Flash messages are asserted after a version-triggered reload.
- [ ] A stale-tab test exists in the browser suite.

Next: [Responses & BigInt](/guide/responses) · [History](/guide/history) ·
[Configuration](/reference/configuration)
