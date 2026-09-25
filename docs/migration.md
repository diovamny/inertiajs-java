# Migration

## Versioning policy

- `0.x` releases may change the public API (`Inertia` facade, SPIs, properties).
  Check the [CHANGELOG](../CHANGELOG.md) before upgrading.
- `1.0.0` will mark a stable public API backed by the green contractual suite
  ([protocol compatibility](protocol-compatibility.md)).

## Upgrading the adapters

Artifacts are published to Maven Central as `io.github.diovamny.spring.inertia`
and `io.github.diovamny.quarkus.inertia` (group `io.github.diovamny`). Bump the version
in your `pom.xml`; no code changes are needed for patch releases.

## 0.0.4 → 0.0.5

- v3-only reaffirmed: no `2.x` client row, no legacy `<div data-page>` mode and
  no legacy API flag (see `COMPATIBILITY_POLICY.md`).
- New files: `docs/COMPATIBILITY_POLICY.md`,
  `specs/inertia-v3-baseline-2026-09-23.yaml`; split rows `PROTO-053B/054B/057B`
  start as `PARCIAL`/`IMPLEMENTADO` and close inside `0.0.5` (M2/M3/M4).
- Build now via Maven Wrapper (`./mvnw` / `.\mvnw.cmd`); clean-clone CI on
  Windows + Linux (M1).
- M2 validation (additive, legacy wire preserved): new core
  `ValidationErrors` (ordered, immutable) + `withValidationErrors` /
  `withErrorMessages` on `InertiaRedirect`/`InertiaRender`/`InertiaResponse`
  (both adapters, overloads avoid `Map` erasure clashes); new flag
  `inertia.validation.all-errors` (default `false` → legacy
  `Map<field,message>`; `true` → `Map<field,List<message>>` in flash, bags
  and Precognition 422, same shape on 3 transports). `japicmp` 0.0.4→0.0.5:
  core/spring/quarkus all MINOR (additive). Quarkus manual
  `InertiaConfig` implementations should add
  `default boolean validationAllErrors() { return false; }`
  (source-level only; binary compatible).
- M3 merge (additive): new core `MergePlan` + `MergeableBuilder` and
  `inertia.mergeable(key, value)...value()` / `applyMergePlan(plan)` on both
  facades (same plan, same wire on 3 transports). Legacy
  `merge(key, value, rule, matchOn...)` unchanged in `0.x`.
- Fase E error bags (behavior fix, 0.0.5): manual
  `back().withErrors(...)` / `withValidationErrors(...)` /
  `withErrorMessages(...)` on `InertiaRedirect`/`InertiaRender`/`InertiaResponse`
  now honor `X-Inertia-Error-Bag` exactly like the validation-exception path
  (new `support.ErrorBags` helper per adapter + unit tests). With a bag, the
  page receives `errors: {<bag>: {...}}`; without one the shape is unchanged
  (flat). Only visits that actually send a bag header are affected.
- M5 SSR (fail-fast, no wire change): new core `SsrEndpointPolicy`; local
  sidecars boot unchanged; remote requires `inertia.ssr-remote-enabled=true`
  + `https` + `inertia.ssr-allowed-hosts`. Both HTTP clients stop following
  redirects (`3xx` → CSR fallback). `japicmp` 0.0.4→0.0.5: core/spring/quarkus
  all MINOR (additive). Quarkus manual `InertiaConfig` implementations should
  add `default boolean ssrRemoteEnabled() { return false; }` and
  `default Optional<List<String>> ssrAllowedHosts() { return Optional.empty(); }`
  (source-level only; binary compatible).

## 0.0.3 → 0.0.4 (breaking)

- **Root template**: delete the `data-page` attribute from
  `<div id="app">`. The page object lives only in
  `<script type="application/json" data-page="app">`
  (`__INERTIA_PAGE_JSON__` placeholder). Legacy `__INERTIA_PAGE__`
  placeholders render empty.
- **`InertiaConfig` (Quarkus) manual implementations**: the new
  `max-page-bytes` method (default 32 MiB) must be implemented
  (`long maxPageBytes() { return 33554432L; }`). CDI-managed configs are
  unaffected.
- **New property** `inertia.max-page-bytes` (default `33554432`, `-1`
  disables): oversized pages now fail with `413` instead of serving
  unbounded payloads.
- **Redirect targets** with control characters or `javascript:`/`data:`/
  `vbscript:`/`file:` schemes are rejected (Spring `400`, Quarkus `400`
  via the error mapper) before any header ships.
- CSRF contract documented as `303 + flash` (behavior unchanged).

## Upgrading a starter

Regenerate is not required: starters are plain Maven projects. To pick up a
new adapter, update the `inertia-adapter.version` property; to pick up new
starter files (e.g. `Dockerfile` improvements), compare with a fresh
generation and copy what you need.

## From Laravel / Rails (PHP/Ruby Inertia adapters)

The frontend does not change: keep your pages/components and the
`@inertiajs/*@3.7.1` client. Only the server side is rewritten. Generate a
starter and port controllers one by one (both adapters verified with the
SSR contract suite on 2026-09-25: server-render + hydrate + CSR fallback).

```bash
# Spring Boot 4.1.x (use -DframeworkVersion=4.1.0) or Quarkus 3.39.2;
# svelte/vue archetypes exist with the same properties.
mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.3.1:generate \
  -DarchetypeCatalog=local \
  -DarchetypeGroupId=io.github.diovamny \
  -DarchetypeArtifactId=inertia-spring-react-archetype -DarchetypeVersion=0.0.5 \
  -DgroupId=com.acme -DartifactId=hello-inertia -Dpackage=com.acme.hello \
  -DappName="Hello Inertia" -DinertiaAdapterVersion=0.0.5 \
  -DjavaVersion=21 -DframeworkVersion=4.1.0
cd hello-inertia/src/main/webui && npm install && npm run build && npm run build:ssr
```

| Laravel / Rails | Spring (`@Inject Inertia inertia`) | Quarkus (JAX-RS, `Uni<Object>`) |
|---|---|---|
| `Inertia::render('Welcome', [...])` / `render inertia: 'Welcome', props:` | `inertia.render("Welcome", Map.of(...))` | same call, return the `Uni` |
| `Inertia::share('key', $v)` in a ServiceProvider / `inertia_share` | `inertia.share("key", value)`, `share(map)` or `share(provider)` | same |
| `withErrors(...)` / validation errors + bags | `back().withErrors(...)` / `withValidationErrors(...)` / `withErrorMessages(...)`; `X-Inertia-Error-Bag` honored exactly like the exception path | same |
| `Inertia::version($v)` / asset versioning | version provider SPI (default manifest/hash chain) | same |
| `Route::get(...)` closures returning Inertia responses | `@Controller` methods returning `inertia.render(...)` | `@Path` resource methods; Reactive Routes `@Route` needs `@Blocking` + the `quarkus-reactive-routes` dependency (`quarkus-vertx-web` does not exist on Quarkus 3.39) |
| `config/inertia.php` (`ssr.enabled`, `ssr.url`) | `inertia.ssr-enabled=true`, `inertia.ssr-url=http://localhost:13714/render` (hyphenated keys; dotted `inertia.ssr.*` is silently ignored) | same keys in `application.properties` |

SSR keeps working through the sidecar: `npm run build:ssr`, then
`node src/main/webui/ssr-server.mjs` (`:13714`) next to the app booted with
`INERTIA_SSR_ENABLED=true`. If the sidecar is down or unreachable, pages fall
back to CSR automatically (never fatal).

Gotchas verified during certification:

- `-DappName` is baked into the generated code (Spring: `@Value` literal),
  so pick the display name at generation time or edit the controller after.
- The root template (`index.html`) must not carry a `data-page` attribute;
  the page object travels in `<script type="application/json"
  data-page="app">` only.
- CSRF is single-token with a `303 + flash` refresh contract on both
  adapters; no per-framework wiring is needed beyond the starter defaults.
