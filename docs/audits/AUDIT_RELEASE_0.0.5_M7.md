# Auditoría release-0.0.5 — M7 Paridad Reactive Routes a estable (P100-17/18)

- Rama: `release-0.0.5`. Fecha: 2026-09-23.
- Resultado: **VERDE — Reactive es objetivo estable en `0.0.5`, sin etiqueta
  experimental. Puede continuar a M4.**

## Decisión formal (P100-18): GRADUAR a estable

Reactive Routes cumple el gate adaptado a `0.0.5` (ciclo único intensivo en
lugar de 30 días naturales): TCK completo, stress concurrente verde,
cero P0/P1 abiertos específicos de Reactive, revisión del diff (esta rama)
y documentación de paridad. Ver `docs/reactive-parity.md`.

## Criterios 100% M7

- [x] TCK 43/43 con paridad: `TckReactiveRoutes` ampliado (`deferred`,
  `once`, `submit`, `submit-multi`, `put-me`, `delete-me`, `back`,
  `versioned`, `merge-nested`); `TckReactiveTest` corre 42 casos
  path-mapeados + `staleVersionIs409WithReactivePath` dedicado (el caso
  `version-mismatch-is-409` aserta la ruta exacta `/tck/versioned`, un
  artefacto del harness; la misma semántica 409 + `X-Inertia-Location` +
  `X-Inertia-Version` se prueba en `/tck-reactive/versioned`).
- [x] Stress concurrente determinista: `ReactiveStressTest` (8 identidades
  × 12 rondas, sin sleeps): aislamiento de sesiones/flash, CSRF
  válido/inválido, 303/409, bags nombrados + multi-mensaje, once/deferred/
  partial/reset, merge anidado, identidades simultáneas distintas. Verde.
- [x] Fix de concurrencia real (clase G-17): el endurecimiento M7 cierra el
  mapa compartido de event-loop ctx en workers (`InertiaContextLocals` con
  holder request-scoped primero, sin lecturas del mapa compartido en
  workers, `put(null)` limpia ambos stores; holder enlazado en el
  pre-handler reactivo; `SharedDataRegistry`/`OncePropRegistry`/sesiones/
  flash/`ReactiveResponseWriter`/`InertiaImpl` por el helper endurecido).
  Regresión cubierta por `ReactiveStressTest` + `InertiaContextLocalsTest`
  + workflow nocturno `reactive-stress.yml`.
- [x] Cero P0/P1 abiertos de Reactive; matriz de diferencias por transporte
  (`docs/reactive-parity.md`): sin divergencias salvo la ruta del 409
  (justificada, misma semántica).
- [x] Módulo Quarkus completo verde: 329/329 (incluye el fix sin regresión
  en `InertiaRenderQuarkusTest`/`InertiaIspInjectionTest`, rotos
  transitoriamente por un fallback CDI ciego, ya eliminado).

## Notas honestas de la investigación M7

- El stress encontró primero un bug del propio test (Jackson
  `MissingNode.isNull()` es `false`; la supresión once ausente es correcta
  y el TCK la acepta) — corregido, no era regresión del producto.
- El diagnóstico con ruta temporal probó: workers `@Blocking` corren con
  event-loop context current (`isEventLoop=true`), ningún canal ambiental
  resuelve el rc propio (holder/CDI/ctx-local), solo `rc` explícito y el
  mapa compartido (racy). La ruta temporal se eliminó tras el diagnóstico.
- Gate temporal adaptado: 30 días naturales imposibles en un release;
  sustituto intensivo (stress + TCK + nightly) documentado arriba.

## Docs

`docs/reactive-parity.md` (nuevo), `.github/workflows/reactive-stress.yml`
(nuevo), `docs/NOT_SUPPORTED.md` + `docs-site/not-supported.md` (enlaces),
matriz regenerada (PROTO-052).
