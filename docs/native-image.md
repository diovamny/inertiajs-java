# GraalVM Native Image

Both adapters are native-ready and every starter kit ships a native
`Dockerfile` (Mandrel builder + UBI Micro runtime).

## Spring Boot starter

```bash
docker build -t hello-inertia .
docker run --rm -p 8080:8080 hello-inertia
```

The generated `pom.xml` wires a `native` profile (`process-aot` +
`native-maven-plugin:compile-no-fork`). To build the binary locally you need
GraalVM/Mandrel for JDK 25:

```bash
./mvnw package -DskipTests -Pnative
```

The adapter registers Jackson reflection hints for all model records
(`PageObject`, props, `RawJson`); no extra configuration is needed for the
starter. If your own DTOs fail native serialization, register them with
`@ImportRuntimeHints` (Spring) or `@RegisterForReflection` (Quarkus).

## Quarkus starter

```bash
docker build -t hello-inertia .
docker run --rm --name app -p 8080:8080 hello-inertia
```

The generated `pom.xml` wires a `native` profile
(`quarkus.package.type=native`).

## Known limitations

- Native builds take 10–20 minutes; the frontend (`npm install` + `vite build`)
  runs inside the builder image and needs network access.
- SSR sidecar is a separate Node.js process; containerize it independently if
  you enable `inertia.ssr-enabled`.
- The `docker build` of a starter resolves the adapter from Maven Central, so
  native images require a published adapter release (see [migration](migration.md)).
