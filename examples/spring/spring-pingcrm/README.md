# Spring Boot PingCRM Demo

Port of the official [inertiajs/pingcrm](https://github.com/inertiajs/pingcrm)
demo (Laravel + Vue 3) built with **Spring Boot 4.1 + [spring-inertia](../../../spring-inertia/)**.
The frontend is the same custom Vue 3 app used by the
[Quarkus port](../quarkus/pingcrm/) — no CDN, real Vite + Tailwind 4 build.

## Features

- Session auth (PBKDF2-hashed passwords) with login/logout and guarded routes
- Dashboard, Contacts (search, pagination, trash/restore, photo upload), Organizations,
  Users (roles, avatars, trash/restore) and Reports
- Flyway migrations + H2 file database, seeded demo data (faker)
- Laravel-style flash messages and validation errors (`props.success`,
  `props.errors`, `props.old`) via 303/302 redirects
- Vite asset versioning (`vite-manifest`) and optional SSR with Node

## Run

```bash
# from the repo root (builds the adapters first)
mvn clean install -DskipTests -pl spring-inertia -am

# build + test the demo
mvn -f examples/spring/spring-pingcrm/pom.xml clean test

# run (http://localhost:8080)
mvn -f examples/spring/spring-pingcrm/pom.xml spring-boot:run
```

The first launch creates the H2 database in `data/spring-pingcrm.mv.db`,
runs the Flyway migrations and seeds demo data.

### Credentials

| Email | Password | Role |
| --- | --- | --- |
| `johndoe@example.com` | `secret` | owner |

## Frontend development

```bash
cd examples/spring/spring-pingcrm/src/main/webui
npm install
npm run dev      # Vite dev server on http://localhost:5173
```

For production builds the `frontend-maven-plugin` runs `npm install` + `vite build`
(downloading Node 22 automatically); the compiled bundle is committed in
`src/main/resources/static` so the app also builds and runs without npm.

### SSR (optional)

```bash
cd examples/spring/spring-pingcrm/src/main/webui
npm run build:ssr
node dist-ssr/ssr.js        # Inertia SSR server on http://localhost:13714
```

Then enable it with `inertia.ssr-enabled=true` in
`src/main/resources/application.properties`. SSR is disabled by default.

## CSRF

Like the Quarkus pingcrm demo, CSRF validation is disabled
(`inertia.csrf-enabled=false`): the Inertia v3 fetch client does not send the
`X-XSRF-TOKEN` header automatically. See the
[spring-inertia README](../../../spring-inertia/README.md) for the adapter's
cookie-based CSRF support.

## Tests

`PingCrmSpringTest` ports the Quarkus demo suite (25 scenarios) to MockMvc:
auth redirects, pagination/search, CRUD + trash/restore, photo upload, image
serving and logout. Tests use an in-memory H2 database seeded per JVM.

```bash
mvn -f examples/spring/spring-pingcrm/pom.xml test
```