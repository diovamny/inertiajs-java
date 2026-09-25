# SSR Setup Guide — Inertia.js Java Adapters

Server-Side Rendering (SSR) allows your Inertia application to render the initial page HTML on the server, improving first-load performance and SEO. Both the Spring Boot and Quarkus adapters support SSR via a Node.js sidecar process.

---

## Table of Contents

1. [Overview](#overview)
2. [Node.js SSR Server](#nodejs-ssr-server)
3. [Vite SSR Build](#vite-ssr-build)
4. [Spring Boot SSR Configuration](#spring-boot-ssr-configuration)
5. [Quarkus SSR Configuration](#quarkus-ssr-configuration)
6. [SSR Fallback Behavior](#ssr-fallback-behavior)
7. [Health Check](#health-check)

---

## Overview

SSR in the Java adapters works as follows:

1. Your Java application sends the Inertia page JSON to a **Node.js SSR sidecar** via HTTP.
2. The Node.js server renders the component to HTML using your frontend framework (React, Vue, Svelte).
3. The adapter receives the rendered HTML + head tags and injects them into the root HTML template.
4. If the SSR sidecar is unavailable, the adapter falls back to client-side rendering.

---

## Node.js SSR Server

Create a minimal SSR server entry point (`bootstrap/ssr/ssr.js`):

```js
import { createServer } from 'node:http';
import { createInertiaApp } from '@inertiajs/react'; // or vue, svelte
import ReactDOMServer from 'react-dom/server';

const server = createServer(async (req, res) => {
    const html = await createInertiaApp({
        page: JSON.parse(req.body),
        render: (node) => ReactDOMServer.renderToString(node),
        resolve: (name) => require(`./pages/${name}`),
    });
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(html));
});

server.listen(13714);
```

Start the sidecar:

```bash
node bootstrap/ssr/ssr.js
```

> The adapter expects the SSR server to respond with `{ "head": [...], "body": "<html>" }` JSON.

---

## Vite SSR Build

Add an SSR entry point (`resources/js/ssr.js`) next to your client entry.
Vue 3:

```js
// vite.config.js (Vue 3 + Spring Boot or Quarkus)
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
    plugins: [vue()],
    build: {
        manifest: true,
        outDir: '../java-static/assets',
        rollupOptions: { input: 'resources/js/app.js' },
    },
    ssr: { noExternal: ['@inertiajs/vue3'] },
});
```

```js
// resources/js/ssr.js (Vue 3)
import { createSSRApp, h } from 'vue';
import { renderToString } from 'vue/server-renderer';
import { createInertiaApp } from '@inertiajs/vue3';

export async function render(page) {
    const app = await createInertiaApp({
        page,
        render: renderToString,
        resolve: (name) => import(`./pages/${name}.vue`),
        setup: ({ App, props, plugin }) => createSSRApp({ render: () => h(App, props) }).use(plugin),
    });
    return app;
}
```

React 18:

```js
// vite.config.js (React 18)
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    build: {
        manifest: true,
        outDir: '../java-static/assets',
        rollupOptions: { input: 'resources/js/app.jsx' },
    },
});
```

Svelte 5:

```js
// vite.config.js (Svelte 5)
import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';

export default defineConfig({
    plugins: [svelte()],
    build: {
        manifest: true,
        outDir: '../java-static/assets',
        rollupOptions: { input: 'resources/js/app.js' },
    },
});
```

Build the bundles:

```bash
npm run build          # client bundle (reads vite.config.js)
vite build --ssr resources/js/ssr.js --outDir bootstrap/ssr  # SSR bundle (Vue/React)
```

Point the adapter at the sidecar (`inertia.ssr-enabled=true`,
`inertia.ssr-url=http://localhost:13714/render`) as described below.

---

## Spring Boot SSR Configuration

In `application.properties` or `application.yml`:

```properties
# Enable SSR (canonical hyphenated keys: dotted inertia.ssr.* does NOT bind
# to these flat fields and is silently ignored — verified 2026-09-24)
inertia.ssr-enabled=true

# URL of the Node.js SSR sidecar (must include the /render path, as the
# adapter posts to this URL verbatim)
inertia.ssr-url=http://localhost:13714/render

# Timeouts for SSR requests (durations)
inertia.ssr-connect-timeout=5s
inertia.ssr-read-timeout=10s
```

```yaml
# Flat hyphenated keys under inertia (a nested ssr: block does NOT bind)
inertia:
  ssr-enabled: true
  ssr-url: http://localhost:13714/render
  ssr-connect-timeout: 5s
  ssr-read-timeout: 10s
```

---

## Quarkus SSR Configuration

In `application.properties`:

```properties
# Enable SSR (canonical hyphenated keys)
quarkus.inertia.ssr-enabled=true

# URL of the Node.js SSR sidecar (the adapter appends /render when missing)
quarkus.inertia.ssr-url=http://localhost:13714/render

# Timeouts for SSR requests (durations; there is no ssr-timeout key)
quarkus.inertia.ssr-connect-timeout=5s
quarkus.inertia.ssr-read-timeout=10s
```

---

## SSR Fallback Behavior

When SSR is enabled but the Node.js sidecar is:

- **Unreachable** (connection refused): the adapter falls back to standard client-side rendering (CSR), sending the Inertia page JSON without pre-rendered HTML.
- **Slow** (timeout exceeded): same fallback to CSR.
- **Returns an error** (5xx): same fallback to CSR.

This means SSR failures are **non-fatal** — your application continues to function, just without server-rendered HTML on that request.

---

## Health Check

The Quarkus adapter exposes a SmallRye Health readiness check for the SSR sidecar:

```
GET /q/health/ready
```

If SSR is enabled and the sidecar is reachable, the check passes. Configure monitoring to alert if the SSR sidecar goes down.

For Spring Boot, you can add a custom `HealthIndicator`:

```java
@Component
public class SsrHealthIndicator implements HealthIndicator {
    @Value("${inertia.ssr-url:}") String ssrUrl;
    @Value("${inertia.ssr-enabled:false}") boolean ssrEnabled;

    @Override
    public Health health() {
        if (!ssrEnabled) return Health.up().withDetail("ssr", "disabled").build();
        // ping ssrUrl/ping and return up/down
        return Health.up().build();
    }
}
```

---

## Trust boundary

The Node.js sidecar is a **trust boundary**:

- **Local by default.** Bind it to `localhost` — never expose it to the
  network. It receives the full page object (including server props) and
  returns raw HTML that the adapter injects without sanitization.
  `http://localhost`, `http://127.0.0.1` and `http://[::1]` boot without
  extra config.
- **Remote requires explicit opt-in (validated at startup, M5).** A
  non-local `inertia.ssr-url` boots only when **all** hold:
  `inertia.ssr-remote-enabled=true` **and** an `https` URL **and** the host
  listed in `inertia.ssr-allowed-hosts`. Anything else fails fast with a
  safe message (scheme/host/port only — never tokens or payloads).
  URLs with credentials (`user:pass@`) or fragments never boot. Example:

  ```properties
  inertia.ssr-enabled=true
  inertia.ssr-url=https://ssr.internal.example.com/render
  inertia.ssr-remote-enabled=true
  inertia.ssr-allowed-hosts=ssr.internal.example.com
  ```

- **No redirect-following.** Both HTTP clients disable redirects: any `3xx`
  from the sidecar is a CSR fallback, never a new request to a
  non-validated host.
- Connection/read timeouts apply (`inertia.ssr-connect-timeout`,
  `inertia.ssr-read-timeout`); circuit breaker + CSR fallback stay intact —
  validation never turns an ordinary sidecar outage into a `500`.
  Failures are logged server-side as structured `unreachable / timeout /
  error-status / unknown` events with scheme/host/port only — never
  rendered to the client.
- Cap the sidecar response size at the reverse proxy; on the adapter side,
  `inertia.max-page-bytes` bounds every served page with `413`.

## First-render note (Quarkus)

The first SSR render after boot may log a one-off Vert.x
`BlockedThreadChecker` warning (~2 s) while the JVM warms up serialization
classes on the event-loop thread. It is self-healing: subsequent renders are
fully non-blocking (verified: 1 warning, then 3 consecutive renders clean).
No action needed; pre-warming the sidecar (`curl` one render after deploy)
moves the warmup out of the first user request.

## Production Tips

- Run the SSR sidecar as a separate process managed by your process supervisor (systemd, Supervisor, Docker).
- Use environment variables to configure the SSR URL per environment.
- Monitor SSR sidecar memory usage — Node.js may need periodic restarts under high load.
- Consider running multiple SSR sidecar instances behind a local load balancer for high-throughput applications.
