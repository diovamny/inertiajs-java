# ADR-001: Native builder image pinned to Mandrel jdk-25

- Date: 2026-09-07
- Status: accepted

## Context

Starter-kit `Dockerfile`s need a GraalVM builder image. The applications target
Java 21, for which a `jdk-21` Mandrel image exists.

## Decision

Use `quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25` for both
frameworks, with `ubi9/ubi-micro` runtime (UBI9 builder requires UBI9 runtime).

## Consequences

- Verified end-to-end: Quarkus and Spring starters compile to native images and
  serve HTML/JSON correctly.
- If a future Mandrel/JDK combination breaks the Java 21 bytecode build, fall
  back to the `jdk-21` builder tag.
