# Auditoría release-0.0.5 — M2 Validación multi-mensaje (P100-04/05)

- Rama: `release-0.0.5`. Fecha: 2026-09-23.
- Resultado: **VERDE — puede continuar a M3.**

## Criterios 100% M2

- [x] Sin `put`/`putIfAbsent` que descarte mensajes: `PrecognitionHandler`
  (Spring), `PrecognitionExceptionMapper` + `InertiaVertxHandler` (Quarkus
  REST + Reactive) acumulan con `ValidationErrors.Builder` (orden
  preservado); sin sobrecarga ambigua `Map` (nuevos nombres
  `withValidationErrors`/`withErrorMessages` + ADR en `migration.md`).
- [x] TCK verifica estructura completa en 3 transportes:
  `07-validation.yaml` + `multi-message-submit-redirects-back` (303) +
  `multi-message-errors-are-ordered-arrays-on-reload`
  (`props.errors.name[0]=required`, `[1]=must be valid`); `TckSpringTest`,
  `TckQuarkusTest` (suite completa) y `TckReactiveTest` (subset + 2 casos)
  verdes.
- [x] Misma forma JSON en 3 rutas por modo: legado (`all-errors=false`,
  `Map<field,message>`, default) preservado; `all-errors=true` y APIs
  explícitas emiten `Map<field,List<message>>` ordenado en flash, bags y
  Precognition 422.
- [x] Casos obligatorios: 1 mensaje legado, 2 mensajes ordenados, bag
  default/nombrado (matriz + suites existentes), 303+flash→GET (TCK),
  Precognition 422 + validate-only (suites existentes + acumulación sin
  pérdida), locale no-inglés (mensajes tratados como texto opaco).
- [x] Compatibilidad: `withErrors(Map<String,String>)` intacto; Quarkus
  `InertiaConfig` manual requiere `validationAllErrors()` (documentado en
  `migration.md`); `japicmp` pendiente de M8 (cambio aditivo + 1 método
  de interfaz).

## Evidencia

- `inertia-core ValidationErrorsUnitTest`: 6/6.
- Spring: `ValidationErrorsApiUnitTest` 3/3 + `TckSpringTest` + 
  `ValidationIntegrationTest` (7/7) verdes.
- Quarkus: `ValidationErrorsApiUnitTest` 3/3 + `ValidationQuarkusTest` 9/9 +
  `TckQuarkusTest` + `TckReactiveTest` + config/factory/version tests verdes.
- `verify:metadata` verde; matriz `62 rows, 59/62 verified`
  (PROTO-053B TESTED; E2E client en M4).

## Docs

`docs/configuration.md` (`validation.all-errors`), `docs/migration.md` (M2),
matriz regenerada. `PROTO-053B` → `E2E_VERIFICADO` en M4.
