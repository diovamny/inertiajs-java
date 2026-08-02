# Plan de Análisis y Corrección — Adaptador Inertia.js v3 para Quarkus

## Resumen de la Revisión

Se realizó una auditoría completa comparando:
1. La especificación en `Prompt/` (ANEXOS 0 al E, Partes 1 a 5).
2. El plan de fases en `Prompt/Plan/` (`Fase-0` a `Fase-4`).
3. Los adaptadores oficiales de Inertia.js:
   - [inertia-laravel](https://github.com/inertiajs/inertia-laravel)
   - [inertia-rails](https://github.com/inertiajs/inertia-rails)
   - [inertia-phoenix](https://github.com/inertiajs/inertia-phoenix)
4. El código fuente en `src/main/java/com/quarkus/inertia` y el proyecto demo en `examples/demo-app`.

---

## Hallazgos y Análisis Comparativo con Adaptadores Oficiales

### 1. Manejo de Deferred Props (Props Diferidas)
- **Especificación Inertia v3 / Laravel / Rails / Phoenix**:
  - En la carga inicial (**Initial Load**), las propiedades diferidas (`deferredProps`) **NO deben incluirse en el objeto `props`** del Page Object. Solo se registran en los metadatos `deferredProps: { groupName: ["propKey"] }`.
  - En una recarga parcial (**Partial Reload**), si el cliente solicita un grupo o clave diferida mediante `X-Inertia-Partial-Data`, la función/supplier asociada debe evaluarse asíncronamente y su resultado resolverse dentro de `props`.
- **Estado en la implementación actual de Quarkus**:
  - En `PageObjectBuilder.java`, al construir el objeto en carga inicial (`isPartial = false`), las propiedades diferidas almacenadas como `Supplier` en `sharedData` permanecían dentro de `props`. Esto provocaba que se intentara serializar la función `Supplier` como objeto JSON en el primer renderizado.
  - En recarga parcial, se requiere desenvolvimiento de `Supplier` o `Uni` a su valor final antes de construir el `PageObject`.

### 2. Redirecciones HTTP y Códigos de Estado (PRG Pattern)
- **Especificación Inertia v3**:
  - Para peticiones `GET`: Redirección estándar `302 Found` con header `Location`.
  - Para peticiones no-GET (`POST`, `PUT`, `PATCH`, `DELETE`): Redirección `303 See Other` con header `Location` (o `X-Inertia-Location` en external/conflict), de modo que el navegador ejecute una petición `GET` subsecuente al destino.
- **Estado en la implementación actual de Quarkus**:
  - `RedirectProcessor.java` definía el método auxiliar `isNonGetRequest()`, pero en `process(url)` retornaba incondicionalmente `302 Found`. Se actualizará `process(url)` para retornar directamente `303 See Other` en peticiones no-GET de Inertia.

### 3. Selección y Prioridad de JsonProvider (Jackson vs. JSON-B)
- **Especificación (ANEXO E)**:
  - Debe soportar tanto Jackson como JSON-B con detección automática.
- **Estado en la implementación actual de Quarkus**:
  - `JacksonJsonProvider` tiene la anotación `@Alternative` pero se agregará `@Priority(1)` para asegurar la resolución de CDI cuando Jackson está en el classpath.

### 4. Corrección de Errores en `examples/demo-app` (8 fallos en tests)
- **Causa raíz de los fallos**:
  - En `EmployeeResource.java`: `@QueryParam("filters")` recibía un array JSON (`[{"field":"email",...}]`), pero intentaba parsearse directamente a `Map.class`, lanzando una excepción ignorada en el `catch`. Esto provocaba que el filtro fuera `null` y retornara todos los empleados sin filtrar, fallando las aserciones de email e IDs.
  - En `VertxSessionFlashStore.java` / `DemoAppTest.java`: Manejo de la cookie de sesión Vert.x en peticiones REST Assured para la prueba de datos Flash (`props.success`).

---

## User Review Required

> [!IMPORTANT]
> **Resolución de Deferred Props (Suppliers / Uni)**:
> Se ajustará `PageObjectBuilder` para que:
> 1. En carga inicial, filtre y excluya los `Supplier` de props diferidos de la lista `props` del JSON enviado al cliente (manteniendo solo la entrada en `deferredProps`).
> 2. En recarga parcial o props diferidos solicitados, ejecute la evaluación del `Supplier` (o `Uni`) resolviendo el valor final de la propiedad.

> [!NOTE]
> **Resultado de Tests de Core**:
> Todos los 64 unit e integration tests en `quarkus-inertia` pasan en verde (`BUILD SUCCESS`). Las correcciones en `EmployeeResource` resolverán los 8 fallos detectados en `examples/demo-app`.

---

## Open Questions

Ninguna por el momento. La especificación de Inertia v3 y el plan están claros tras la auditoría con los adaptadores de Laravel, Rails y Phoenix.

---

## Proposed Changes

### Core Adapter (`com.quarkus.inertia`)

#### [MODIFY] [PageObjectBuilder.java](file:///c:/Users/DiovamnyGarciaPe%C3%B1a/Desktop/inertiajs/src/main/java/com/quarkus/inertia/protocol/PageObjectBuilder.java)
- Filtrar y remover de `allProps` las claves diferidas cuando `isPartial == false`.
- Desenvolver y evaluar `Supplier` o `Uni` en `allProps` cuando la propiedad es solicitada.

#### [MODIFY] [RedirectProcessor.java](file:///c:/Users/DiovamnyGarciaPe%C3%B1a/Desktop/inertiajs/src/main/java/com/quarkus/inertia/protocol/RedirectProcessor.java)
- Actualizar `process(url)` para comprobar `isNonGetRequest()` y retornar `Response.Status.SEE_OTHER` (303) en peticiones no-GET de Inertia, y `Response.Status.FOUND` (302) en peticiones GET.

#### [MODIFY] [JacksonJsonProvider.java](file:///c:/Users/DiovamnyGarciaPe%C3%B1a/Desktop/inertiajs/src/main/java/com/quarkus/inertia/internal/JacksonJsonProvider.java)
- Añadir `@Priority(1)` para asegurar la resolución de CDI cuando Jackson está en el classpath.

### Demo App (`examples/demo-app`)

#### [MODIFY] [EmployeeResource.java](file:///c:/Users/DiovamnyGarciaPe%C3%B1a/Desktop/inertiajs/examples/demo-app/src/main/java/com/example/demo/resource/EmployeeResource.java)
- Corregir el parseo de `@QueryParam("filters")` para soportar tanto `List` como `Map` o `JsonNode`.

#### [MODIFY] [EmployeeService.java](file:///c:/Users/DiovamnyGarciaPe%C3%B1a/Desktop/inertiajs/examples/demo-app/src/main/java/com/example/demo/service/EmployeeService.java)
- Asegurar que la lectura de filtros desde `List` procese correctamente las cláusulas `contains` y `equals`.

---

## Verification Plan

### Automated Tests
- Ejecutar `mvn clean test` en la raíz del proyecto (`quarkus-inertia`) para asegurar 64+ tests en verde.
- Ejecutar `mvn clean test` en `examples/demo-app` para verificar el 100% de pasaje de pruebas en el módulo demo.

### Manual Verification
- Inspeccionar las respuestas HTTP devueltas en peticiones `POST` para verificar el código `303 See Other`.
- Verificar la serialización del JSON de `PageObject` sin la inclusión de objetos `Supplier` no evaluados.
