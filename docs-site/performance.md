# Performance

JMH (`benchmarks/`, profile `-Pbenchmarks`): page serialization + script
encoding throughput on this machine (1 fork, indicative):

| Payload | Size | Throughput |
|---|---|---:|
| small (10 props) | 258 B | 531.883 ops/s |
| medium (50 props) | 1,3 KB | 103.244 ops/s |
| large (1 MB) | 1.000.068 B | 171 ops/s |

Encoder overhead is constant (+2 B). Rerun with
`java -jar benchmarks/target/benchmarks.jar` and attach the CSV/HTML to
each release.
