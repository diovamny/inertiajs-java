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

In `vite.config.js`, add an SSR entry point:

```js
export default defineConfig({
    plugins: [laravel({ input: 'resources/js/app.js' })],
    ssr: {
        input: 'resources/js/ssr.js',
    },
});
```

Build the SSR bundle:

```bash
npm run build        # client bundle
npm run build -- --ssr  # SSR bundle
```

---

## Spring Boot SSR Configuration

In `application.properties` or `application.yml`:

```properties
# Enable SSR
inertia.ssr.enabled=true

# URL of the Node.js SSR sidecar
inertia.ssr.url=http://localhost:13714

# Timeout for SSR requests (milliseconds)
inertia.ssr.timeout=3000
```

```yaml
inertia:
  ssr:
    enabled: true
    url: http://localhost:13714
    timeout: 3000
```

---

## Quarkus SSR Configuration

In `application.properties`:

```properties
# Enable SSR
quarkus.inertia.ssr-enabled=true

# URL of the Node.js SSR sidecar
quarkus.inertia.ssr-url=http://localhost:13714

# Timeout for SSR requests (milliseconds)
quarkus.inertia.ssr-timeout=3000
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
    @Value("${inertia.ssr.url:}") String ssrUrl;
    @Value("${inertia.ssr.enabled:false}") boolean ssrEnabled;

    @Override
    public Health health() {
        if (!ssrEnabled) return Health.up().withDetail("ssr", "disabled").build();
        // ping ssrUrl/ping and return up/down
        return Health.up().build();
    }
}
```

---

## Production Tips

- Run the SSR sidecar as a separate process managed by your process supervisor (systemd, Supervisor, Docker).
- Use environment variables to configure the SSR URL per environment.
- Monitor SSR sidecar memory usage — Node.js may need periodic restarts under high load.
- Consider running multiple SSR sidecar instances behind a local load balancer for high-throughput applications.
