# Auditoría release-0.0.5 — M5 SSR remoto seguro (P100-10/11)

- Rama: `release-0.0.5`. Fecha: 2026-09-23.
- Resultado: **VERDE — puede continuar a M7.**

## Criterios 100% M5

- [x] URL remota insegura nunca arranca: core `SsrEndpointPolicy` (pura,
  sin frameworks) rechaza esquema no-http(s), host vacío, userinfo,
  fragmento, remoto sin `ssr-remote-enabled`, remoto sin `https`, remoto
  fuera de `ssr-allowed-hosts`; mensajes seguros (esquema/host/puerto,
  sin credenciales — probado).
- [x] Invocada al arranque en ambos frameworks cuando `ssr-enabled=true`:
  Spring `InertiaConfigValidator.validateSsrEndpoint`, Quarkus
  `InertiaConfigValidator.validateSsrEndpoint`; `ssr-enabled=false` omite
  la validación (sin ruptura para apps sin SSR).
- [x] Locales válidos sin config adicional: `localhost`/`127.0.0.1`/`::1`
  por HTTP pasan con flags por defecto (probado en ambos validadores).
- [x] Sin redirects a hosts no validados: Spring `SsrClient` desactiva
  `followRedirects` + convierte `3xx` en fallo (CSR fallback); Quarkus
  `SsrHandler` trata `>=300` como fallo (antes `>=400`); sin redirects
  automáticos en Vert.x.
- [x] Sin convertir caídas en 500: timeout, circuit breaker, límite de
  tamaño y fallback CSR intactos; sidecar controlado probado (válido,
  redirect→vacío, cuerpo inválido→vacío, caído→vacío).
- [x] Pruebas en Spring, Quarkus REST y Reactive: la política es común en
  `inertia-core`; el handler Reactive comparte `SsrHandler`/validador
  Quarkus (paridad M7 la re-verifica).

## Evidencia

- Core: `SsrEndpointPolicyTest` 5/5.
- Spring: `SsrEndpointValidatorTest` 4/4 + `SsrClientSidecarTest` 4/4 +
  `HtmlRendererTest` 5/5 verdes.
- Quarkus: `SsrEndpointValidatorTest` 4/4 + `SsrHandlerUnitTest` 4/4 +
  `SsrHandlerRenderUnitTest` 4/4 + `InertiaConfigUnitTest` 7/7 verdes.

## Docs

`docs/ssr-setup.md#trust-boundary` (postura local + opt-in remoto),
`docs/configuration.md` (`ssr-remote-enabled`, `ssr-allowed-hosts`),
`docs/migration.md` (M5). M4 verifica SSR real + fallback CSR con este
contrato.
