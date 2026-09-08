# Inertia v3 Conformance Matrix

**Date:** 2026-08-23
**Scope:** Spring Boot Inertia.js v3 Adapter & Quarkus Inertia.js v3 Adapter
**Reference:** [Inertia v3 Protocol](https://inertiajs.com/docs/v3/core-concepts/the-protocol)

Status semantics: 🟩 means an executable test exists and is green in the current suite; `PENDING` means the requirement is known but not yet backed by a real test; `N/A` means not applicable.

Important: this file is now evidence-based. Rows that cite non-existent test classes are removed or marked as `PENDING`. Claims of 100% compatibility are deliberately avoided until the E2E matrix and quality gates are green.

| # | Category | Requirement | Source | Spring | Quarkus | Test | Notes |
|---|----------|-------------|--------|--------|---------|------|-------|
| **Detection** | | | | | | | |
| 1 | Detection | X-Inertia header present on Inertia visits | [Protocol](https://inertiajs.com/docs/v3/core-concepts/the-protocol) | 🟩 | 🟩 | InertiaHeaderExtractorTest, ReactiveRouteInertiaTest | Both set X-Inertia:true in response |
| 2 | Detection | X-Inertia-Version header present | Protocol | 🟩 | 🟩 | FeatureMetadataIntegrationTest, InertiaRedirectQuarkusTest | Version included in page responses |
| 3 | Detection | HTTP method detected from request | Protocol | 🟩 | 🟩 | InertiaHeaderExtractorTest, InertiaResponseFilterUnitTest | GET/POST/PUT/PATCH/DELETE |
| 4 | Detection | Partial reload headers parsed | Protocol | 🟩 | 🟩 | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | X-Inertia-Partial-Component/Data/Except/Reset |
| **Page Response** | | | | | | | |
| 5 | Response | X-Inertia:true header on JSON responses | Protocol | 🟩 | 🟩 | RenderPageIntegrationTest, ReactiveRouteInertiaTest | JSON page responses include header |
| 6 | Response | Component, props, URL, version in JSON | Protocol | 🟩 | 🟩 | InertiaPageTest (both), PageObjectTest | Full page object serialized |
| 7 | Response | clearHistory boolean in page object | Protocol | 🟩 | 🟩 | PageObjectTest (both), InertiaPageTest | Configurable via properties and API |
| 8 | Response | encryptHistory boolean in page object | Protocol | 🟩 | 🟩 | PageObjectTest (both), InertiaPageTest | Configurable via properties and API |
| 9 | Response | preserveFragment boolean in page object | Protocol | 🟩 | 🟩 | InertiaPageTest (both) | Inertia.preserveFragment() API |
| **Versioning** | | | | | | | |
| 10 | Version | Asset version match returns normal response | Protocol | 🟩 | 🟩 | RenderPageIntegrationTest, ReactiveRouteInertiaTest | Same version -> 200 JSON |
| 11 | Version | Version mismatch returns 409 + X-Inertia-Location | Protocol | 🟩 | 🟩 | FeatureMetadataIntegrationTest (both) | Different version -> 409 |
| 12 | Version | X-Inertia-Version sent in 409 response | Protocol | 🟩 | 🟩 | FeatureMetadataIntegrationTest (both) | Header included |
| 13 | Version | Vary header includes version | Protocol | 🟩 | 🟩 | RedirectProcessorTest, InertiaResponseFilterUnitTest | X-Inertia-Version in Vary |
| 14 | Version | Configurable version strategy | Protocol | 🟩 | 🟩 | DefaultVersionProviderUnitTest (Quarkus), FeatureMetadataIntegrationTest (Spring) | sha256, vite-manifest, custom |
| **Redirects** | | | | | | | |
| 15 | Redirect | GET redirect -> 302 | Protocol | 🟩 | 🟩 | RedirectProcessorTest (both) | Standard redirect |
| 16 | Redirect | POST redirect -> 303 See Other | Protocol | 🟩 | 🟩 | RedirectProcessorTest, InertiaRedirectQuarkusTest | State-changing -> 303 |
| 17 | Redirect | External URL -> 409 + X-Inertia-Location | Protocol | 🟩 | 🟩 | RedirectProcessorTest, InertiaRedirectQuarkusTest | External detected |
| 18 | Redirect | Vary header preserved on redirects | Protocol | 🟩 | 🟩 | RedirectProcessorTest, InertiaResponseFilterUnitTest | X-Inertia in Vary |
| 19 | Redirect | 303 only for redirect objects, not rendered pages | Protocol | 🟩 | 🟩 | RedirectIntegrationTest, InertiaRedirectQuarkusTest | POST rendering page -> 200, redirect -> 303 |
| **Partial Reloads** | | | | | | | |
| 20 | Partial | X-Inertia-Partial-Component header | Protocol | 🟩 | 🟩 | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Component match detection |
| 21 | Partial | only (X-Inertia-Partial-Data) filters props | Protocol | 🟩 | 🟩 | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Dot notation supported |
| 22 | Partial | except (X-Inertia-Partial-Except) excludes props | Protocol | 🟩 | 🟩 | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Dot notation supported |
| 23 | Partial | Structural dot notation filtering | Protocol | 🟩 | 🟩 | PartialReloadProcessorTest (both) | auth.user -> selective nested |
| 24 | Partial | Reset clears merge metadata | Protocol | 🟩 | 🟩 | PartialReloadProcessorTest, PartialReloadProcessorUnitTest | Resets merge/prepend/deepMerge |
| **Special Props** | | | | | | | |
| 25 | Special | always props survive partial reloads | Protocol | 🟩 | 🟩 | RenderPageIntegrationTest, InertiaPageUnitTest | Always included |
| 26 | Special | optional resolved only on partial with only/except | Protocol | 🟩 | 🟩 | OptionalPropsIntegrationTest, OptionalPropsUnitTest | Not on full visits |
| 27 | Special | deferred excluded until group requested | Protocol | 🟩 | 🟩 | DeferredPropsIntegrationTest (Spring), OnceLazyShareOnceUnitTest (Quarkus) | Resolved on partial |
| 28 | Special | once props tracked via X-Inertia-Except-Once-Props header | Protocol | 🟩 | 🟩 | OncePropRegistryTest (both), OncePropsIntegrationTest | Header-based, not session |
| 29 | Special | once explicit partial request overrides Except-Once | Protocol | 🟩 | 🟩 | OncePropRegistryTest (Spring), OnceLazyShareOnceUnitTest (Quarkus) | only:notice -> always returns notice |
| 30 | Special | merge/prepend/deepMerge metadata | Protocol | 🟩 | 🟩 | MergePropProcessorTest (both) | Client-side merge |
| 31 | Special | scroll props metadata | Protocol | 🟩 | 🟩 | InertiaPageTest (both) | Scroll metadata preserved |
| 32 | Special | rescue marks failed deferred/cached props | Protocol | 🟩 | 🟩 | DeferredPropsIntegrationTest, SharedDataRegistryUnitTest | rescuedProps metadata |
| **State & History** | | | | | | | |
| 33 | State | clearHistory configurable per-request | Protocol | 🟩 | 🟩 | Inertia.setClearHistory() (Spring), Inertia.clearHistory() (Quarkus) | API + properties |
| 34 | State | encryptHistory configurable per-request | Protocol | 🟩 | 🟩 | Inertia.setEncryptHistory() (both) | API + properties |
| 35 | State | preserveFragment configurable per-request | Protocol | 🟩 | 🟩 | Inertia.preserveFragment() (both) | API |
| **CSRF** | | | | | | | |
| 36 | CSRF | XSRF-TOKEN cookie on all responses | Protocol | 🟩 | 🟩 | InertiaCsrfFilterTest (Spring), CsrfQuarkusTest (Quarkus) | Cookie emitted always, validated on mutating |
| 37 | CSRF | Validation only on mutating Inertia requests | Protocol | 🟩 | 🟩 | InertiaCsrfFilterTest (Spring), CsrfQuarkusTest (Quarkus) | POST/PUT/PATCH/DELETE with X-Inertia |
| 38 | CSRF | 419 on missing/invalid token | Protocol | 🟩 | 🟩 | InertiaCsrfFilterTest (Spring), CsrfQuarkusTest (Quarkus) | Status 419 + X-Inertia-Location |
| **HTML/SSR** | | | | | | | |
| 39 | HTML | Safe JSON encoding in script context | Protocol | 🟩 | 🟩 | SafeJsonEncoderTest (both) | Escapes <, >, &, U+2028, U+2029 |
| 40 | HTML | SSR timeout configurable | Protocol | 🟩 | 🟩 | HtmlRendererTest, SsrHandlerUnitTest | ssrConnectTimeout + ssrReadTimeout |
| 41 | HTML | Error details hidden by default | Protocol | 🟩 | 🟩 | ErrorResponseFactoryUnitTest (Quarkus) | errorDetailsEnabled=false default |
| 42 | HTML | SSR error does not leak response body | Protocol | 🟩 | 🟩 | SsrHandlerUnitTest (Quarkus) | Returns fallback on error |
| **HTTP** | | | | | | | |
| 43 | HTTP | Vary header on JSON responses | Protocol | 🟩 | 🟩 | RenderPageIntegrationTest, InertiaResponseFilterUnitTest | X-Inertia, Version, Partial-*, Except |
| 44 | HTTP | ETag support with If-None-Match | Protocol | 🟩 | 🟩 | EtagQuarkusTest | Supports wildcard, comma lists |
| 45 | HTTP | Default status 200 when none specified | Protocol | 🟩 | 🟩 | RenderPageIntegrationTest, InertiaRenderQuarkusTest | Null-safe default |
| **Shared Props** | | | | | | | |
| 46 | Shared | Shared props included on full visits | Protocol | 🟩 | 🟩 | RenderPageIntegrationTest, InertiaPageUnitTest | putIfAbsent semantics |
| 47 | Shared | Shared props included as base in partial reloads | Protocol | 🟩 | 🟩 | SharedPropsIntegrationTest, SharedDataRegistryUnitTest | Survive partial filter |
| **Versioning (Asset)** | | | | | | | |
| 48 | Asset | Version strategy: custom > vite-manifest > sha256 > fallback | Protocol | 🟩 | 🟩 | FeatureMetadataIntegrationTest, DefaultVersionProviderUnitTest | Deterministic precedence |
| 49 | Asset | SHA-256 hash of static assets | Protocol | 🟩 | 🟩 | DefaultVersionProviderUnitTest, FeatureMetadataIntegrationTest | Path + content hash |
| 50 | Asset | JAR resource enumeration | Protocol | 🟩 | 🟩 | DefaultVersionProviderUnitTest, FeatureMetadataIntegrationTest | ZIP entry fallback |
