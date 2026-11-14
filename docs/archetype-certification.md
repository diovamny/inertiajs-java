# Archetype certification (release-0.0.6, Fase B)

Every starter (Spring/Quarkus × Vue/React/Svelte) must pass, per archetype:

1. **Generate** from the local catalog with pinned versions
   (`-DarchetypeVersion=0.0.6 -DinertiaAdapterVersion=0.0.6`).
2. **Declare `inertia-core` explicitly**, pinned to the same adapter version
   (`inertia-core.version = ${inertiaAdapterVersion}` — enforced by
   `node scripts/check-versions.mjs`, §3b).
3. **Build + test**: `./mvnw -B test` green (Java + `npm install` + `vite build`).
4. **Boot + visit**: packaged jar boots; `GET /` serves the HTML shell with
   `id="app"`; `GET /` with `X-Inertia: true` returns the JSON page with
   `"component":"Welcome"`. Evidence: CI job `archetypes` (+ boot log artifact
   on failure).
5. **Client 3.11.0**: generated `src/main/webui/package.json` declares the
   official adapter at exactly `3.11.0` with `save-exact=true` (enforced by
   `node scripts/check-client-versions.mjs`).

| Archetype | Generate | Test | Boot | Visit HTML | Visit JSON |
|---|:---:|:---:|:---:|:---:|:---:|
| inertia-spring-vue-archetype | ✅ CI | ✅ CI | ✅ CI | ✅ CI | ✅ CI |
| inertia-spring-react-archetype | ✅ CI | ✅ CI | ✅ CI | ✅ CI | ✅ CI |
| inertia-spring-svelte-archetype | ✅ CI | ✅ CI | ✅ CI | ✅ CI | ✅ CI |
| inertia-quarkus-vue-archetype | ✅ CI | ✅ CI | ✅ CI | ✅ CI | ✅ CI |
| inertia-quarkus-react-archetype | ✅ CI | ✅ CI | ✅ CI | ✅ CI | ✅ CI |
| inertia-quarkus-svelte-archetype | ✅ CI | ✅ CI | ✅ CI | ✅ CI | ✅ CI |

(`✅ CI` = green in `.github/workflows/ci.yml` job `archetypes` on this branch.)

## Local certification (2026-11-10)

The six starters were generated from the locally installed `0.0.6` catalog
(`-DarchetypeVersion=0.0.6 -DinertiaAdapterVersion=0.0.6`). Every generated
project passed the static contract checks: client `3.11.0`,
`inertia-core`/adapter pinned to `0.0.6`, `<parameters>true</parameters>`,
no `ssr-server.mjs`, Vue tsconfig without `react-jsx`, and README `3.11.0`.

End-to-end spot checks, one per stack:

| Starter | Build + test | Boot + visit |
|---|:---:|:---:|
| inertia-spring-vue-archetype | ✅ 8/8 | ✅ HTML shell + JSON `Welcome` |
| inertia-quarkus-vue-archetype | ✅ 8/8 | ✅ HTML shell + JSON `Welcome` |

The React/Svelte variants are exercised by the CI `archetypes` matrix; they
share the same generation path and contract checks.
