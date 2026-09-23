# ADR 003 — Multi-message validation without `Map` overloads (P100-04)

- Status: accepted (release-0.0.5, M2).
- Context: Inertia `withAllErrors` allows several messages per field, but the
  Java APIs only accepted `Map<String, String>` and the mappers kept one
  message via `putIfAbsent`/`put`. Java erasure forbids overloading
  `withErrors(Map<String, String>)` with `withErrors(Map<String, List<String>>)`
  (same erasure `Map`), so a naive overload does not compile.
- Decision: introduce the immutable ordered core value `ValidationErrors`
  (framework-free) plus unambiguous entry points
  `withValidationErrors(ValidationErrors)` and
  `withErrorMessages(Map<String, ? extends Collection<String>>)` on
  `InertiaRedirect`/`InertiaRender`/`InertiaResponse` (both adapters);
  existing `withErrors(Map<String, String>)` keeps the legacy wire.
  New flag `inertia.validation.all-errors` (default `false`): `false`
  emits `Map<field, firstMessage>`, `true` emits ordered
  `Map<field, List<message>>` in flash, bags and Precognition 422.
  Spring wires the flag via an optional setter (keeps the `@Bean` method
  binary-compatible, verified by `japicmp` MINOR); Quarkus via
  `InertiaConfig.validationAllErrors()`.
- Consequences: additive API only (`japicmp` 0.0.4→0.0.5 MINOR on all
  modules); Quarkus manual `InertiaConfig` implementations should add the
  method with a `default` (source-level only).
