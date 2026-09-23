# SSR

Node.js sidecar rendering with CSR fallback:

```properties
inertia.ssr-enabled=true
inertia.ssr-url=http://localhost:13714
```

Failures classify as `unreachable / timeout / error-status / unknown` with
hints — logged server-side, never rendered. The sidecar is a trust
boundary: bind `localhost`, keep timeouts tight (see the full
[SSR setup](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/docs/ssr-setup.md)).
