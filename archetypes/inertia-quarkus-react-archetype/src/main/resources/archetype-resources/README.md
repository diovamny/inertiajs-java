# ${artifactId} (Quarkus + React 19 + Inertia.js)

Minimal Inertia.js v3 starter. Base only, not a production security setup.

## Requirements

- JDK 21, Maven 3.9+, Node 22

## Develop

```text
npm --prefix src/main/webui install
npm --prefix src/main/webui run dev
mvn quarkus:dev
```

## Production build

```text
npm --prefix src/main/webui install
npm --prefix src/main/webui run build
mvn -B package
```

Open `http://localhost:8080/` — smoke test: heading `I \u2665 Inertia.js`, link `Visit Inertia.js`, no console errors.

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
