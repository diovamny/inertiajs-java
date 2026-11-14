# Native Image

Both adapters are GraalVM-native ready. Every starter kit ships a multi-stage
`Dockerfile` that builds the frontend, runs the AOT step and produces a small
UBI Micro runtime image. Native images cut startup to tens of milliseconds and
reduce memory, at the cost of a long build.

## What is verified

The release pipeline generates a starter, builds the native binary and boots it:

1. Generate the archetype (`0.0.6`).
2. `npm install && npm run build` for the frontend.
3. `./mvnw package -DskipTests -Pnative`.
4. Boot the binary.
5. Assert the initial HTML and the Inertia JSON response over HTTP.

The adapter ships Jackson reflection hints for every wire record (`PageObject`,
props, `RawJson`, live metadata, validation errors), so the generated starter
needs no extra configuration.

## Requirements

| Tool | Version |
|---|---|
| JDK | 21+ to compile the application |
| GraalVM / Mandrel | JDK 25 toolchain for the native build |
| Docker | Any recent version (for the container path) |
| Network | The builder image runs `npm install` |

The `Dockerfile` pins
`quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25` for both adapters.
Local native builds need the same toolchain; a stock JDK cannot run
`native-image`.

## Spring Boot starter

```bash
# from the generated project root
docker build -t hello-inertia .
docker run --rm -p 8080:8080 hello-inertia
```

The generated `pom.xml` activates a `native` profile with:

- `spring-boot-maven-plugin:process-aot` (Spring AOT);
- `native-maven-plugin:compile-no-fork` bound to `package`, with
  `<classesDirectory>${project.build.outputDirectory}</classesDirectory>` so
  `native-image` sees the exploded classes instead of the repackaged
  `BOOT-INF` layout.

Local build:

```bash
./mvnw package -DskipTests -Pnative
./target/hello-inertia
```

The executable name matches the artifact ID. Startup should complete in well
under a second.

## Quarkus starter

```bash
docker build -t hello-inertia .
docker run --rm --name app -p 8080:8080 hello-inertia
```

The `native` profile sets `quarkus.package.type=native`. Local build:

```bash
./mvnw package -DskipTests -Pnative
./target/*-runner -Dquarkus.http.host=0.0.0.0
```

Quarkus performs its own build-time augmentation. Most extension metadata is
generated automatically; the adapter's model classes are already registered.

## Reflection hints

The adapter registers its own types. Register **your** DTOs when they are
serialized reflectively:

::: code-group

```java [Spring Boot]
@ImportRuntimeHints(ContactHints.class)
@Configuration
public class NativeConfig {
}

public class ContactHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(ContactDto.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS);
    }
}
```

```java [Quarkus]
@RegisterForReflection(targets = { ContactDto.class })
public class ReflectionConfig {
}
```

:::

Records serialized by Jackson usually work because the adapter registers the
Jackson module; custom serializers, `@JsonCreator` factories and polymorphic
types are the usual sources of `No serializer found` or
`Cannot construct instance` errors in native mode.

## Testing the native binary

Run the same smoke checks as the release pipeline:

```bash
# Initial HTML
curl -s http://localhost:8080/ | grep -q 'id="app"'

# Inertia JSON
curl -s -H 'X-Inertia: true' -H 'X-Inertia-Version: x' \
  http://localhost:8080/ | head -c 200

# Health (Quarkus)
curl -fsS http://localhost:8080/q/health/ready

# Health (Spring Boot Actuator)
curl -fsS http://localhost:8080/actuator/health
```

The response for a stale version is `409`, not a page; use the current version
from a first JSON response when scripting assertions.

## Known limitations

- Native builds take roughly 10–20 minutes; the frontend build runs inside the
  builder image and needs network access.
- The SSR sidecar is a separate Node.js process. It is **not** part of the
  native binary; containerize it independently and point `inertia.ssr-url` at
  it. The adapter falls back to CSR if it is unavailable.
- `docker build` resolves the adapter from Maven Central, so native images
  require a published adapter release. Building from a local snapshot needs a
  mirror or `mvn install` before the Docker build.
- Reflection-heavy libraries (some templating engines, reporting tools) may
  need additional hints. Test the exact endpoints you ship.
- Native images are platform-specific: build on the target architecture or use
  a multi-arch builder.
- Heap and thread defaults differ from the JVM; measure and set
  `-Xmx`/`-XX:MaximumHeapSizePercent` for your workload.

## Container notes

Both Dockerfiles use UBI Micro and run as UID `1001`:

```dockerfile
FROM registry.access.redhat.com/ubi9/ubi-micro:latest
WORKDIR /app
COPY --from=builder --chown=1001:root /app-root /app
USER 1001
ENTRYPOINT ["./hello-inertia"]
```

The Spring image copies `libz.so.1` explicitly because the native binary links
against it and UBI Micro does not include it. The Quarkus image does the same.

Do not copy the whole `target/` directory into the runtime image: it includes
JARs, test reports and source artifacts that are useless at runtime and enlarge
the attack surface.

## When native is worth it

| Workload | Recommendation |
|---|---|
| Serverless / scale-to-zero | Strong fit: fast cold start and low memory |
| CLI or short-lived process | Strong fit |
| Long-running monolith with heavy JIT warmup | Measure; JVM may be faster under sustained load |
| SSR-heavy application | Native Java + separate Node sidecar; measure end-to-end |
| Native + reflection-heavy stack | Budget time for hints and smoke tests |

Use JMH results and production traces, not intuition. Native startup wins are
easy to measure; throughput differences depend on the workload.

## Checklist

- [ ] The starter was generated with version `0.0.6`.
- [ ] Frontend builds inside the builder image (network available).
- [ ] `-Pnative` completes and the binary boots.
- [ ] Initial HTML and Inertia JSON are asserted over HTTP.
- [ ] Custom DTOs have reflection hints.
- [ ] The SSR sidecar is containerized separately, if used.
- [ ] The runtime image runs as a non-root user.
- [ ] The image is built for the deployment architecture.
- [ ] Heap and thread limits are set from measurements.

Next: [Performance](/guide/performance) · [SSR](/guide/ssr) ·
[Migration](/reference/migration)
