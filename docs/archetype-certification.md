# Archetype certification (release-0.0.5, Fase B)

Every starter (Spring/Quarkus × Vue/React/Svelte) must pass, per archetype:

1. **Generate** from the local catalog with pinned versions
   (`-DarchetypeVersion=0.0.5 -DinertiaAdapterVersion=0.0.5`).
2. **Declare `inertia-core` explicitly**, pinned to the same adapter version
   (`inertia-core.version = ${inertiaAdapterVersion}` — enforced by
   `node scripts/check-versions.mjs`, §3b).
3. **Build + test**: `./mvnw -B test` green (Java + `npm install` + `vite build`).
4. **Boot + visit**: packaged jar boots; `GET /` serves the HTML shell with
   `id="app"`; `GET /` with `X-Inertia: true` returns the JSON page with
   `"component":"Welcome"`. Evidence: CI job `archetypes` (+ boot log artifact
   on failure).
5. **Client 3.7.1**: generated `src/main/webui/package.json` declares the
   official adapter at exactly `3.7.1` with `save-exact=true` (enforced by
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
