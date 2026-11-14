# Performance

Inertia adds a thin serialization layer on top of the framework you already use.
The adapter does not query your database, render components or proxy requests;
its cost is building the page object and encoding it into the HTML. That hot
path is measured with JMH in `benchmarks/`.

## What the benchmark measures

`PageSerializationBenchmark` serializes a page-like map with Jackson and runs
`SafeJsonEncoder.encodeForScript`, the function that makes the JSON safe to embed
inside a `<script type="application/json">` element. Three payloads are used:

| Param | Shape |
|---|---|
| `small` | 10 string props |
| `medium` | 50 props, each a four-element array |
| `large` | one 1 MB string plus 50 string props |

Two benchmarks run per payload:

- `serializeAndEncode` — the real hot path (Jackson + script-context encoder).
- `rawLength` — baseline that only reads the pre-serialized string.

Comparing both separates serialization cost from encoder cost. The encoder is
linear and adds a constant two bytes per payload for the escaping wrapper.

## Running it

```bash
./mvnw -pl benchmarks -am package -DskipTests
java -jar benchmarks/target/benchmarks.jar
```

Useful variants:

```bash
# Only the real hot path
java -jar benchmarks/target/benchmarks.jar serializeAndEncode

# One payload size
java -jar benchmarks/target/benchmarks.jar -p size=medium

# More stable numbers before publishing a release
java -jar benchmarks/target/benchmarks.jar -f 2 -wi 3 -i 5
```

JMH writes a text summary to stdout. For release notes, keep the JSON or CSV
output next to the release artifact:

```bash
java -jar benchmarks/target/benchmarks.jar -rf json -rff target/jmh.json
```

## Reference numbers

Indicative results on a developer machine (1 fork, 1 warmup, 2 measurement
iterations — enough for regressions, not for a capacity plan):

| Payload | Raw size | Throughput |
|---|---|---:|
| small (10 props) | 258 B | ~531,000 ops/s |
| medium (50 props) | 1.3 KB | ~103,000 ops/s |
| large (1 MB) | 1,000,068 B | ~171 ops/s |

Read them as orders of magnitude, not absolute limits. Absolute throughput
depends on CPU, JDK, heap and the Jackson version. The shape is what matters:

- Small and medium pages serialize in microseconds. Serialization will not be
  the bottleneck of a normal CRUD page — the database query will.
- A 1 MB prop is expensive on every visit. At ~171 ops/s the encoder alone caps
  a single core; the browser also has to parse and hydrate that payload.
- The encoder overhead is constant, so it never changes the asymptotics.

## Where time actually goes

For a typical request:

| Stage | Typical cost | Notes |
|---|---|---|
| Routing + middleware | Framework | Unchanged by Inertia |
| Controller / queries | Your code | Usually dominant |
| Prop resolution | Your code | Deferred props remove it from first paint |
| Page serialization | Microseconds to milliseconds | Measured above |
| Root template render | Sub-millisecond | Cached by default |
| SSR sidecar (optional) | Milliseconds to tens of ms | Network + Node render |

Use the benchmark when changing the encoder, the JSON provider or the page
model. Use a profiler on the real application for everything else.

## Keeping pages small

The cheapest optimization is sending less data:

- Use [partial reloads](/guide/partial-reloads) so a visit only fetches the
  props it needs.
- Move slow props to [deferred](/guide/props#deferred-props); the first paint
  does not wait for them.
- Paginate collections. A page with 10,000 rows is a payload problem and a
  database problem.
- Select DTOs, not entities. Lazy-loading a graph during serialization is the
  classic hidden N+1.
- Avoid duplicating the same large list in shared data and in page props.
- Use [once props](/guide/props#once-props) for values that never change during
  a session.

## ETag and conditional requests

With `inertia.lazy-etag-enabled=true`, repeated partial reloads can answer
`304 Not Modified`, skipping the body entirely. This helps polling dashboards
and live-prop refreshes far more than micro-optimizing the encoder.

```properties
inertia.lazy-etag-enabled=true
```

The ETag is derived from the rendered page payload. A prop that changes on every
request (`Instant.now()`, a random value) defeats it — keep volatile values out
of the props you want cached.

## HTTP compression

Page JSON compresses well because it is repetitive. Enable gzip or Brotli at the
reverse proxy or in the framework:

```properties
# Spring Boot
server.compression.enabled=true
server.compression.mime-types=application/json,text/html,text/css,application/javascript
```

```properties
# Quarkus
quarkus.http.enable-compression=true
```

Compression trades CPU for bandwidth. For a 1 MB payload it is almost always a
win; for a 300-byte page it is noise.

## SSR cost

SSR adds a Node render per full page load. It improves first contentful paint
but costs server CPU and a network hop. Mitigations, in order:

1. Cache the rendered output with `inertia.ssr-cache-enabled=true` when the page
   is not personalized.
2. Keep the sidecar on localhost; remote SSR adds latency to every render.
3. Open the circuit breaker early (`ssr-breaker-failure-threshold`) so an
   unhealthy sidecar fails fast to CSR instead of timing out each request.
4. Never enable SSR for health checks, webhooks or JSON-only endpoints
   (`inertia.ssr-exclude-paths`).

See [SSR](/guide/ssr) for the full resilience model.

## Load testing

JMH measures one function in isolation. Before a release, exercise the real
application:

```bash
# k6 example: 50 virtual users for 2 minutes
k6 run --vus 50 --duration 2m load-test.js
```

Watch p95 and p99 latency, not only the average. For Inertia specifically:

- A full page load returns HTML and runs SSR; an Inertia visit returns JSON.
  Measure both, because they have different costs.
- A `409` version mismatch is cheap by design but produces a full reload; a
  spike in `409`s means the asset version changed more often than expected.
- Deferred props arrive as follow-up requests. They add load after the first
  paint, which is the point, but the database must still handle them.

## JVM notes

- `inertia.template-cache-enabled=true` (default) avoids re-reading the root
  template. Disable it only in development.
- Use a warm JVM for benchmarks; JIT compilation dominates the first seconds of
  a cold start.
- Native images start faster but are not automatically faster under sustained
  load. Measure both if that trade-off matters to you.
- Set the heap from measurements, not from a template. Large props are the main
  source of allocation pressure in the adapter.

## Checklist

- [ ] Page payloads stay in the kilobyte range for normal CRUD screens.
- [ ] Large collections are paginated.
- [ ] Slow props are deferred.
- [ ] ETags are enabled for polling pages.
- [ ] Compression is enabled for JSON and HTML.
- [ ] SSR is cached or excluded for personalized and health endpoints.
- [ ] p95/p99 measured for both HTML and JSON visits.
- [ ] Benchmark CSV attached to the release when the hot path changed.

Next: [Native Image](/guide/native-image) · [SSR](/guide/ssr) ·
[Configuration](/reference/configuration)
