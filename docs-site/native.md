# Native Image

GraalVM smoke tests run on every release for Spring and Quarkus starters:
generate the archetype, `package -Pnative`, boot the binary, assert HTML +
Inertia JSON over HTTP. Library hints (`RuntimeHints`, reflection config)
ship in the adapters.
