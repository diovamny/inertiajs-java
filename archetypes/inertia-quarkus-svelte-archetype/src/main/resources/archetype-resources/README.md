# ${artifactId} (Quarkus + Svelte 5 + Inertia.js)

Inertia.js v3 starter with a working contacts CRUD (`Contacts/Index`,
`Contacts/Create`, `Contacts/Edit`) on Quarkus. Base only, not a production
security setup.

## Requirements

- JDK 21, Maven 3.9+, Node 22+

## Develop (two terminals)

- Terminal 1 (Vite dev server, `:5173`):
  `npm --prefix src/main/webui install` then
  `npm --prefix src/main/webui run dev`
- Terminal 2 (backend, `:8080`): `mvn quarkus:dev`

Open `http://localhost:5173/`. Vite serves the frontend with HMR; the
commented `proxy` entry in `vite.config.ts` forwards API calls to the
backend when needed.

Routes: `/` welcome, `/contacts` list, `/contacts/create`,
`/contacts/{id}/edit`.

## Session

The adapter auto-registers a Vert.x session for flash messages and
session-based CSRF. Set `inertia.session.enabled=false` only if you register
your own `SessionHandler`.

## Tests

```text
mvn test
```

`ContactsResourceTest` drives the CRUD through the real Inertia contract
(page objects, `303 + Location` mutations, validation errors flashed back to
the form). CSRF stays enabled: the test fetches the `XSRF-TOKEN` cookie and
replays it as `X-XSRF-TOKEN`, exactly like the frontend.

## SSR (optional sidecar)

```text
npm --prefix src/main/webui run build
npm --prefix src/main/webui run build:ssr
npm --prefix src/main/webui run ssr
```

Then uncomment `inertia.ssr-enabled=true` in
`src/main/resources/application.properties` and restart the backend. The
sidecar listens on `:13714`; the client-side fallback stays active while it
is down.

## Production build

```text
npm --prefix src/main/webui install
npm --prefix src/main/webui run build
mvn -B package
```

Open `http://localhost:8080/`.

## Troubleshooting

- **Disabled session warning**: `inertia.session.enabled=false` logs a
  warning because flash messages and session-based CSRF stop working unless
  the application registers its own `SessionHandler`.
- **`409 Conflict` + `X-Inertia-Location`**: an Inertia visit that asks for
  a full-page redirect (`back("/somewhere")`, external URLs, asset-version
  mismatch) answers `409` and the client reloads that location. The starter
  uses `back()` without a fallback, so failed validation returns
  `303 + Location` and stays a SPA visit.
- **CSRF**: the adapter issues an `XSRF-TOKEN` cookie; the frontend replays
  it as `X-XSRF-TOKEN` on mutation requests.
- **`@` alias**: `tsconfig.json` maps `@/*` to `./src/*`; keep it aligned
  with `vite.config.ts`.
- **TS 6 and `import.meta.dirname`**: `tsconfig.node.json` sets
  `types: ["node"]`, required by `vite.config.ts` and the SSR server.

## Compatibility

| Component | Version |
|-----------|---------|
| Java | 21 |
| Quarkus | 3.40.1 (default), 3.39.2 (legacy) |
| Inertia.js | 3.8.0 |
| TypeScript | 6.0.3 |
| Node | 24.21+ |

## Native image + Docker

The `Dockerfile` builds a GraalVM native binary (Mandrel `jdk-25` builder,
explicit project decision; the app targets Java 21) and packs it into UBI Micro.
The frontend (`npm install` + `npm run build`) is built inside the image.

```text
docker build -t ${artifactId} .
docker run --rm -p 8080:8080 ${artifactId}
```

Native builds take several minutes. You can also build the binary locally
(requires GraalVM/Mandrel 25): `./mvnw package -DskipTests -Pnative`.

- Inertia: https://inertiajs.com/
- App name prop: ${appName}
