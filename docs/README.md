# Documentation Index

Start here and follow the guides in order. All pages are in English.

## Start

- [Getting started — Spring Boot](getting-started-spring.md) — starter kit or manual setup, first page, first test.
- [Getting started — Quarkus](getting-started-quarkus.md) — starter kit or manual setup, first page, first test.

## Reference

- [Configuration](configuration.md) — all 23 `inertia.*` properties with defaults and Spring/Quarkus availability.
- [Protocol compatibility](protocol-compatibility.md) — Inertia v3 requirement matrix, row-by-row linked to contract tests (CI-verified).
- [Migration](migration.md) — versioning policy and upgrading adapters/starters.

## Features

- [Shared data and props](shared-data-and-props.md) — contributor SPI and every prop strategy (shared, optional, deferred, once, merge, scroll).
- [View data](viewdata-guide.md) — root-template data injection and placeholders.
- [Testing](testing-guide.md) — MockMvc (Spring) and REST-assured (Quarkus) with the `InertiaPage` DSL.
- [SSR setup](ssr-setup.md) — Node.js sidecar, config and fallback behavior.
- [Native image](native-image.md) — GraalVM builds, Dockerfiles and known limitations.

## Design decisions

- [ADR-001: native builder image](adr/001-native-builder-jdk-25.md)
- [ADR-002: null props on partial reloads](adr/002-partial-null-props.md)

## External

- [Inertia.js protocol (v3)](https://inertiajs.com/docs/v3/core-concepts/the-protocol)
- [Project roadmap](../ROADMAP.md) · [Changelog](../CHANGELOG.md) · [Contributing](../CONTRIBUTING.md)
