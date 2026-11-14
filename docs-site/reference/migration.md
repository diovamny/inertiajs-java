# Migration

## Versioning policy

- `0.x` releases may change the public API (`Inertia` facade, SPIs,
  configuration properties). Read the [Changelog](/reference/changelog) before
  upgrading.
- `1.0.0` will mark a stable public API backed by the green conformance suites.
- Patch releases are additive unless a security issue forces a breaking change,
  which is called out in the changelog.

## Upgrading the adapters

Artifacts are published to Maven Central under the group
`io.github.diovamny`:

```xml
<dependency>
  <groupId>io.github.diovamny.spring.inertia</groupId>
  <artifactId>spring-inertia</artifactId>
  <version>0.0.6</version>
</dependency>

<dependency>
  <groupId>io.github.diovamny.quarkus.inertia</groupId>
  <artifactId>quarkus-inertia</artifactId>
  <version>0.0.6</version>
</dependency>
```

For most projects only the version changes. The sections below list behavior
changes that may require action.

## 0.0.5 → 0.0.6

### Quarkus session auto-provisioning (behavior change)

`quarkus-inertia` now registers a Vert.x `SessionHandler` with
`LocalSessionStore` on every router by default. Flash messages and
session-backed CSRF work without manual wiring; previously they silently
degraded (lost flash, fail-open CSRF, no startup warning).

If your application registers its own `SessionHandler` (Redis, JDBC, custom
store), disable the auto-provisioning:

```properties
inertia.session.enabled=false
```

Startup then emits a `WARN` describing the consequences. Spring is unaffected:
the HTTP session is available out of the box and the property does not exist
there.

Manual `InertiaConfig` implementations in Quarkus must add:

```java
@Override
public boolean sessionEnabled() {
    return true;
}
```

CDI-managed config mappings are unaffected. The change is source-level only and
binary compatible.

### Live Props (additive)

Declare live props with:

```java
// Spring
inertia.live("stockCount", () -> inventory.count(), 2000,
    LiveChannel.presence("inventory"));

// Quarkus
inertia.live("stockCount", () -> Uni.createFrom().item(inventory.count()), 2000,
    LiveChannel.presence("inventory"));
```

Spring takes `Supplier<Object>`; Quarkus takes `Supplier<Uni<Object>>`. The page
gains a `liveProps` map and resolves the prop on full visits and on partial
reloads that select it. `defer().live()` is supported.

Clients must be on `@inertiajs/*` `3.11.0` to consume the metadata. Applications
without live props keep the exact previous wire shape: `liveProps` is omitted.

### BigInt opt-in (additive)

`inertia.bigint-enabled=true` serializes unsafe 64-bit integers as
`{"$bigint":"..."}` markers and sets `preserveBigIntegers: true` on the page.
The official client revives them as native `BigInt`.

```properties
inertia.bigint-enabled=true
```

Per-response override:

```java
// Spring, before rendering
inertia.preserveBigIntegers(true);

// Spring, fluent after rendering
return ((InertiaRender) inertia.render("Orders/Show", Map.of("id", order.id())))
    .preserveBigIntegers();

// Quarkus
inertia.preserveBigIntegers(true);
return inertia.render("Orders/Show", Map.of("id", order.id()));
```

Precedence is request override > page flag > global flag. Safe values stay plain
JSON numbers. See [Responses & BigInt](/guide/responses).

### Official SSR server (behavior change)

The starters removed the hand-written `ssr-server.mjs` sidecar. They now use
`createServer` from `@inertiajs/*/server`:

```json
{
  "scripts": {
    "build:ssr": "vite build --ssr src/ssr.ts --outDir dist-ssr",
    "ssr": "node dist-ssr/ssr.js"
  }
}
```

The default entry is `dist-ssr/ssr.js` (the supervisor default is
`dist-ssr/ssr.mjs`; set `inertia.ssr-supervisor-entry` if you use the
supervisor). The sidecar listens on `127.0.0.1:13714` and exposes `/render` and
`/health`.

If you kept a custom sidecar, it must still answer `POST /render` with
`{ head, body }` and `GET /health`. The UTF-8 chunk bug from the old sidecar is
gone; do not reimplement manual chunk concatenation.

### Starter CRUD

All six archetypes now generate an in-memory `Contact` CRUD: list, create, edit,
update and delete, an `ErrorPage`, `useForm` field errors and a `success` flash.
Generated POMs enable `<parameters>true</parameters>` so record binding and
explicit `@PathVariable`/`@PathParam` work without `-parameters` surprises.

Regeneration is optional. To adopt the new files, generate a fresh starter in a
temporary directory and copy what you need.

### DevTools protocol excluded

No `/__inertia_devtools` endpoint or request-correlation headers are
implemented. Network-level page-object inspection still works with the official
extension. See [Not Supported](/reference/not-supported).

## 0.0.4 → 0.0.5

- v3-only reaffirmed: no `2.x` client row, no legacy `<div data-page>` mode and
  no legacy API flag.
- New validation core: `ValidationErrors` plus `withValidationErrors` and
  `withErrorMessages` on redirects and renders. `inertia.validation.all-errors`
  defaults to `false` (legacy `Map<field, message>`); `true` emits ordered arrays
  in flash, bags and Precognition.
- New merge core: `MergePlan` and `MergeableBuilder` with
  `inertia.mergeable(key, value)...value()` and `applyMergePlan(plan)`. The
  legacy `merge(key, value, rule, matchOn...)` is unchanged in `0.x`.
- Error bags honor `X-Inertia-Error-Bag` on manual `back().withErrors(...)` calls
  exactly like the validation-exception path.
- SSR endpoint policy: remote sidecars require
  `inertia.ssr-remote-enabled=true`, HTTPS and `inertia.ssr-allowed-hosts`.
  Redirects from the sidecar are never followed (`3xx` becomes a CSR fallback).
- Manual Quarkus `InertiaConfig` implementations should add defaults for
  `validationAllErrors()`, `ssrRemoteEnabled()` and `ssrAllowedHosts()`
  (source-level only).

## 0.0.3 → 0.0.4 (breaking)

- **Root template**: remove `data-page` from `<div id="app">`. The page object
  lives only in `<script type="application/json" data-page="app">`
  (`__INERTIA_PAGE_JSON__`). Legacy `__INERTIA_PAGE__` renders empty.
- **Quarkus `InertiaConfig`**: manual implementations must implement
  `long maxPageBytes() { return 33554432L; }`.
- **New property** `inertia.max-page-bytes` (default 32 MiB, `-1` disables):
  oversized pages answer `413` instead of serving an unbounded payload.
- **Redirect targets** containing control characters or `javascript:`, `data:`,
  `vbscript:` or `file:` schemes are rejected with `400` before any header is
  sent.
- CSRF contract documented as `303 + flash` (behavior unchanged).

## Upgrading a starter

Starters are plain Maven projects; regeneration is not required. To pick up a
new adapter, change the `inertia-adapter.version` property:

```xml
<properties>
  <inertia-adapter.version>0.0.6</inertia-adapter.version>
</properties>
```

To pick up new starter files (Dockerfile, SSR script, CRUD controller), compare
with a fresh generation and copy what you need.

## From Laravel or Rails

The frontend does not change: keep your pages, components and the
`@inertiajs/*@3.11.0` client. Only the server side is rewritten.

```bash
mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.3.1:generate \
  -DarchetypeCatalog=local \
  -DarchetypeGroupId=io.github.diovamny \
  -DarchetypeArtifactId=inertia-spring-react-archetype \
  -DarchetypeVersion=0.0.6 \
  -DgroupId=com.acme -DartifactId=hello-inertia -Dpackage=com.acme.hello \
  -DappName="Hello Inertia" -DinertiaAdapterVersion=0.0.6 \
  -DjavaVersion=21 -DframeworkVersion=4.1.1
```

| Laravel / Rails | Spring | Quarkus |
|---|---|---|
| `Inertia::render('Welcome', [...])` | `inertia.render("Welcome", Map.of(...))` | Same call, return `Uni<Object>` |
| `Inertia::share('key', $v)` | `inertia.share("key", value)` | Same |
| `withErrors(...)` | `back().withErrors(...)` | Same |
| `Inertia::version($v)` | Version provider SPI or `setVersion` | `version(...)` |
| Route closures returning Inertia | `@Controller` methods | `@Path` methods or `@Route` |
| `config/inertia.php` | `inertia.*` properties | Same keys |

Use hyphenated keys (`inertia.ssr-enabled`). Dotted keys such as
`inertia.ssr.enabled` are silently ignored, which is the most common migration
mistake.

## Checklist

- [ ] Adapter versions bumped in every module.
- [ ] Quarkus `SessionHandler` ownership decided (auto or manual).
- [ ] Manual `InertiaConfig` implementations updated.
- [ ] Root template uses the v3-pure script, not `data-page` on the mount div.
- [ ] SSR entry updated to `dist-ssr/ssr.js` and bound to `127.0.0.1`.
- [ ] Security mode explicit in production.
- [ ] Tests assert `409`, partial reloads and validation after the upgrade.
- [ ] Changelog read for behavior changes, not only new features.

Next: [Changelog](/reference/changelog) · [Configuration](/reference/configuration)
