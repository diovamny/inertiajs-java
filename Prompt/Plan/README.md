# Plan de Implementación — Adaptador Inertia.js v3 para Quarkus Reactivo

## Versión: 1.1
## Fecha: 2026-07-27

---

## Estructura del Plan

| Fase | Archivo | Descripción | Depende de |
|------|---------|-------------|------------|
| 0 | `Fase-0-Infraestructura.md` | Infraestructura base: Maven, CDI, ConfigMapping, paquetes | Ninguna |
| 1 | `Fase-1-Nucleo-Protocolo.md` | Núcleo del protocolo Inertia: API pública, PageObject, render, redirect, location, SharedData, VersionProvider, HTML/JSON | Fase 0 |
| 2 | `Fase-2-Protocolo-HTTP.md` | Protocolo HTTP: Redirects, Partial Reload, Version Mismatch, Headers, Status Codes | Fase 1 |
| 3 | `Fase-3-Optimizacion.md` | Optimización: Native Image, rendimiento, reducción de asignaciones, Hot Path | Fase 2 |
| 4 | `Fase-4-Avanzadas.md` | Características avanzadas: SSR, Deferred Props, Merge Props, Once Props | Fase 3 |

---

## Changelog v1.1 — Correcciones post-validación (2026-07-27)

Se validaron los planes contra los 3 adaptadores oficiales (Laravel, Rails, Phoenix) y se aplicaron 6 correcciones:

| # | Corrección | Archivos afectados |
|---|-----------|-------------------|
| 1 | `deferredProps: List<String>` → `Map<String, List<String>>` (soporta grupos) | Fase-1: `PageObject.java`; Fase-4: `PageObjectBuilder.java`, `Inertia.java`, `InertiaImpl.java`, `DeferredProp.java` |
| 2 | Metadata (deferred/merge/once) excluida en partial reload | Fase-2: `PageObjectBuilder.java` (flag `isPartial`); Fase-4: `PageObjectBuilder.build()` filtra metadatos si `isPartial=true` |
| 3 | Flash data persistente entre requests vía `FlashStore` SPI | Fase-4: `FlashStore.java`, `VertxSessionFlashStore.java`, `InertiaImpl.java` inyecta `FlashStore` |
| 4 | Capturar header `X-Inertia-Error-Bag` | Fase-2: `InertiaRequestFilter.java` |
| 5 | Cookie `XSRF-TOKEN` en respuestas Inertia | Fase-2: `InertiaCsrfFilter.java` (ContainerResponseFilter) |
| 6 | OnceProps con custom key + expiration | Fase-4: `OncePropRegistry.set(key, value, customKey, expiresAt)` |

## Reglas de ejecución

1. Cada fase debe completarse al 100% antes de iniciar la siguiente.
2. Cada fase debe pasar todos sus tests (unitarios + integración) antes de avanzar.
3. No implementar funcionalidades de fases posteriores.
4. No introducir código especulativo.
5. No anticipar optimizaciones innecesarias.
6. Actualizar la Matriz de Cumplimiento (ANEXO 0) al completar cada fase.
7. Ejecutar `mvn compile` y `mvn test` al final de cada fase.

## Convenciones

### Paquete base
```
com.quarkus.inertia
```

### Estilo de código
- Java moderno (records, pattern matching, switch expressions, text blocks)
- Colecciones inmutables
- Composición sobre herencia
- Sin comentarios redundantes
- Sin código muerto
- Sin warnings

### Testing
- **Tests unitarios**: JUnit 5 + Mockito, aislados, sin contexto Quarkus
- **Tests de integración**: @QuarkusTest + REST Assured, contexto completo
- Cobertura mínima: 80% en núcleo del protocolo

### Tecnologías (según ANEXO E)
- Java 25
- Quarkus 3.37
- Mutiny (Uni / Multi)
- CDI (Jakarta CDI)
- Qute (templates)
- JSON-B con auto-detect (fallback Jackson)
- GraalVM Native Image
- JUnit 5 + Mockito + REST Assured

### Coordenadas Maven
```xml
<groupId>com.quarkus.inertia</groupId>
<artifactId>quarkus-inertia</artifactId>
<version>0.1.0-SNAPSHOT</version>
```

---
