# Demo certification (release-0.0.5, Fase C)

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
| spring-pingcrm | Vue 3.7.1 | 8080 | `/login` | `Auth/Login` | ✅ |
| spring-pingcrm-react | React 3.7.1 | 8080 | `/login` | `Auth/Login` | ✅ |
| spring-pingcrm-svelte | Svelte 3.7.1 | 8181 | `/login` | `Auth/Login` | ✅ |
| spring-kitchen-sink | Vue 3.7.1 | 8080 | `/login` | `Auth/Login` | ✅ |
| quarkus demo-app | Vue 3.7.1 | 8080 | `/persons` | `Persons/Index` | ✅ |
| quarkus kitchen-sink | Vue 3.7.1 | 8081 | `/login` | `Auth/Login` | ✅ |
| quarkus pingcrm | Vue 3.7.1 | 8081 | `/login` | `Auth/Login` | ✅ |
| quarkus pingcrm-react | React 3.7.1 | 8080 | `/login` | `Auth/Login` | ✅ |
| quarkus-pingcrm-svelte | Svelte 3.7.1 | 8082 | `/login` | `Auth/Login` | ✅ |

(`✅` = certified this branch: packaged jar, booted, shell + JSON visit.)

## Fixes landed in Fase C

- `spring-pingcrm-react` had no `webui`: ported from `quarkus/pingcrm-react`
  (React 3.7.1, `app.tsx`, all Pages/Shared, `tsconfig.json`), Spring-style
  `vite.config.ts` (outDir `static` + `.vite/manifest.json` emit),
  `templates/index.html` root template, `package.json` + fresh lockfile,
  `.npmrc` `save-exact`.
- Missing `repackage` in `spring-pingcrm-react` (181 KB non-bootable jar →
  59 MB fat jar); same gap fixed earlier in Spring archetypes (Fase B).
- Missing Spring Boot BOM in `spring-pingcrm-react` (no managed Hibernate →
  `ClassNotFoundException: net.bytebuddy` at boot); aligned with
  `spring-pingcrm` (`spring-boot-dependencies` 4.1.0).
- `spring-pingcrm(-react|-svelte)` use `<inertia.version>0.0.5</inertia.version>`
  pins like the other demos (checked by `check-versions.mjs` §4, extended).
- `spring-pingcrm-react|svelte` registered in `examples/spring/pom.xml`
  (were reactor orphans).
