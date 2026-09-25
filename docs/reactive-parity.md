# Quarkus Reactive Routes — parity matrix (release-0.0.5, M7)

Reactive Routes (`@RouteBase` + `@Route`) is a stable target in `0.0.5`:
same contract as Spring MVC and Quarkus REST. No `experimental` label.
Every difference below is either closed in this release or a justified
`NO_APLICA` row — never a hidden note.

## Transport parity by area

| Area | Spring MVC | Quarkus REST | Quarkus Reactive | Evidence |
|---|---|---|---|---|
| Handshake / partials / deferred / once | ✅ | ✅ | ✅ | TCK `01/02/03` on 3 stacks (`TckSpringTest`, `TckQuarkusTest`, `TckReactiveTest` 42 cases + dedicated 409) |
| Redirects 302/303/409 + back + versioning | ✅ | ✅ | ✅ | TCK `04/05` on 3 stacks; reactive `submit/put-me/delete-me/back/versioned` endpoints |
| CSRF 303 + flash / 419 | ✅ | ✅ | ✅ | TCK `06` on 3 stacks (incl. `plain-post-without-token-diverges` → 419) |
| Validation bags + multi-message arrays | ✅ | ✅ | ✅ | TCK `07` incl. multi-message on 3 stacks; `ReactiveStressTest` bags |
| Merge / append / nested routes / reset | ✅ | ✅ | ✅ | TCK `08` incl. `merge-nested` + child reset on 3 stacks |
| Scroll + infinite-scroll intent | ✅ | ✅ | ✅ | TCK `09` on 3 stacks |
| Uploads multipart | ✅ | ✅ | ✅ | TCK `10` on 3 stacks |
| SSR fallback (CSR on sidecar failure) | ✅ | ✅ | ✅ | `SsrHandler*` suites; shared `SsrEndpointPolicy` + validators |
| Concurrency isolation | ✅ | ✅ | ✅ | `ReactiveStressTest` (8 identities × 12 rounds: sessions/flash, CSRF, 303/409, bags, once/deferred/partial/reset) + `InertiaContextLocalsTest` |
| Version 409 location path | `/tck/...` | `/tck/...` | `/tck-reactive/...` | Same semantics, transport-local path (harness prefix, not a divergence); dedicated `staleVersionIs409WithReactivePath` |

## M7 concurrency fix (G-17 class, found by `ReactiveStressTest`)

Vert.x event-loop context locals are shared across requests on the same
loop and visible to `@Blocking` workers, so per-request header reads could
cross-talk under load. Fixed in `0.0.5`:

- `InertiaContextLocals.routingContext(ctx)`: resolution order is now
  request-scoped holder → Vert.x local → `CurrentVertxRequest` → CDI
  `RoutingContext`; `get()` prefers the routing context and never reads the
  shared map from worker threads; `put(key, null)` clears both stores.
- `InertiaVertxHandler` binds the request-scoped holder for every reactive
  request; `SharedDataRegistry` / `OncePropRegistry` / `PageObjectBuilder`
  sessions / `VertxSessionFlashStore` / `ReactiveResponseWriter` /
  `InertiaImpl` resolve through the hardened helper (no raw ctx-local reads).
- `ReactiveStressTest` is the permanent regression guard (also runs in the
  `reactive-stress` nightly workflow). During M7 it additionally caught and
  fixed a test bug (Jackson `MissingNode.isNull()` is `false`; assertions
  now accept absent-or-null like the TCK).

## Operation

- Prefer `@Blocking` for blocking I/O (classic JPA/JDBC); leave it off with
  reactive clients.
- Reactive CSRF in `framework` mode: declare mutating prefixes via
  `inertia.security.reactive-csrf-paths` (quarkus-rest-csrf only sees JAX-RS).
- Stress profile: `ReactiveStressTest` (default suite) + nightly
  `.github/workflows/reactive-stress.yml` (extended rounds).
