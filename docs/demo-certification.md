# Demo certification (release-0.0.6, Fase C)

Every demo must pass, per demo: `package` (Java + `npm ci` + `vite build`
with pinned clients) → boot → `GET` probe path returns the v3 HTML shell
(`id="app"`, payload once in script) → same path with `X-Inertia: true`
returns the JSON page with the expected component. Evidence: CI job
`demo-visit` (+ this audit for the local run).

**Boot rule:** start each demo with CWD = its module dir. H2 file DBs
(`./data/…`) and upload dirs resolve relative to CWD; Spring and Quarkus
PingCRM demos share the `./data/pingcrm` name, so booting both from the
repo root corrupts the file DB (`Wrong user name or password`). Stray
repo-root `data/*.mv.db` files are gitignored runtime residue, not sources.

| Demo | Client | Port | Probe | Component | Cert |
|---|:---:|---:|---|---|:---:|
| spring-pingcrm | Vue 3.11.0 | 8080 | `/login` | `Auth/Login` | ✅ |
| spring-pingcrm-react | React 3.11.0 | 8080 | `/login` | `Auth/Login` | ✅ |
| spring-pingcrm-svelte | Svelte 3.11.0 | 8181 | `/login` | `Auth/Login` | ✅ |
| spring-kitchen-sink | Vue 3.11.0 | 8080 | `/login` | `Auth/Login` | ✅ |
| quarkus demo-app | Vue 3.11.0 | 8080 | `/persons` | `Persons/Index` | ✅ |
| quarkus kitchen-sink | Vue 3.11.0 | 8081 | `/login` | `Auth/Login` | ✅ |
| quarkus pingcrm | Vue 3.11.0 | 8081 | `/login` | `Auth/Login` | ✅ |
| quarkus pingcrm-react | React 3.11.0 | 8080 | `/login` | `Auth/Login` | ✅ |
| quarkus-pingcrm-svelte | Svelte 3.11.0 | 8082 | `/login` | `Auth/Login` | ✅ |

(`✅` = certified this branch: packaged jar, booted, shell + JSON visit.)

## Live Props certification (Fase 6, 2026-11-09)

Both kitchen-sink jars were packaged with the pinned 3.11.0 clients and the
Live Props page compiled into the Vite manifest
(`resources/js/pages/Features/LiveProps/Index.vue` → `assets/Index2.js`):

| Demo | Boot | Health | Live Props route | E2E result |
|---|---|:---:|---|---|
| spring-kitchen-sink | CWD = module, 8080 | UP | `/features/live-props` | 18/18 green |
| quarkus kitchen-sink | CWD = module, 8081 | UP | `/features/live-props` | 18/18 green |

Command (same suites against both jars, one worker to avoid cold-start
contention on Windows):

```powershell
cd e2e
$env:E2E_BASE_URL='http://localhost:8080'  # then 8081
npx playwright test inertia-contracts.spec.ts live-props-contracts.spec.ts --workers=1
```

- `live-props-contracts.spec.ts`: LP-01 (`liveProps.stockCount` on the
  private `inventory` channel with `StockUpdated`), LP-02 (presence/private
  wire shape), LP-03 (simulate then partial reload returns the fresh value),
  LP-04 (`throttle: 2000`), plus the browser simulate/re-render proof.
- `inertia-contracts.spec.ts`: 13 cases including server validation errors,
  named error bags, merge append, instant visit, upload, 409, redirect-back
  flash, and the new E2E-05 depths (rescue slot via `X-Force-Success`;
  infinite-scroll append + `window.scrollY` restored after `goBack()`).

The Quarkus Live Props demo runs on JAX-RS; reactive parity for the same
wire/partial/lazy contract is certified by `11-live-props.yaml` and
`LivePropsReactiveQuarkusTest`, not claimed as a browser cell here.

## Fixes landed in Fase C

- `spring-pingcrm-react` had no `webui`: ported from `quarkus/pingcrm-react`
  (React, `app.tsx`, all Pages/Shared, `tsconfig.json`), Spring-style
  `vite.config.ts` (outDir `static` + `.vite/manifest.json` emit),
  `templates/index.html` root template, `package.json` + fresh lockfile,
  `.npmrc` `save-exact`.
- Missing `repackage` in `spring-pingcrm-react` (181 KB non-bootable jar →
  59 MB fat jar); same gap fixed earlier in Spring archetypes (Fase B).
- Missing Spring Boot BOM in `spring-pingcrm-react` (no managed Hibernate →
  `ClassNotFoundException: net.bytebuddy` at boot); aligned with
  `spring-pingcrm` (`spring-boot-dependencies` 4.1.0).
- `spring-pingcrm(-react|-svelte)` use `<inertia.version>0.0.6</inertia.version>`
  pins like the other demos (checked by `check-versions.mjs` §4, extended).
- `spring-pingcrm-react|svelte` registered in `examples/spring/pom.xml`
  (were reactor orphans).
