# ADR 005 — SSR endpoint fail-fast policy (P100-10)

- Status: accepted (release-0.0.5, M5).
- Context: docs asked for caution with remote SSR sidecars but nothing
  rejected an insecure destination at startup (SSRF-shaped risk: credentials
  in URL, plain-http remote, unlisted host, redirect following).
- Decision: pure core `SsrEndpointPolicy.validate(url, remoteEnabled,
  allowedHosts)`. Local-only by default (`localhost`/`127.0.0.1`/`::1`
  over HTTP, no extra config); remote requires all of
  `inertia.ssr-remote-enabled=true` + `https` + host in
  `inertia.ssr-allowed-hosts`; userinfo/fragment/non-http(s) always
  rejected; messages carry scheme/host/port only. Both adapters validate at
  startup when `ssr-enabled=true` (fail fast, no boot on insecure remote)
  and their HTTP clients never follow redirects (`3xx` → CSR fallback;
  timeout/breaker/size-limit/fallback behavior unchanged).
- Consequences: local setups boot unchanged; insecure remotes fail with a
  safe message; no wire change.
