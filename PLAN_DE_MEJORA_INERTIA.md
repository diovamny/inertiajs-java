# Plan de mejora y auditoría de conformidad Inertia

**Estado:** planificado; no implementado  
**Fecha de auditoría base:** 22 de agosto de 2026  
**Alcance:** `spring-inertia`, `quarkus-inertia`, sus ejemplos, pruebas, documentación y automatización de entrega.  
**Restricción vigente:** no crear commits. Este documento no autoriza cambios de implementación por sí mismo.

> **Actualización de reauditoría — 23 de agosto de 2026.** Spring completa
> actualmente 96 pruebas sin fallos. Quarkus compila, pero su suite aislada
> termina con 16 fallos y 12 errores de 238 pruebas. Por tanto, el orden de
> este plan cambia: primero se restaura la funcionalidad básica de Quarkus;
> después se completa la conformidad estricta de ambos adaptadores. No se
> considerará alcanzado el 100% mientras exista una prueba roja, una
> desviación conocida de la matriz o una diferencia no documentada entre
> Spring y Quarkus.

## Plan de corrección posterior a la reauditoría

### Resultado objetivo

El resultado es **100% de los requisitos aplicables de la matriz oficial de
Inertia v3, comprobados en Spring y Quarkus**. La referencia de comportamiento
es el [protocolo oficial](https://inertiajs.com/docs/v3/core-concepts/the-protocol),
no una equivalencia superficial de nombres de API.

Un requisito se considera terminado solo si se cumplen estas cuatro condiciones:

1. La implementación de ambos frameworks da la misma respuesta HTTP observable.
2. Existe una prueba de regresión que fallaba antes de la corrección.
3. El caso aparece en la matriz de conformidad con una referencia oficial.
4. La suite completa, los ejemplos aplicables y el empaquetado pasan en limpio.

### Fase R0 — Congelar una línea base reproducible

**Objetivo:** separar fallos de código de problemas de la caché de compilación.

1. No usar `.m2-cache` compartida con compilaciones concurrentes para la
   validación de release en Windows; usar una caché Maven estable o una caché
   por ejecución.
2. Ejecutar Spring y Quarkus de forma aislada antes de una ejecución reactor
   paralela.
3. Registrar por módulo: número de pruebas, fallos, errores, versión de Java,
   versión de Maven, plataforma y configuración activa.
4. Añadir al CI una ejecución limpia que no dependa de archivos `target` ni de
   una caché local bloqueada.

**Aceptación:** las suites pueden repetirse dos veces seguidas sin errores de
acceso a JARs ni variación de resultado.

### Fase R1 — Recuperar Quarkus a estado funcional (P0)

Esta fase bloquea cualquier aspiración de conformidad hasta quedar en verde.

| Incidencia | Corrección requerida | Prueba de aceptación |
|---|---|---|
| `ResponseProcessor` pasa un estado `null` a una API `int` | Definir `200` antes de serializar una página JSON. Mantener un estado explícito solo cuando el controlador lo haya definido. Aplicar la misma regla a las rutas reactivas y síncronas. | GET Inertia normal, partial reload, ETag, redirect posterior y preserve fragment devuelven la respuesta esperada, nunca un 500. |
| El código convierte una página JSON mutante en `303` sin `Location` | Limitar `303 See Other` a objetos de redirección. Una página Inertia renderizada conserva su estado explícito o `200`. | POST que renderiza una página devuelve JSON válido; POST que redirige devuelve `303` con `Location`. |
| La cookie CSRF no se entrega en la primera visita HTML | Separar **emisión** de token y **validación**: emitir cookie en visitas seguras iniciales; validar solo solicitudes mutantes Inertia que deban protegerse. Usar el mismo contexto de sesión en JAX-RS y rutas Vert.x. | GET HTML obtiene `XSRF-TOKEN`; POST Inertia válido pasa; token ausente, erróneo o sin sesión recibe 419; una ruta no Inertia no es reescrita. |
| `OncePropRegistry` devuelve suppliers en vez de sus valores | Resolver once props perezosas dentro del ciclo reactivo, preservar su metadata y no drenarlas antes de la resolución. | Once lazy en full visit y con key personalizada produce el valor; expiradas se omiten. |
| Deferred/rescue no informa la prop rescatada | Resolver props diferidas en el momento seleccionado, capturar solo las que usen `rescue` y añadirlas a `rescuedProps`. | Error recuperable devuelve 200, omite la prop y lista exactamente su clave; éxito no lista nada. |

**Aceptación de fase:** las 28 pruebas rojas actuales de Quarkus pasan y no
aparecen regresiones en rutas JAX-RS, reactive routes o pruebas unitarias.

### Fase R2 — Recargas parciales y evaluación de props en Quarkus (P0/P1)

1. Reescribir la selección de propiedades como un único algoritmo con estos
   pasos: comprobar componente, seleccionar `only`, sustraer `except`, añadir
   `always`, resolver únicamente valores seleccionados.
2. Tratar rutas anidadas de forma estructural: al pedir `auth.user`, copiar
   solo esa rama dentro de `auth`; al excluirla, eliminar solo esa rama y
   conservar sus hermanas. No mezclar mapas anidados y claves planas.
3. Aplicar reset a padres y descendientes: resetear `contacts` elimina
   etiquetas de merge para `contacts`, `contacts.data` y sus descendientes.
4. Resolver `optional` tanto por `only` como por `except`; no resolverlo sin
   selector en una visita parcial.
5. Para `once`, ignorar `X-Inertia-Except-Once-Props` si la prop fue
   seleccionada explícitamente por partial reload.
6. No convertir shared props ordinarias en props `always`; conservar sus
   metadatos `sharedProps` y aplicar la regla de selección adecuada.

**Pruebas obligatorias:** tabla cruzada para `only`, `except`, ambas cabeceras,
dot notation, `always`, `optional`, `deferred`, `once`, `merge`, `prepend`,
`deepMerge`, `matchOn`, `scroll` y `X-Inertia-Reset`.

### Fase R3 — Respuestas HTTP y navegación de Quarkus (P1)

1. Usar siempre `request.uri()` o `rawPath + rawQuery` como URL del page
   object; nunca `absoluteURI()` ni `getRequestUri().toString()`.
2. Hacer que `inertia.redirect(url)` detecte una URL externa igual que las
   demás sobrecargas y emita `409 + X-Inertia-Location` en una visita Inertia.
3. En asset mismatch, producir un `409` sin `X-Inertia`, con
   `X-Inertia-Location`, `X-Inertia-Version` de la página resultante y el
   `Vary` requerido.
4. Mantener ETags HTTP válidos, incluyendo comillas, `W/`, listas de
   `If-None-Match` y `*`; no calcular un ETag para respuestas de control.
5. Implementar o verificar `Purpose: prefetch`, `Cache-Control: no-cache`,
   precognition, error bag, fragment redirects y `Vary` sin encabezados
   duplicados contradictorios.

**Aceptación:** contratos HTTP de JAX-RS y Vert.x dan el mismo status, headers
y body para cada visita de la matriz.

### Fase R4 — Cerrar las brechas de Spring MVC (P1)

| Brecha | Corrección requerida | Prueba de aceptación |
|---|---|---|
| Optional ignora `except` | Compartir la misma función de selección con deferred/partial; resolver optional al ser elegido por `only` o quedar incluido por `except`. | Full visit no lo contiene; `only` y `except` lo resuelven cuando corresponde. |
| Once se omite incluso si fue pedido explícitamente | Aplicar `Except-Once` después de saber qué props fueron solicitadas; la solicitud parcial explícita prevalece. | `only: notice` devuelve `notice` aunque el header Except-Once lo incluya. |
| No existe API para `clearHistory` | Añadir la operación de fachada, atributo de solicitud y valor condicional en el page object, igual que Quarkus. | La API pública y el JSON entregan `clearHistory: true` solo cuando se pide. |
| `camelizeProps` hace snake_case | Sustituir por conversión recursiva a camelCase y alinear documentación/API. | Claves planas, mapas anidados y listas pasan de `first_name` a `firstName`. |
| CSRF no se inicializa en HTML normal | Aplicar la misma separación emisión/validación definida en R1. | La primera página HTML emite token y la validación Inertia mutante sigue siendo estricta. |
| Props shared se incluyen fuera de la selección parcial | Comprobar y alinear el comportamiento contra la matriz de evaluación del protocolo. | Un partial solo entrega shared props si su categoría/regla lo exige. |

### Fase R5 — Conformidad HTML, seguridad y SSR compartida (P1)

1. Usar una única codificación JSON para `<script type="application/json">`:
   escapar `/`, `<`, `>`, `&`, U+2028 y U+2029 sin usar entidades HTML dentro
   del cuerpo del script.
2. Probar payloads con `</script>`, Unicode separador, atributos HTML y JSON
   válido tras `JSON.parse`.
3. Mantener la política de errores segura por defecto; las pruebas que activan
   detalles deben estar aisladas de la configuración de producción.
4. Configurar una fecha límite real de SSR, no solo timeout de conexión/idle.
   La API síncrona no puede usar espera indefinida. Definir fallback, logging
   estructurado y una política de cancelación del cliente.
5. Escapar atributos y nodos HTML generados a partir de datos SSR salvo que
   se declaren explícitamente contenido confiable.

**Aceptación:** pruebas de seguridad reproducen los payloads hostiles; un SSR
que no responde retorna fallback dentro del límite configurado para Spring y
Quarkus.

### Fase R6 — Versionado de assets y caché de ambos adaptadores (P2)

1. Definir una sola precedencia: `version-custom` > manifest Vite > huella de
   assets > versión estable derivada de la distribución.
2. Al hashear, incluir ruta normalizada, longitud y contenido de cada archivo;
   no concatenar bytes sin límites ni nombre de archivo.
3. Enumerar recursos en directorio y JAR sin depender de una entrada de
   directorio opcional del ZIP. Cubrir empaquetado Spring Boot, Quarkus JVM y
   nativo cuando aplique.
4. Reemplazar el fallback basado solo en el nombre de clase por un identificador
   de build real o exigir versión explícita si no puede obtenerse.
5. Hacer configurables, observables y deterministas los límites de caché;
   limpiar entradas expiradas y evitar cálculos duplicados para una misma clave.

**Aceptación:** misma distribución = misma versión; cambio de asset/manifest =
nueva versión; JAR empaquetado y directorio producen resultados correctos.

### Fase R7 — Matriz de conformidad, ejemplos y release (P0 para la meta 100%)

1. Crear `docs/conformance-matrix.md` con un requisito por fila: fuente
   oficial, Spring, Quarkus, prueba exacta, resultado y exclusión razonada.
2. Añadir pruebas de contrato HTTP idénticas para ambos módulos. Los fixtures
   deben cubrir HTML inicial, JSON Inertia, mismatch, redirect, external
   location, partial reload, props especiales, CSRF, SSR y ETag.
3. Añadir pruebas de concurrencia para versión por solicitud, once props,
   props compartidas y caché.
4. Validar todos los ejemplos contra un cliente Inertia real y documentar la
   configuración de CSRF, SSR y versionado.
5. Alinear POM, changelog, README, ramas de CI y perfiles de empaquetado.

**Criterio final de 100%:**

- Spring y Quarkus: 0 fallos y 0 errores en suites completas.
- Todas las filas aplicables de `conformance-matrix.md` aprobadas en ambos.
- Ningún P0/P1 abierto; diferencias de API únicamente si están justificadas
  por el framework y producen el mismo wire contract.
- Verificación en limpio de JAR, ejemplos y, para Quarkus, perfil nativo si
  está disponible en el entorno.

### Orden de ejecución

1. R0 y R1 completos: no mezclar mejoras nuevas mientras Quarkus devuelva 500.
2. R2 y R3 con pruebas de contrato primero.
3. R4 en paralelo conceptual, manteniendo la misma matriz y los mismos
   fixtures que Quarkus.
4. R5 y R6 solo después de que el protocolo básico esté verde.
5. R7 y reauditoría final; entonces, y solo entonces, recalcular puntuaciones.

## 1. Objetivo verificable

Llevar ambos adaptadores a conformidad completa con la matriz aplicable del protocolo Inertia v3 y a una madurez de producción demostrable. La referencia normativa es el [protocolo oficial de Inertia](https://inertiajs.com/docs/v3/core-concepts/the-protocol).

No se asignará una puntuación de `10/10` por intención ni por una suite verde. Para considerarlo logrado deberán cumplirse simultáneamente:

1. Todos los requisitos aplicables de la matriz están implementados en Spring y Quarkus.
2. Cada requisito tiene una prueba automatizada de contrato, integración o regresión.
3. Los casos de seguridad, concurrencia, empaquetado y fallback están cubiertos.
4. La documentación, los ejemplos, el versionado y el CI no contradicen el comportamiento probado.

## 2. Línea base de auditoría

Se ejecutó la suite Maven existente como diagnóstico, sin editar fuentes. Los reportes de Surefire disponibles indican cero fallos y cero errores. Esto solo certifica las pruebas actuales: no equivale a conformidad total porque faltan escenarios de protocolo, seguridad y concurrencia.

### Riesgos encontrados

| Prioridad | Hallazgo | Impacto |
|---|---|---|
| P0 | El JSON de la página se inserta sin codificación segura en un contexto `<script>` en Spring y Quarkus. | Posible XSS mediante valores serializados que cierren el script. |
| P0 | Spring mezcla props en sesión del servidor. | Estado obsoleto entre visitas y pestañas; Inertia define la fusión en el cliente mediante metadatos. |
| P1 | `once` se controla con estado de sesión en lugar de la cabecera del cliente `X-Inertia-Except-Once-Props`. | Semántica incorrecta, problemas de expiración y de múltiples pestañas. |
| P1 | `optional`, `always`, props compartidas y la combinación `only`/`except` no cumplen de forma completa la precedencia del protocolo. | Recargas parciales con datos erróneos o cálculo innecesario. |
| P1 | Quarkus conserva la versión de página como estado mutable global. | Una solicitud puede afectar a otra en carga concurrente. |
| P1 | CSRF en Quarkus acepta casos sin sesión; el filtro Spring se aplica más allá de solicitudes Inertia. | Seguridad inconsistente y efectos colaterales en rutas ajenas. |
| P1 | SSR no tiene límites robustos de tiempo y los errores pueden exponer detalles internos. | Saturación de solicitudes y filtración de información. |
| P2 | Versionado de assets no es fiable para JAR/nativo; ETag, redirects externos y URL de página tienen divergencias. | Caché rota o comportamiento distinto al de los adaptadores oficiales. |
| P2 | CI, versionado, changelog y ramas configuradas no están completamente alineados. | Riesgo de publicación y mantenimiento. |

## 3. Matriz de conformidad que se construirá primero

Antes de cambiar comportamiento, se añadirá una matriz de requisitos trazable con estas categorías:

| Categoría | Casos que deben verificarse |
|---|---|
| Detección de visita | `X-Inertia`, `X-Inertia-Version`, componente, URL y método HTTP. |
| Respuesta de página | `X-Inertia`, JSON, componente, props, URL, versión y metadatos de historia. |
| Versionado | Coincidencia, mismatch `409`, `X-Inertia-Location`, versión configurada y versión de assets. |
| Redirecciones | `302`, conversión a `303` tras métodos mutantes, redirección externa `409`, cabeceras y preservación de `Vary`. |
| Recargas parciales | `X-Inertia-Partial-Component`, `X-Inertia-Partial-Data`, `X-Inertia-Partial-Except`, dot notation y combinación de filtros. |
| Props especiales | `always`, `optional`, `deferred`, `once`, `merge`, `prepend`, `deepMerge`, `scroll` y reset. |
| Estado e historia | `clearHistory`, `encryptHistory`, `preserveFragment` y flash. |
| Precognition y prefetched visits | Cabeceras, respuestas y ausencia de efectos de navegación no deseados. |
| Renderizado HTML / SSR | Escape seguro, fallback, timeout, estado SSR y plantilla raíz. |
| HTTP transversal | `Vary`, ETag válido, `If-None-Match`, cookies y errores. |

Cada fila indicará: fuente oficial, soporte Spring, soporte Quarkus, tipo de prueba, archivo de prueba y resultado.

## 4. Fases de ejecución

### Fase 1 — Seguridad y aislamiento de solicitudes (P0)

**Cambios previstos**

1. Crear un serializador JSON seguro para contexto HTML `script` y usarlo exclusivamente al insertar `data-page`.
2. Escapar `<`, `>`, `&`, U+2028 y U+2029 tras serializar JSON, sin modificar el JSON entregado por respuestas Inertia.
3. Sustituir los mensajes de excepción expuestos por un error genérico de producción, conservando el detalle solo en logs o en configuración explícita de desarrollo.
4. Eliminar la fusión de props basada en sesión de Spring. El servidor emitirá únicamente datos de la respuesta y metadatos de merge; el cliente realizará la fusión.
5. Hacer que la versión indicada por una llamada de Quarkus viva solo en el contexto de la solicitud actual, no en un singleton mutable.

**Pruebas de aceptación**

- Un prop con `</script><script>…` no puede crear un segundo elemento script ejecutable.
- Dos solicitudes simultáneas con versiones distintas devuelven su propia versión.
- Dos pestañas no heredan arrays, props ni metadatos de una respuesta anterior.
- Las respuestas de error de producción no contienen mensaje, clase ni traza de excepción.

### Fase 2 — Props parciales y props especiales (P0/P1)

**Cambios previstos**

1. Definir un único algoritmo de selección de props para ambos adaptadores.
2. Aplicar primero `only` y después `except`; respetar la intersección cuando ambas cabeceras estén presentes.
3. Permitir selección con dot notation sin eliminar ramas no relacionadas.
4. Mantener `always` aunque exista filtro parcial; no mantener automáticamente toda prop compartida.
5. Resolver `optional` solo cuando lo seleccionen `only` o `except`; no evaluarlo en una visita parcial sin selector.
6. Mantener `deferred` fuera de la visita inicial y respetar grupos, filtros y errores de resolución.
7. Convertir `once` a un mecanismo dirigido por `X-Inertia-Except-Once-Props`, incluyendo la excepción cuando la prop se pide explícitamente en una recarga parcial.
8. Emitir metadatos correctos para `merge`, `prepend`, `deepMerge`, `matchOn` y reset sin guardar snapshots de props en sesión.

**Pruebas de aceptación**

- Matriz cruzada de `only`, `except`, `always`, `optional`, `once`, shared props y dot notation en Spring y Quarkus.
- El callback de una prop perezosa no se ejecuta cuando el protocolo no la selecciona.
- Una prop `once` vuelve a entregarse si el cliente la solicita explícitamente.
- `X-Inertia-Reset` elimina solo los metadatos de merge/scroll solicitados y no datos ajenos.

### Fase 3 — HTTP, navegación e historia (P1)

**Cambios previstos**

1. Normalizar `Vary` para todas las respuestas afectadas por cabeceras Inertia.
2. Devolver `409` y `X-Inertia-Location` para mismatch de versión y navegación externa conforme a protocolo.
3. Aplicar `303 See Other` tras peticiones mutantes de Inertia cuando corresponda.
4. Usar URL relativa con query string como valor de página en todos los puntos de entrada Quarkus.
5. Exponer y propagar `clearHistory`, `encryptHistory` y `preserveFragment` con una API equivalente en Spring y Quarkus.
6. Corregir ETags: formato entre comillas, comparación de listas, comodín `*` y respuesta `304` correcta.
7. Unificar prefetch, precognition y validación de cabeceras históricas/compatibles.

**Pruebas de aceptación**

- Contratos HTTP de cada código de estado, cabecera y combinación de método.
- Peticiones `If-None-Match` con una lista de ETags y con `*`.
- Redirect externo y asset mismatch se validan como casos distintos.

### Fase 4 — CSRF, SSR, errores y rendimiento (P1)

**Cambios previstos**

1. Limitar la protección CSRF propia del adaptador a solicitudes Inertia mutantes, salvo configuración explícita de alcance global.
2. Rechazar de forma inequívoca solicitudes Inertia mutantes con token, cookie o sesión ausentes/incorrectos.
3. Configurar timeout de conexión y lectura para SSR; aplicar fallback HTML o JSON definido, con logs estructurados.
4. Añadir límites de tamaño, expiración y observabilidad a cachés de props si conservan estado en proceso.
5. Evitar buffering indiscriminado de respuestas no Inertia en el filtro Spring.

**Pruebas de aceptación**

- GET inicial obtiene el token necesario; POST Inertia sin token recibe rechazo; rutas no Inertia no son modificadas por el filtro del adaptador.
- SSR inaccesible, lento, inválido y correcto siguen una política documentada y no bloquean indefinidamente.
- Pruebas de concurrencia y de memoria validan que no hay datos cruzados ni crecimiento sin límite.

### Fase 5 — Versionado de assets y empaquetado (P2)

**Cambios previstos**

1. Definir precedencia inequívoca: versión explícita > manifest Vite > huella de recursos > valor estable documentado.
2. Calcular huellas deterministas de recursos dentro de directorios, JAR y artefactos nativos cuando sean accesibles.
3. Eliminar fallbacks basados en timestamp que cambian sin modificación de assets.
4. Validar la configuración `custom`, `vite`, `sha256` y ausencia de assets sin producir mismatch erróneo.

**Pruebas de aceptación**

- La misma distribución genera la misma versión en dos reinicios.
- Cambiar un asset o el manifest cambia la versión.
- Pruebas de recursos empaquetados cubren directorio y JAR; Quarkus incluye ejecución nativa cuando el entorno lo permita.

### Fase 6 — Paridad pública y documentación (P2)

**Cambios previstos**

1. Inventariar todas las APIs de Spring y Quarkus y documentar diferencias justificadas por el framework.
2. Alinear nombres, valores predeterminados, propiedades de configuración y ejemplos.
3. Actualizar README, guía de migración, changelog y referencias de versión.
4. Corregir los disparadores CI para las ramas reales y añadir jobs de pruebas de contrato.
5. Verificar todos los ejemplos contra una aplicación cliente Inertia real.

**Pruebas de aceptación**

- Tabla pública de paridad sin discrepancias accidentales.
- Ejemplos arrancan, navegan, hacen recargas parciales, validan formularios y prueban SSR cuando está activado.
- CI ejecuta la misma matriz esencial para ambos módulos.

## 5. Estrategia de pruebas

| Nivel | Propósito | Ejemplos de pruebas |
|---|---|---|
| Unitarias | Aislar parsing de cabeceras, selección de props, versionado y ETag. | `only` + `except`, `once`, SHA-256 de JAR, ETag multi-valor. |
| Integración de framework | Verificar filtros, cookies, sesiones, controladores y serialización reales. | CSRF, 303, 409, HTML inicial, JSON Inertia. |
| Contrato HTTP | Comparar respuesta observable contra la matriz del protocolo. | Cabeceras, body, código y `Vary`. |
| Seguridad | Evitar regresiones de XSS, exposición de error y CSRF. | Payload JSON hostil, excepción controlada, token inválido. |
| Concurrencia | Asegurar aislamiento entre solicitudes y pestañas. | Versiones distintas, once y props paralelas. |
| Empaquetado | Validar artefactos que no son el árbol de fuentes. | JAR, recursos empaquetados y nativo Quarkus. |
| E2E de ejemplos | Validar interacción con el cliente Inertia. | Navegación, formulario, flash, deferred y SSR. |

La ejecución mínima de verificación será:

```text
mvn -Dmaven.repo.local=.m2-cache clean test -T 1C
```

Además se ejecutarán los perfiles de integración, empaquetado y nativo que se definan durante la Fase 5. Ninguna prueba nueva deberá depender de tiempo real, orden de ejecución o estado compartido entre casos.

## 6. Criterios de puntuación final

La puntuación se separará en dos ejes para cada adaptador:

| Eje | Evidencia necesaria para 10/10 |
|---|---|
| Conformidad del protocolo | 100% de requisitos aplicables de la matriz aprobados, sin exclusiones silenciosas. |
| Madurez | Seguridad P0/P1 resuelta, aislamiento concurrente, pruebas de contrato y regresión, empaquetado, observabilidad, CI, documentación y ejemplos verificados. |

La nota conjunta se calculará a partir de los resultados independientes de Spring y Quarkus, sin ocultar una divergencia de uno de los dos detrás de la media.

## 7. Orden de implementación obligatorio

1. Crear matriz y pruebas que reproduzcan los defectos P0/P1.
2. Corregir seguridad y aislamiento de solicitudes.
3. Corregir selección y ciclo de vida de props.
4. Corregir HTTP, historia, versiones y SSR.
5. Añadir pruebas de empaquetado, concurrencia y E2E.
6. Alinear API, documentación, ejemplos, CI y release.
7. Ejecutar la batería completa en limpio.
8. Reauditar y publicar la matriz final con resultados reales.

No se hará commit durante ninguna fase hasta recibir autorización explícita.

## 8. Archivos inicialmente implicados

- `spring-inertia/src/main/java/io/github/dg/spring/inertia/protocol/PageObjectBuilder.java`
- `spring-inertia/src/main/java/io/github/dg/spring/inertia/protocol/PartialReloadProcessor.java`
- `spring-inertia/src/main/java/io/github/dg/spring/inertia/protocol/OncePropRegistry.java`
- `spring-inertia/src/main/java/io/github/dg/spring/inertia/renderer/HtmlRenderer.java`
- `spring-inertia/src/main/java/io/github/dg/spring/inertia/mvc/InertiaCsrfFilter.java`
- `quarkus-inertia/src/main/java/io/github/dg/quarkus/inertia/protocol/PageObjectBuilder.java`
- `quarkus-inertia/src/main/java/io/github/dg/quarkus/inertia/protocol/OncePropRegistry.java`
- `quarkus-inertia/src/main/java/io/github/dg/quarkus/inertia/renderer/HtmlRenderer.java`
- `quarkus-inertia/src/main/java/io/github/dg/quarkus/inertia/version/DefaultVersionProvider.java`
- Los tests de integración, pruebas unitarias, ejemplos, workflows CI, README y changelog relacionados.

## 9. Fuera de alcance sin nueva decisión

- Cambiar el protocolo Inertia o sus clientes oficiales.
- Publicar artefactos, tags, releases o dependencias en repositorios remotos.
- Hacer commits, push o reescribir historial Git.
- Modificar aplicaciones consumidoras fuera de este repositorio.
