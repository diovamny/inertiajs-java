# Server-Side Rendering

SSR renders the first page on a Node.js sidecar before it reaches the browser.
The Java application still owns routing, data and sessions; Node only turns the
page object into HTML with the same Vue, React or Svelte components used in the
browser. The result hydrates into a normal Inertia SPA.

SSR is optional. When the sidecar is unavailable the adapter degrades to
client-side rendering for that request instead of failing the visit.

## How it works

```text
Browser ──GET /contacts──▶ Java adapter
                             │  page object (props, url, version)
                             ▼
                        POST /render ──▶ Node sidecar (createServer)
                             │                 │ render component
                             │◀── { head, body }
                             ▼
                     inject into root template
                             │
Browser ◀── HTML + page JSON ┘
```

1. The adapter builds the page object exactly as in a client-side visit.
2. On a full page load with SSR enabled, it POSTs the page JSON to the sidecar.
3. The sidecar runs `createInertiaApp` with a server renderer and returns
   `{ head, body }`.
4. The adapter injects both into the root template (`__INERTIA_SSR_HEAD__` and
   `__INERTIA_ROOT__` / `__INERTIA_SSR_BODY__`).
5. The browser hydrates the existing markup with the embedded page JSON.

Inertia visits (`X-Inertia: true`) never use SSR: they already run in the
browser and receive JSON only.

## Build the sidecar

The starter kits already contain `src/ssr.ts` and the scripts. The pattern is
the same for every framework.

::: code-group

```ts [Vue 3]
// src/ssr.ts
import createServer from '@inertiajs/vue3/server'
import { createInertiaApp } from '@inertiajs/vue3'
import { createSSRApp, h } from 'vue'
import { renderToString } from 'vue/server-renderer'
import type { DefineComponent } from 'vue'

createServer(
  (page) =>
    createInertiaApp({
      page,
      render: renderToString,
      resolve: (name) => {
        const pages = import.meta.glob<{ default: DefineComponent }>(
          './pages/**/*.vue',
          { eager: true },
        )
        const resolved = pages[`./pages/${name}.vue`]
        if (!resolved) throw new Error(`Page not found: ${name}`)
        return resolved
      },
      setup({ App, props, plugin }) {
        return createSSRApp({ render: () => h(App, props) }).use(plugin)
      },
    }),
  { host: '127.0.0.1', port: 13714 },
)
```

```tsx [React 19]
// src/ssr.tsx
import createServer from '@inertiajs/react/server'
import { createInertiaApp } from '@inertiajs/react'
import ReactDOMServer from 'react-dom/server'
import type { ResolvedComponent } from '@inertiajs/react'

createServer(
  (page) =>
    createInertiaApp({
      page,
      render: ReactDOMServer.renderToString,
      resolve: (name) => {
        const pages = import.meta.glob<{ default: ResolvedComponent }>(
          './pages/**/*.tsx',
          { eager: true },
        )
        const resolved = pages[`./pages/${name}.tsx`]
        if (!resolved) throw new Error(`Page not found: ${name}`)
        return resolved
      },
      setup: ({ App, props }) => <App {...props} />,
    }),
  { host: '127.0.0.1', port: 13714 },
)
```

```ts [Svelte 5]
// src/ssr.ts
import createServer from '@inertiajs/svelte/server'
import { createInertiaApp } from '@inertiajs/svelte'
import { render } from 'svelte/server'
import type { ResolvedComponent } from '@inertiajs/svelte'

createServer(
  (page) =>
    createInertiaApp({
      page,
      render,
      resolve: (name) => {
        const pages = import.meta.glob<ResolvedComponent>(
          './pages/**/*.svelte',
          { eager: true },
        )
        const resolved = pages[`./pages/${name}.svelte`]
        if (!resolved) throw new Error(`Page not found: ${name}`)
        return resolved
      },
    }),
  { host: '127.0.0.1', port: 13714 },
)
```

:::

`createServer` comes from the framework package, not from `@inertiajs/core`.
It starts an HTTP server with `/render` and `/health` routes and listens on
port `13714` by default.

::: warning Bind to localhost
The upstream default host is `0.0.0.0`. Always pass
`{ host: '127.0.0.1', port: 13714 }` unless you intentionally run the sidecar on
a private network with the adapter's remote-SSR allowlist configured. The
sidecar receives every server prop and its HTML is injected without
sanitization.
:::

Build and run scripts:

```json
{
  "scripts": {
    "build": "vite build",
    "build:ssr": "vite build --ssr src/ssr.ts --outDir dist-ssr",
    "ssr": "node dist-ssr/ssr.js"
  }
}
```

```bash
npm install
npm run build      # client bundle
npm run build:ssr  # dist-ssr/ssr.js
npm run ssr        # sidecar on 127.0.0.1:13714
```

`build:ssr` compiles the SSR entry only. The client build and the SSR build must
come from the same source revision, or hydration will mismatch.

## Configure the Java side

::: code-group

```properties [Spring Boot]
inertia.ssr-enabled=true
# Spring posts to this URL verbatim: include /render
inertia.ssr-url=http://localhost:13714/render
inertia.ssr-connect-timeout=5s
inertia.ssr-read-timeout=10s
```

```properties [Quarkus]
inertia.ssr-enabled=true
# Quarkus appends /render when missing
inertia.ssr-url=http://localhost:13714
inertia.ssr-connect-timeout=5s
inertia.ssr-read-timeout=10s
```

:::

`inertia.ssr-exclude-paths` skips SSR for specific paths (comma-separated),
which is useful for health endpoints, webhooks or pages that must always be
client-rendered:

```properties
inertia.ssr-exclude-paths=/health,/webhooks/*
```

## Root template

The recommended template is mode-agnostic:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>__INERTIA_PAGE_TITLE__</title>
    __INERTIA_SSR_HEAD__
    <link rel="stylesheet" href="/assets/app.css" />
</head>
<body>
    __INERTIA_ROOT__
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

| Placeholder | CSR | SSR |
|---|---|---|
| `__INERTIA_ROOT__` | Mount point + page JSON | Server body + page JSON |
| `__INERTIA_SSR_BODY__` | Empty | Rendered HTML |
| `__INERTIA_SSR_HEAD__` | Empty | Rendered `<head>` tags |
| `__INERTIA_PAGE_JSON__` | Page object | Page object |

If you spell out the mount point yourself, keep the v3-pure contract:

```html
<div id="app">__INERTIA_SSR_BODY__</div>
<script type="application/json" data-page="app">__INERTIA_PAGE_JSON__</script>
```

Do not put `data-page` on the mount element. See [Root Template](/guide/root-template)
for the full placeholder table and the Qute variant.

## What the client does

- With SSR HTML present, the official client hydrates instead of mounting from
  scratch. Component state, refs and event listeners attach to the existing DOM.
- If hydration fails (usually a mismatch between the server and client markup),
  the client logs the mismatch and recovers by rendering on the client.
- Inertia visits after the first load never touch SSR; they use the JSON
  protocol as usual.

Common hydration mismatches:

| Cause | Fix |
|---|---|
| `Date.now()` or random values rendered directly | Compute on the server and pass as a prop, or render after mount |
| `window`/`localStorage` read during render | Guard with `onMounted` / `useEffect` |
| Different component build on each side | Build client and SSR from one source revision |
| Invalid HTML nesting (e.g. `<div>` inside `<p>`) | Fix the markup; browsers reparse it and hydration diverges |

## Resilience: fallback, breaker, cache

SSR failures are non-fatal by design. The adapter classifies a failure as
`unreachable`, `timeout`, `error-status` or `unknown`, logs it with scheme, host
and port only, and serves the page with client-side rendering.

### Circuit breaker

After a configurable number of consecutive failures the breaker opens and the
adapter skips the sidecar entirely (fail-fast CSR) until a cooldown elapses and
a half-open probe succeeds.

```properties
inertia.ssr-breaker-failure-threshold=5
inertia.ssr-breaker-cooldown=30s
```

| State | Behavior |
|---|---|
| Closed | Every full page load tries SSR |
| Open | Every render falls back to CSR immediately |
| Half-open | One probe is allowed; success closes, failure re-opens |

### Response cache

The cache keys rendered output by the page hash, so an unchanged page does not
pay the Node render cost twice.

```properties
inertia.ssr-cache-enabled=true
inertia.ssr-cache-ttl=15m
```

Per-render override:

```java
return ((InertiaRender) inertia.render("Marketing/Home", Map.of()))
    .enableSsrCache(Duration.ofMinutes(5));
```

Do not cache pages that embed per-user secrets or rapidly changing data unless
the page hash includes everything that varies.

### Process supervisor

The adapter can start, restart and stop the sidecar itself. This is convenient
for single-node deployments and unnecessary when systemd, Docker or Kubernetes
already owns the process.

```properties
inertia.ssr-supervisor-enabled=true
inertia.ssr-supervisor-command=node
inertia.ssr-supervisor-entry=dist-ssr/ssr.js
inertia.ssr-supervisor-workdir=/opt/app
inertia.ssr-supervisor-max-restarts=5
```

The supervisor restarts with backoff and stops the child on application
shutdown. Do not enable it together with an external orchestrator that also
restarts the sidecar; two supervisors will fight.

### Health

The Node server exposes `GET /health` returning
`{ "status": "OK", "timestamp": ... }`.

Quarkus exposes `SsrHealthBean`, which aggregates the breaker and supervisor
state. Wire it into SmallRye Health without duplicating logic:

```java
@Readiness
@ApplicationScoped
public class InertiaSsrHealthCheck implements HealthCheck {

    @Inject SsrHealthBean ssr;

    @Override
    public HealthCheckResponse call() {
        var snapshot = ssr.snapshot();
        return HealthCheckResponse.named("inertia-ssr")
            .status(snapshot.status() != SsrHealth.Status.DOWN)
            .withData(snapshot.details())
            .build();
    }
}
```

For Spring Boot, a small `HealthIndicator` that calls `/health` with a short
timeout is enough. Alert on the breaker being open, not on a single failure.

## Trust boundary

The sidecar is part of your server, not a public service:

- It receives the complete page object, including props the browser may never
  see in a JSON visit (deferred keys are excluded, but everything resolved is
  sent).
- Its HTML is injected into the root template **without sanitization**. A
  compromised sidecar can inject scripts.
- URLs with credentials (`user:pass@`) or fragments are rejected at startup.
- Redirects from the sidecar are never followed; a `3xx` is a CSR fallback.

Remote sidecars require an explicit opt-in, HTTPS and an allowlist:

```properties
inertia.ssr-enabled=true
inertia.ssr-url=https://ssr.internal.example.com/render
inertia.ssr-remote-enabled=true
inertia.ssr-allowed-hosts=ssr.internal.example.com
```

Anything else fails fast at startup. Startup errors report scheme, host and port
only — never tokens or payloads.

Timeout defaults are intentionally short (`5s` connect, `10s` read): a sidecar
that cannot render quickly should not delay the page. Tune them only with
measured evidence.

## Development workflow

Two terminals are enough:

```bash
# terminal 1 — Java
mvn spring-boot:run          # or: mvn quarkus:dev

# terminal 2 — frontend
npm run dev                  # Vite with HMR
npm run ssr                  # rebuild ssr after changing server-rendered code
```

During development, disable the response cache and leave the breaker at its
defaults:

```properties
inertia.ssr-cache-enabled=false
inertia.template-cache-enabled=false
```

If a page renders correctly without SSR but not with it, compare the server and
client props: a component that branches on `typeof window` will produce
different markup on the first render.

## Production checklist

- [ ] Client and SSR bundles built from the same commit.
- [ ] Sidecar bound to `127.0.0.1` (or remote allowlist + HTTPS configured).
- [ ] Process supervised by exactly one owner (adapter or orchestrator).
- [ ] `/health` scraped and the breaker state alerted.
- [ ] Timeouts and breaker thresholds tuned from real latency data.
- [ ] Response cache disabled for personalized pages.
- [ ] Root template contains `__INERTIA_ROOT__` (or the explicit placeholders).
- [ ] Hydration warnings fail the browser test suite.
- [ ] A sidecar outage test proves the page still loads with CSR.

Next: [Performance](/guide/performance) · [Testing](/guide/testing) ·
[Configuration](/reference/configuration)
