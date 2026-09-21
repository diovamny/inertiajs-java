# Auditoría Fase 0 — Seguridad y Corrección de Bugs Críticos

**Rama:** `fase-0-seguridad-critica`
**Fecha:** 2026-09-20
**Alcance:** Solo Fase 0 (G-17, G-03, G-02). Sin cambios de Fases 1–5.

## 1. Veredicto

**Fase 0: 100% completa.** Los 3 gaps están cerrados con regresión verde:

| Gap | Estado | Evidencia |
|---|---|---|
| G-17 Fuga de contexto `InertiaVertxHandler` | ✅ Cerrado | `InertiaContextLocals` + dual-write rc-first + `addEndHandler` cleanup; todos los lectores de headers/flags migrados |
| G-03 `Vary` sobrescribe | ✅ Cerrado | `VaryHeaderUtil` en ambos módulos; filtros/decoradores aditivos; builders con valor canónico |
| G-02 Fixtures XSS adversariales | ✅ Cerrado | `SafeJsonEncoderTest` 7→13 casos en ambos módulos |

## 2. Regresión ejecutada

| Suite | Resultado |
|---|---|
| Spring `spring-inertia` | **133/133 ✅** (121 base + 6 XSS + 6 Vary) |
| Quarkus `quarkus-inertia` | **274/274 ✅** (258 base + 6 XSS + 6 Vary + 4 ContextLocals) |
| Spring demos (`spring-pingcrm`, `spring-kitchen-sink`) | **SUCCESS ✅** (kitchen-sink 24/24) |
| Quarkus demos (`pingcrm` 25/25, `kitchen-sink` 9/9) | **SUCCESS ✅** |
| `node scripts/check-conformance-matrix.mjs` | **OK ✅** (83 referencias resueltas) |
| Compilación `spring-inertia,quarkus-inertia` | **BUILD SUCCESS ✅** |

Nota: la suite Quarkus completa tarda ~90s (múltiples `@QuarkusTest` con arranque);
una ejecución inicial superó el timeout de 15 min del runner por lentitud, no por
fallos — re-ejecutada con 30 min: 274/274 verde. Sin fallos ni errores en ningún
reporte `surefire`.

## 3. Cambios por gap

### G-17 — Aislamiento por request (crítico)
- Nuevo `quarkus/.../protocol/InertiaContextLocals.java`: `get/put` con preferencia
  a `RoutingContext`, espejo a `Vert.x Context` para `@Blocking`, limpieza de claves
  opcionales (evita herencia entre requests del mismo event-loop) y `addEndHandler`.
- `InertiaVertxHandler.handle()`: bindea `rc` antes de extraer, usa overload
  `extract(rc, ctx, ...)`, CSRF escribe en ambos, `handleFailure`/`isInertia` con
  lectura rc-first.
- `InertiaRequestFilter`: bindeo temprano + `InertiaContextLocals.put`.
- Migrados a lectura rc-first: `ResponseProcessor`, `RedirectProcessor`,
  `PageObjectBuilder` (partial/version/errors/history), `InertiaResponseDecorator`,
  `InertiaResponseFilter`, `JsonResponseProcessor`, `PrecognitionExceptionMapper`,
  `ErrorResponseFactory`, `OncePropRegistry`, `Inertia(In)ValidationExceptionMapper`,
  `InertiaImpl` (headers/view-data/history/root-view/ssr/page-status),
  `SsrHandler`, `HtmlRenderer`, `DefaultVersionProvider`, `InertiaCsrfFilter`,
  `ReactiveResponseWriter`.
- Puentes `inertia-routing-context` conservados como lookup directo (resolución de
  `rc`, no lectura de header).
- Nuevo `InertiaContextLocalsTest` (4 tests): preferencia rc, fallback ctx,
  espejo dual, concurrencia 20×100 sin contaminación.

### G-03 — `Vary` aditivo (paridad Rails)
- Nuevos `spring/.../util/VaryHeaderUtil.java` y `quarkus/.../util/VaryHeaderUtil.java`:
  `merge` con split por coma, dedup case-insensitive, preserva orden; `addTo` para
  `HttpHeaders`, `HttpServletResponse`, Vert.x `MultiMap`, JAX-RS `MultivaluedMap`.
- Spring: `InertiaFilter` (3 sitios), `ResponseProcessor` (4), `PrecognitionHandler` (1).
- Quarkus: `InertiaResponseFilter` (Vary Precognition + X-Inertia aditivos),
  `InertiaResponseDecorator` (ambos), resto de builders con valor canónico vía util
  donde aplica.
- Nuevos `VaryHeaderUtilTest` (6 tests por módulo) + aserciones existentes
  (`Vary contains X-Inertia/Precognition`) siguen verdes.

### G-02 — XSS adversarial
- `SafeJsonEncoder` ya escapaba `< > & / U+2028/29`; el gap era falta de fixtures.
- Añadidos 6 casos por módulo: breakout `</script>`, comentarios `<!--`,
  `<svg/onload>`/`<img onerror>`, surrogate pairs/emoji, payload con comillas,
  `null` → `""`.

## 4. Bugs hallados durante la regresión (corregidos + re-verificados)
1. `ReactiveResponseWriter`: variable `rc` reasignada y capturada en lambda
   (error `effectively final`) — corregido con `final routingContext`.
2. `InertiaResponseDecorator`: uso de `locals.get(...)` sobre `Class` en vez de
   llamada estática — corregido a llamadas estáticas directas.
3. Intento de parcheo masivo con PowerShell corrompió 3 ficheros (BOM + `\` literal
   por comillas simples) — revertidos con `git checkout` y re-aplicados con `Edit`
   (compilación verde posterior).

## 5. Límites declarados (no rompen el 100% de Fase 0)
- `OncePropRegistry`/`SharedDataRegistry` conservan fallback de testing
  (`fallback`/`this`) fuera de request — comportamiento previo, sin cambios.
- Builders JAX-RS (`ResponseBuilder.header("Vary", ...)`) crean respuestas nuevas;
  el valor es canónico. La aditividad estricta aplica a filtros/decoradores que
  mutan respuestas existentes (ya corregidos).
- E2E Playwright no ejecutado en esta máquina (requiere Node/Chromium + demos en
  marcha); CI lo cubre tras `examples`. Demos smoke sí ejecutados.

## 6. Ficheros nuevos
- `spring-inertia/.../util/VaryHeaderUtil.java`
- `quarkus-inertia/.../util/VaryHeaderUtil.java`
- `quarkus-inertia/.../protocol/InertiaContextLocals.java`
- `spring-inertia/.../util/VaryHeaderUtilTest.java`
- `quarkus-inertia/.../util/VaryHeaderUtilTest.java`
- `quarkus-inertia/.../protocol/InertiaContextLocalsTest.java`
- Este `AUDIT_FASE_0.md`
