# Changelog

Release notes for the Inertia.js Java adapters. The canonical history lives in
`CHANGELOG.md` at the repository root; this page summarizes the changes that
affect application code and documentation.

## Unreleased — toward 1.0.0

- Per-response BigInt opt-in: `inertia.preserveBigIntegers(true/false)` in both
  adapters (Spring facade plus fluent `InertiaRender` re-serialization; Quarkus
  facade). Precedence: request override > page flag > global
  `inertia.bigint-enabled`.
- `java.math.BigInteger` support in `BigIntSerializer` and both Jackson modules.
- `PageObject` gains the tristate `preserveBigIntegers` component and
  `InertiaPageResult.of(component, props, preserveBigIntegers)`.
- TCK `PROTO-BigInt-03/04/05`; C100 65/65 and I100 90/90 at the time of the
  change.

## 0.0.6

### Added

- **Live Props** (`inertia.live`): wire contract and real-time resolution with
  public, private and presence channels, event listeners and configurable
  throttle. `PageObject` gains component #20 `liveProps`, omitted when empty.
  Spring takes `Supplier<Object>`, Quarkus REST and Reactive take
  `Supplier<Uni<Object>>`. Full visits resolve every live prop, partial reloads
  resolve only selected keys, and `defer().live()` emits the key in both
  `deferredProps` and `liveProps`.
- TCK `11-live-props.yaml` (six cases) green on Spring MVC, Quarkus REST and
  Quarkus Reactive Routers, plus dedicated integration tests and isolation
  coverage in `ReactiveStressTest`.
- E2E Live Props (`LP-01..LP-04`) green against the Spring and Quarkus kitchen
  sinks, including simulate/re-render in a real browser.
- ADR-007 and the `NOT_SUPPORTED` declaration for the Inertia DevTools server
  protocol.
- Starter CRUD of `Contact` in all six archetypes: list, create, edit, update
  and delete, `ErrorPage`, `useForm` field errors, `success` flash and
  `<parameters>true</parameters>` in generated POMs.
- BigInt opt-in (`inertia.bigint-enabled`, default `false`) with
  `BigIntSerializer` in `inertia-core`; TCK `PROTO-BigInt-01/02` and
  `PROTO-Version-History-01`.

### Changed

- Official client aligned to `@inertiajs/{core,vue3,react,svelte}` `3.11.0`,
  `@inertiajs/vite` `^3.11.0` and TypeScript `^6.0.3`.
- Official SSR: all six starters drop `ssr-server.mjs` and use `createServer`
  from `@inertiajs/*/server`. `npm run build:ssr` emits `dist-ssr/ssr.js`;
  `npm run ssr` starts it on port `13714`.
- Quarkus `3.40.1` LTS as default (dual CI matrix with `3.39.2`); Spring CI
  `framework-version` aligned to `4.1.1`.
- Vert.x session auto-provisioned on Quarkus (`inertia.session.enabled=true`,
  `order(-2)`): flash and session CSRF work without manual wiring.
- Tests: CRUD cycle per archetype with MockMvc/RestAssured, auto-provisioned
  session tests, TypeScript 6 typecheck gates and CRUD smoke in CI.

### Fixed

- E2E-05: rescue and scroll notes closed with real assertions (rescued slot
  restored through `X-Force-Success`; page append and scroll depth restored
  after `goBack()`), green on Spring MVC and Quarkus REST.
- DevTools documentation replaced with the precise architectural decision from
  ADR-007.
- SSR UTF-8 bug in the old sidecar (`raw += chunk`) removed by the migration to
  the official server.

## 0.0.5

- v3-only policy and honest compatibility matrix: split rows for validation bags
  vs multi-message, merge wire vs explicit `append()`, instant-visit
  serialization vs client behavior.
- Maven Wrapper 3.9.11, Enforcer (Java 21+/Maven 3.9+), Node `24.21.0` and npm
  `11.17.0` pins, clean-checkout CI on Ubuntu and Windows.
- Core `ValidationErrors` plus `withValidationErrors`/`withErrorMessages` and
  `inertia.validation.all-errors` (default `false`).
- Core `MergePlan` plus `MergeableBuilder`,
  `inertia.mergeable()...value()`/`applyMergePlan`; legacy `merge()` unchanged.
- `SsrEndpointPolicy` with startup fail-fast and HTTP clients that do not follow
  redirects.
- Reactive Routes graduated to stable: 43-case TCK parity,
  `ReactiveStressTest` with 8×12 concurrent identities, G-17 request-context
  isolation fix and a nightly `reactive-stress` workflow.
- Playwright coverage with the official Vue client: validation, bags,
  merge/append, instant visits, uploads, `409` and partial reloads.
- Quality gates: Checkstyle, SpotBugs High, `japicmp` 0.0.4→0.0.5 MINOR,
  pitest report-only on the local station, SCA fail-closed on tags and SBOM
  aggregation.

## Earlier releases

| Version | Highlights |
|---|---|
| `0.0.4` | v3-pure root template (`data-page` on a JSON script), `max-page-bytes` (`413`), redirect target validation, stable Reactive Routes |
| `0.0.3` | Initial public adapters, deferred/once/merge props, SSR sidecar, validation bags |
| `0.0.2` | Spring Boot and Quarkus auto-configuration, starters |
| `0.0.1` | First internal milestone |

## Compatibility and support

- Supported JDK: **21+**.
- Spring Boot: **4.1.x** (certified with `4.1.1`).
- Quarkus: **3.40.x** (certified with `3.40.1`).
- Official clients: `@inertiajs/*` **3.11.0**.
- Protocol: Inertia.js **v3 only**. There is no v1/v2 compatibility mode.

## Reading the changelog

- **Added** — new API or wire capability. Usually additive.
- **Changed** — behavior or default that may require action.
- **Fixed** — bug fix; upgrade recommended.
- **Breaking** — explicitly marked and documented in
  [Migration](/reference/migration).

Every release note that changes behavior links to the relevant guide page. If a
change is not documented there, treat it as a bug in the documentation and open
an issue.

Next: [Migration](/reference/migration) · [Not Supported](/reference/not-supported)
