# Inertia v3 Conformance Matrix

**Date:** 2026-09-23
**Scope:** Spring Boot Inertia.js v3 Adapter & Quarkus Inertia.js v3 Adapter
**Reference:** [Inertia v3 Protocol](https://inertiajs.com/docs/v3/core-concepts/the-protocol)
**Source of truth:** [`specs/inertia-v3-compliance.yaml`](../specs/inertia-v3-compliance.yaml) + [`specs/e2e-compliance.yaml`](../specs/e2e-compliance.yaml) — this file is GENERATED, do not edit by hand (see `scripts/generate-compatibility-matrix.mjs`).

[![C100 contract](https://img.shields.io/badge/C100-61%2F62%20contract--verified-brightgreen)](../specs/inertia-v3-compliance.yaml)
[![I100 interop](https://img.shields.io/badge/I100-57%2F90%20cells-green)](../specs/e2e-compliance.yaml)
**C100 contract: 61/62 TCK-verified. I100 interop: 57/90 E2E cells green with official clients.**

Status semantics (contract vs interop are separate): ✅ = normative test green on all 3 transports (counts for C100 only); 🧪 = officially observed by a real client in the listed transports (partial I100 evidence); 🔄 = code exists with indirect coverage and a tracked test; ❌ = known but untested; `N/A` = not applicable. A contract ✅ never counts as interop.

Important: this file is now evidence-based and generated. Claims of 100% compatibility are deliberately avoided until C100, I100 and the quality gates are all green.

| # | Category | Requirement | Source | Status | Spring | Quarkus | E2E (official clients) | Test | Notes |
|---|----------|-------------|--------|--------|--------|---------|------------------------|------|-------|
| 1 | Detection | X-Inertia header present on Inertia visits | https://inertiajs.com/docs/v3/core-concepts/the-protocol | TESTED | ✅ | ✅ | — | InertiaHeaderExtractorTest, ReactiveRouteInertiaTest | Both set X-Inertia:true in response |
| 2 | Detection | X-Inertia-Version header present | Protocol | TESTED | ✅ | ✅ | — | FeatureMetadataIntegrationTest, InertiaRedirectQuarkusTest | Version included in page responses |
| 3 | Detection | HTTP method detected from request | Protocol | TESTED | ✅ | ✅ | — | InertiaHeaderExtractorTest, InertiaResponseFilterUnitTest | GET/POST/PUT/PATCH/DELETE |
| 4 | Detection | Partial reload headers parsed | Protocol | TESTED | ✅ | ✅ | — | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | X-Inertia-Partial-Component/Data/Except/Reset |
| 5 | Response | X-Inertia:true header on JSON responses | Protocol | TESTED (+E2E spring-mvc, quarkus-rest) | ✅ | ✅ | 🧪 spring-mvc, quarkus-rest | RenderPageIntegrationTest, ReactiveRouteInertiaTest | JSON page responses include header; Playwright e2e on Spring/Quarkus demos |
| 6 | Response | Component, props, URL, version in JSON | Protocol | TESTED (+E2E spring-mvc, quarkus-rest) | ✅ | ✅ | 🧪 spring-mvc, quarkus-rest | InertiaPageTest (both), PageObjectUnitTest (core) | Full page object serialized; Playwright e2e asserts component/url/version/props |
| 7 | Response | clearHistory boolean in page object | Protocol | TESTED | ✅ | ✅ | — | PageObjectUnitTest (core), InertiaPageTest | Configurable via properties and API |
| 8 | Response | encryptHistory boolean in page object | Protocol | TESTED | ✅ | ✅ | — | PageObjectUnitTest (core), InertiaPageTest | Configurable via properties and API |
| 9 | Response | preserveFragment boolean in page object | Protocol | TESTED | ✅ | ✅ | — | InertiaPageTest (both) | Inertia.preserveFragment() API |
| 10 | Version | Asset version match returns normal response | Protocol | TESTED | ✅ | ✅ | — | RenderPageIntegrationTest, ReactiveRouteInertiaTest | Same version -> 200 JSON |
| 11 | Version | Version mismatch returns 409 + X-Inertia-Location | Protocol | TESTED (+E2E spring-mvc, quarkus-rest) | ✅ | ✅ | 🧪 spring-mvc, quarkus-rest | FeatureMetadataIntegrationTest (both) | Different version -> 409; Playwright e2e asserts 409 + x-inertia-location |
| 12 | Version | X-Inertia-Version sent in 409 response | Protocol | TESTED | ✅ | ✅ | — | FeatureMetadataIntegrationTest (both) | Header included |
| 13 | Version | Vary header includes version | Protocol | TESTED | ✅ | ✅ | — | RedirectProcessorTest, InertiaResponseFilterUnitTest | X-Inertia-Version in Vary |
| 14 | Version | Configurable version strategy | Protocol | TESTED | ✅ | ✅ | — | DefaultVersionProviderUnitTest (Quarkus), FeatureMetadataIntegrationTest (Spring) | sha256, vite-manifest, custom |
| 15 | Redirect | GET redirect -> 302 | Protocol | TESTED | ✅ | ✅ | — | RedirectProcessorTest (both) | Standard redirect |
| 16 | Redirect | POST redirect -> 303 See Other | Protocol | TESTED | ✅ | ✅ | — | RedirectProcessorTest, InertiaRedirectQuarkusTest | State-changing -> 303 |
| 17 | Redirect | External URL -> 409 + X-Inertia-Location | Protocol | TESTED | ✅ | ✅ | — | RedirectProcessorTest, InertiaRedirectQuarkusTest | External detected |
| 18 | Redirect | Vary header preserved on redirects | Protocol | TESTED | ✅ | ✅ | — | RedirectProcessorTest, InertiaResponseFilterUnitTest | X-Inertia in Vary |
| 19 | Redirect | 303 only for redirect objects, not rendered pages | Protocol | TESTED | ✅ | ✅ | — | RedirectIntegrationTest, InertiaRedirectQuarkusTest | POST rendering page -> 200, redirect -> 303 |
| 20 | Partial | X-Inertia-Partial-Component header | Protocol | TESTED | ✅ | ✅ | — | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Component match detection |
| 21 | Partial | only (X-Inertia-Partial-Data) filters props | Protocol | TESTED (+E2E spring-mvc, quarkus-rest) | ✅ | ✅ | 🧪 spring-mvc, quarkus-rest | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Dot notation supported; Playwright e2e asserts partial-data filtering |
| 22 | Partial | except (X-Inertia-Partial-Except) excludes props | Protocol | TESTED | ✅ | ✅ | — | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Dot notation supported |
| 23 | Partial | Structural dot notation filtering | Protocol | TESTED | ✅ | ✅ | — | PartialReloadProcessorTest (both) | auth.user -> selective nested |
| 24 | Partial | Reset clears merge metadata | Protocol | TESTED | ✅ | ✅ | — | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Resets merge/prepend/deepMerge |
| 25 | Special | always props survive partial reloads | Protocol | TESTED | ✅ | ✅ | — | RenderPageIntegrationTest, InertiaPageUnitTest | Always included |
| 26 | Special | optional resolved only on partial with only/except | Protocol | TESTED | ✅ | ✅ | — | OptionalPropsIntegrationTest, OptionalPropsUnitTest | Not on full visits |
| 27 | Special | deferred excluded until group requested | Protocol | TESTED | ✅ | ✅ | — | DeferredPropsIntegrationTest (Spring), OnceLazyShareOnceUnitTest (Quarkus) | Resolved on partial |
| 28 | Special | once props tracked via X-Inertia-Except-Once-Props header | Protocol | TESTED | ✅ | ✅ | — | OncePropRegistryTest (both), OncePropsIntegrationTest | Header-based, not session |
| 29 | Special | once explicit partial request overrides Except-Once | Protocol | TESTED | ✅ | ✅ | — | OncePropRegistryTest (Spring), OnceLazyShareOnceUnitTest (Quarkus) | only:notice -> always returns notice |
| 30 | Special | merge/prepend/deepMerge metadata | Protocol | TESTED | ✅ | ✅ | — | MergePropProcessorTest (both) | Client-side merge |
| 31 | Special | scroll props metadata | Protocol | TESTED | ✅ | ✅ | — | InertiaPageTest (both) | Scroll metadata preserved |
| 32 | Special | rescue marks failed deferred/cached props | Protocol | TESTED | ✅ | ✅ | — | DeferredPropsIntegrationTest, SharedDataRegistryUnitTest | rescuedProps metadata |
| 33 | State | clearHistory configurable per-request | Protocol | TESTED | ✅ | ✅ | — | Inertia.setClearHistory() (Spring), Inertia.clearHistory() (Quarkus) | API + properties |
| 34 | State | encryptHistory configurable per-request | Protocol | TESTED | ✅ | ✅ | — | Inertia.setEncryptHistory() (both) | API + properties |
| 35 | State | preserveFragment configurable per-request | Protocol | TESTED | ✅ | ✅ | — | Inertia.preserveFragment() (both) | API |
| 36 | CSRF | XSRF-TOKEN cookie on all responses | Protocol | TESTED | ✅ | ✅ | — | InertiaCsrfFilterTest (Spring), CsrfQuarkusTest (Quarkus) | Cookie emitted always, validated on mutating |
| 37 | CSRF | Validation only on mutating Inertia requests | Protocol | TESTED | ✅ | ✅ | — | InertiaCsrfFilterTest (Spring), CsrfQuarkusTest (Quarkus) | POST/PUT/PATCH/DELETE with X-Inertia |
| 38 | CSRF | 303 + flash on missing/invalid token (Inertia visits) | Protocol | TESTED | ✅ | ✅ | — | CsrfIntegrationTest (Spring), CsrfQuarkusTest (Quarkus) | 303 + Location + flash on Inertia visits in both adapters; 419 only for non-Inertia requests in the Quarkus reactive pre-handler (Spring adapter passes non-Inertia through); never X-Inertia-Location on CSRF |
| 39 | HTML | Safe JSON encoding in script context | Protocol | TESTED | ✅ | ✅ | — | SafeJsonEncoderTest (both) | Escapes <, >, &, U+2028, U+2029 |
| 40 | HTML | SSR timeout configurable | Protocol | TESTED | ✅ | ✅ | — | HtmlRendererTest, SsrHandlerUnitTest | ssrConnectTimeout + ssrReadTimeout |
| 41 | HTML | Error details hidden by default | Protocol | TESTED | ✅ | ✅ | — | ErrorResponseFactoryUnitTest (Quarkus) | errorDetailsEnabled=false default |
| 42 | HTML | SSR error does not leak response body | Protocol | TESTED | ✅ | ✅ | — | SsrHandlerUnitTest (Quarkus) | Returns fallback on error |
| 43 | HTTP | Vary header on JSON responses | Protocol | TESTED | ✅ | ✅ | — | RenderPageIntegrationTest, InertiaResponseFilterUnitTest | X-Inertia, Version, Partial-*, Except |
| 44 | HTTP | ETag support with If-None-Match | Protocol | TESTED | ✅ | ✅ | — | EtagQuarkusTest | Supports wildcard, comma lists |
| 45 | HTTP | Default status 200 when none specified | Protocol | TESTED | ✅ | ✅ | — | RenderPageIntegrationTest, InertiaRenderQuarkusTest | Null-safe default |
| 46 | Shared | Shared props included on full visits | Protocol | TESTED | ✅ | ✅ | — | RenderPageIntegrationTest, InertiaPageUnitTest | putIfAbsent semantics |
| 47 | Shared | Shared props included as base in partial reloads | Protocol | TESTED | ✅ | ✅ | — | SharedPropsIntegrationTest, SharedDataRegistryUnitTest, TckSpringTest, TckQuarkusTest, TckReactiveTest | Laravel parity: only always props survive the only/except filter; plain shared props are filtered (shared-props-survive-partial-base) |
| 48 | Asset | Version strategy: custom > vite-manifest > sha256 > fallback | Protocol | TESTED | ✅ | ✅ | — | FeatureMetadataIntegrationTest, DefaultVersionProviderUnitTest | Deterministic precedence |
| 49 | Asset | SHA-256 hash of static assets | Protocol | TESTED | ✅ | ✅ | — | DefaultVersionProviderUnitTest, FeatureMetadataIntegrationTest | Path + content hash |
| 50 | Asset | JAR resource enumeration | Protocol | TESTED | ✅ | ✅ | — | DefaultVersionProviderUnitTest, FeatureMetadataIntegrationTest | ZIP entry fallback |
| 51 | TCK | Handshake, partial only/except, deferred, once | 01-handshake.yaml, 02-partial.yaml, 03-deferred-once.yaml | TESTED | ✅ | ✅ | — | TckSpringTest, TckQuarkusTest, TckReactiveTest | inertia-tck, same cases on 3 stacks |
| 52 | TCK | Redirects 302/303/409, versioning, adapter CSRF, validation | 04-redirects.yaml, 05-version.yaml, 06-csrf.yaml, 07-validation.yaml | TESTED | ✅ | ✅ | — | TckSpringTest, TckQuarkusTest, TckReactiveTest, ReactiveStressTest | 43 normative cases; reactive runs 42 path-mapped + dedicated staleVersionIs409WithReactivePath (same 409 semantics, transport-local path); M7 concurrency isolation green (see docs/reactive-parity.md) |
| 53 | Validation | X-Inertia-Error-Bag default + named bags, one message per field | https://inertiajs.com/docs/v3/the-basics/validation | TESTED | ✅ | ✅ | — | TckSpringTest, TckQuarkusTest, TckReactiveTest, ValidationQuarkusTest, ValidationIntegrationTest | Default + named bags via 07-validation.yaml on 3 transports (redirect 303 + nested flash); wire shape Map<field,message> (see PROTO-053B for multi-message) |
| 54 | Validation | Multiple messages per field (array of messages, order preserved) | https://inertiajs.com/docs/v3/the-basics/validation | TESTED (+E2E spring-mvc, quarkus-rest) | ✅ | ✅ | 🧪 spring-mvc, quarkus-rest | TckSpringTest, TckQuarkusTest, TckReactiveTest, ValidationErrorsUnitTest, ValidationErrorsApiUnitTest | M2 wire on 3 transports (TCK); Vue-observed bags on Spring + Quarkus REST (Playwright validation + named-bag); remaining E2E cells land in M4b |
| 55 | Special | merge/prepend/deepMerge wire metadata + matchPropsOn | https://inertiajs.com/docs/v3/data-props/merging-props | TESTED | ✅ | ✅ | — | TckSpringTest, TckQuarkusTest, TckReactiveTest, MergePropProcessorTest (both) | TCK 08-merge.yaml green on 3 transports (metadata, reset, partial); scroll merge-intent covered (see PROTO-054B for explicit append API) |
| 56 | Special | Explicit append() API with nested routes (append/prepend per path) | https://inertiajs.com/docs/v3/data-props/merging-props | TESTED (+E2E spring-mvc, quarkus-rest) | ✅ | ✅ | 🧪 spring-mvc, quarkus-rest | TckSpringTest, TckQuarkusTest, TckReactiveTest, MergePlanUnitTest, MergeableBuilderUnitTest | M3 wire on 3 transports (TCK); Vue-observed append on Spring + Quarkus REST (Playwright badge 1-total to 2-total); remaining E2E cells land in M4b |
| 57 | Special | X-Inertia-Infinite-Scroll-Merge-Intent honored | https://inertiajs.com/docs/v3/data-loading/infinite-scrolling | TESTED | ✅ | ✅ | — | InertiaPageTest (both), InertiaHeaderExtractorTest | Header extracted; scrollProps metadata; dedicated infinite-scroll TCK in Fase 2 (H8) |
| 58 | Special | once key, fresh and expiry semantics | https://inertiajs.com/docs/v3/data-loading/once-props | TESTED | ✅ | ✅ | — | OncePropRegistryTest (both), OncePropsIntegrationTest, TckSpringTest, TckQuarkusTest, TckReactiveTest | customKey + expiry (Duration/Instant) + once+merge/optional/deferred combos via 03-deferred-once.yaml on 3 transports; fresh is server-internal with no wire effect |
| 59 | Shared | sharedProps field serialized in page object (server-side) | https://inertiajs.com/docs/v3/the-basics/instant-visits | TESTED | ✅ | ✅ | — | InertiaPageTest (both) | Explicit sharedProps field in PageObject, omitted when empty (see PROTO-057B for client-observed behavior) |
| 60 | Shared | Instant-visit sharedProps behavior observed by official client | https://inertiajs.com/docs/v3/the-basics/instant-visits | IMPLEMENTADO | 🔄 | 🔄 | 🧪 spring-mvc, quarkus-reactive | InertiaPageTest (both) | Vue-observed on Spring MVC and Reactive Routes (placeholder with shared+current props before server 2s response, then server greeting); no TCK applies (client behavior); full E2E_VERIFICADO needs all 9 cells in M4b |
| 61 | HTML | SSR failure uses structured error contract | Protocol | TESTED | ✅ | ✅ | — | SsrFailureClassifierTest, SsrHandlerUnitTest | Failures classify as unreachable/timeout/error-status/unknown with hints (server-side only, CSR fallback); sidecar-reported browserApi/sourceLocation travel in render payloads |
| 62 | HTML | Bootstrap v3 structural: payload once in script, div without data-page | https://inertiajs.com/docs/v3/core-concepts/the-protocol | TESTED (+E2E spring-mvc, quarkus-rest) | ✅ | ✅ | 🧪 spring-mvc, quarkus-rest | HtmlRendererTest, RenderPageIntegrationTest, TckSpringTest, TckQuarkusTest, TckReactiveTest | v3-pure (59% smaller bootstrap); legacy v1/v2 data-page attribute intentionally unsupported, no opt-in; Playwright e2e parses the script JSON on Spring/Quarkus demos (reactive cell lands in M4) |
