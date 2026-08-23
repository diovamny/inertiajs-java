# Plan para 100% de protocolo Inertia v3 y madurez 10/10

**Estado:** plan de trabajo; no implica que el objetivo esté ya certificado.  
**Alcance:** `spring-inertia` y `quarkus-inertia`.  
**Referencia normativa:** [The Inertia v3 Protocol](https://inertiajs.com/docs/v3/core-concepts/the-protocol), con Laravel como implementación de referencia según esa especificación.

## 1. Qué significan los objetivos

### 100% de protocolo

Significa que cada requisito aplicable del contrato HTTP de Inertia v3 está implementado, probado de forma positiva y negativa, y es compatible con un cliente oficial real. No significa solamente que las pruebas internas pasen.

La evidencia mínima de cada requisito será:

1. requisito y enlace a la sección exacta del protocolo;
2. prueba de contrato que envía el request HTTP real;
3. aserción exacta de status, headers y cuerpo;
4. prueba de regresión para el caso límite que motivó la corrección;
5. ejecución con al menos un cliente oficial de Inertia en un navegador.

### Madurez 10/10

Significa que, además del protocolo, los adaptadores son operables y mantenibles en producción: pruebas no frágiles, seguridad, límites de recursos, observabilidad, empaquetado, documentación, compatibilidad y proceso de releases. Es una meta que se certifica por puertas de calidad, no por cantidad de funcionalidades.

## 2. Puertas de aceptación finales

El proyecto no se marcará como 100% / 10/10 hasta que todas estén en verde.

| Puerta | Criterio medible |
|---|---|
| Contrato | Matriz completa Inertia v3: cada fila tiene requisito, prueba Spring, prueba Quarkus y resultado PASS. Sin `PASS*` para requisitos obligatorios. |
| Compatibilidad real | Navegador automatizado contra clientes oficiales Vue, React y Svelte, para ambos backends, cubriendo visita inicial, navegación, parcial, redirect, error, SSR y versión. |
| Corrección | `mvn clean test` de raíz en un entorno limpio: 0 fallos, 0 errores, 0 pruebas deshabilitadas sin justificación. |
| Aislamiento | Pruebas ejecutadas tres veces consecutivas y en paralelo sin flakiness ni dependencia de orden. |
| Seguridad | Pruebas de XSS de JSON embebido, CSRF, redirects externos, exposición de errores y cabeceras de caché. |
| Operación | SSR, timeouts y fallbacks probados bajo fallo, lentitud, respuesta malformada y carga concurrente. |
| Entrega | JAR publicado/consumido desde un proyecto externo mínimo; ejemplos arrancan y una guía permite integrarlos sin código interno. |

## 3. Fase 0 — convertir el protocolo en una especificación ejecutable

**Objetivo:** evitar que una matriz de texto declare PASS sin evidencia.

1. Reescribir `docs/conformance-matrix.md` para que cada fila contenga:
   - sección y URL de la norma;
   - prioridad (`P0`, `P1`, `P2`);
   - clase y método de prueba Spring;
   - clase y método de prueba Quarkus;
   - prueba E2E correspondiente;
   - estado `PASS`, `FAIL` o `N/A` con justificación.
2. Eliminar declaraciones hoy contradichas por el código: 303 de páginas renderizadas, `optional` con `except`, `once` parcial de Quarkus, JSON seguro y shared props parciales.
3. Crear fixtures HTTP idénticos para ambos adaptadores. Cada fixture debe validar status, `Vary`, cabeceras Inertia y JSON completo, no solo un prop aislado.
4. Añadir un job de CI que falle si una fila `PASS` apunta a una prueba inexistente.

**Criterio de salida:** la matriz es trazable, no contiene resultados declarativos sin prueba y reproduce los ejemplos del protocolo.

## 4. Fase 1 — correcciones comunes de contrato y seguridad

### 4.1 JSON embebido en HTML

Corregir ambos `SafeJsonEncoder` para escapar cada `/` como `\\/`, además de los caracteres ya tratados. La salida debe seguir siendo JSON válido y no usar entidades HTML dentro de `<script>`.

Pruebas obligatorias:

- URL normal: `"/events/80"` se serializa como `"\\/events\\/80"` en el script;
- prop hostil con `</script><script>…` no cierra el nodo script;
- `<`, `>`, `&`, U+2028 y U+2029;
- `JSON.parse()` del contenido extraído del script;
- plantilla Spring y plantilla Quarkus, incluida la variante Qute.

### 4.2 Semántica de shared props

Tratar una shared prop normal como prop regular durante una recarga parcial. Solo los props marcados explícitamente como `always` deben sobrevivir a `only` y `except`.

Esto requiere eliminar la inclusión incondicional de shared props de los filtros parciales de ambos adaptadores y conservar `errors` como `always`.

Pruebas obligatorias:

- `only=events` entrega `events` y `errors`, pero no `auth` compartido regular;
- `except=auth` excluye `auth` y conserva los demás props regulares;
- `only` y `except` simultáneos: primero se restringe por `only` y después se excluye por `except`;
- shared prop marcada `always` permanece incluso en `except`.

**Criterio de salida:** los dos adaptadores entregan el mismo page object para el mismo fixture parcial.

## 5. Fase 2 — cerrar los defectos de Spring MVC

### 5.1 Status de una página renderizada

Eliminar la conversión automática de cualquier page object de una solicitud mutante a `303`. Mantener `200` —o el status explícitamente definido para una página— y aplicar `303` solamente al procesar un objeto redirect con `Location`.

Pruebas:

- `POST` que renderiza una página Inertia: `200`, `X-Inertia: true`, page JSON y sin `Location`;
- `POST` que redirige: `303` con `Location`;
- `GET` redirect: `302`;
- redirect externo: `409` más `X-Inertia-Location`.

### 5.2 `optional()` con `X-Inertia-Partial-Except`

Modificar `InertiaImpl.optional()` para resolver el prop cuando el componente coincide y el prop queda seleccionado por cualquiera de estas reglas:

- está en `Partial-Data`; o
- no está en `Partial-Except` cuando la petición solo usa `except`; o
- está en `Partial-Data` y no está en `Partial-Except` cuando ambos aparecen.

La misma selección debe reutilizarse para `optional`, `deferred`, `lazy` y la filtración final; no duplicar reglas de selección en varios métodos.

### 5.3 Props `once` con clave de seguimiento personalizada

Separar claramente:

- **tracking key**: clave enviada en `X-Inertia-Except-Once-Props` y usada en `onceProps`;
- **prop key**: clave real dentro de `props` y de `Partial-Data`.

En una parcial explícita, comprobar si `Partial-Data` contiene `OnceProp.prop()`, no si contiene la tracking key.

### 5.4 Pruebas específicas Spring

Añadir pruebas de integración para los tres defectos anteriores y para combinaciones `only + except + nested paths + shared/always + once`. Cubrir tanto `MockMvc` como un servidor HTTP arrancado en puerto aleatorio para verificar cabeceras reales.

**Criterio de salida:** no existe conversión implícita a 303, y las tablas de evaluación de props del protocolo pasan en Spring.

## 6. Fase 3 — cerrar los defectos de Quarkus

### 6.1 URL relativa del page object

En JAX-RS y Vert.x, construir la URL como `path + ?query`, nunca con esquema, host o puerto. Mantener la URL absoluta únicamente donde la semántica HTTP de redirect la requiera.

Pruebas:

- JAX-RS con host y puerto: `/events?page=2`;
- ruta reactiva con host y puerto: `/events?page=2`;
- proxy con `X-Forwarded-*`: el page object sigue siendo relativo;
- versión desactualizada conserva la URL correcta en `X-Inertia-Location`.

### 6.2 Motor único de selección parcial

Reemplazar la lógica repartida entre `PageObjectBuilder` y `PartialReloadProcessor` por un selector común que reciba el conjunto completo de props y produzca:

1. qué props se resuelven;
2. qué props se serializan;
3. qué rutas anidadas sobreviven;
4. qué metadatos merge se eliminan por `reset`.

Reglas obligatorias:

- `only` puede contener muchas rutas hermanas (`auth.user,auth.permissions`), no solo la primera;
- `except` puede contener muchas rutas;
- `only` se aplica antes que `except`;
- una ruta padre en `X-Inertia-Reset` elimina etiquetas descendientes como `contacts.data` y `contacts.data.id`;
- los props `always` sobreviven; los shared props regulares no;
- una partial incompatible con el componente no filtra nada.

### 6.3 `optional`, `deferred` y suppliers con `except`

Calcular las claves solicitadas a partir de **todos** los props de la página, incluidos los declarados por el endpoint, no solo `sharedData`. En una petición exclusiva con `except`, resolver todo supplier no excluido; en `only + except`, resolver solo los seleccionados y no excluidos.

Pruebas con supplier local y shared para `only`, `except`, ambos headers y rutas no coincidentes.

### 6.4 Props `once`

Construir el contexto parcial antes de aplicar `Except-Once`. Si una parcial solicita explícitamente el prop, ignorar `Except-Once` y devolver valor fresco; conservar el comportamiento de omisión en visitas no parciales.

### 6.5 SSR y límites de recursos

1. Configurar explícitamente la unidad de `idleTimeout` o pasar segundos; no convertir un `Duration` de 10 segundos a 10.000 segundos por error.
2. Aplicar timeout total a la operación SSR, incluidas las rutas síncronas; sustituir `await().indefinitely()` por una espera acotada.
3. Cerrar el cliente HTTP en éxito, timeout, cancelación y error.
4. Registrar un evento/métrica de fallback SSR sin incluir body, stack ni datos sensibles en la respuesta.
5. Probar timeout de conexión, lectura lenta, body JSON inválido, 500, cancelación y recuperación a HTML sin SSR.

**Criterio de salida:** Quarkus pasa los mismos fixtures de contrato que Spring en JAX-RS y rutas reactivas, y ninguna petición SSR puede quedar bloqueada indefinidamente. La configuración de Vert.x debe verificarse contra su API, donde `setIdleTimeout(int)` usa segundos por defecto.

## 7. Fase 4 — suite de compatibilidad con clientes reales

**Objetivo:** detectar diferencias que las pruebas Java no pueden observar.

1. Usar las aplicaciones de ejemplo existentes como fixtures de navegador.
2. Añadir Playwright con escenarios compartidos para Vue, React y Svelte:
   - visita HTML inicial e hidratación;
   - navegación Inertia y reemplazo de componente;
   - asset version mismatch;
   - `only`, `except`, rutas anidadas y reset;
   - optional, deferred, rescued, once y custom once keys;
   - redirects 302/303, externo 409 y fragment redirect;
   - CSRF válido e inválido;
   - SSR activo, timeout y fallback;
   - script JSON con contenido hostil.
3. Ejecutar la misma especificación contra Spring, Quarkus JAX-RS y Quarkus reactivo.
4. Guardar HAR, captura de respuesta y screenshot solo al fallar para diagnóstico reproducible.

**Criterio de salida:** los tres clientes oficiales completan la suite sin modal de error de Inertia, recarga inesperada ni diferencias de page object.

## 8. Fase 5 — elevar madurez a 10/10

### Calidad y mantenibilidad

- Activar cobertura de líneas y ramas en CI, con umbral por módulo y sin excluir el código de protocolo.
- Mutation testing focalizado en selección parcial, redirects, CSRF, serialización y version mismatch.
- Añadir pruebas de orden aleatorio y repetición para detectar estado request-scoped filtrado entre peticiones.
- Centralizar reglas compartidas para reducir divergencia Spring/Quarkus; mantener adaptadores de framework delgados.
- Revisiones obligatorias de API pública, compatibilidad binaria y semántica antes de cada release.

### Seguridad

- Fuzzing de valores de props para HTML/script, cabeceras y rutas.
- Auditoría de dependencias y generación de SBOM.
- Política documentada para CVE, versiones soportadas y tiempos de corrección.
- Pruebas de CSRF con cookie, header, sesión, rotación, SameSite, Secure y proxy HTTPS.

### Fiabilidad y rendimiento

- Pruebas de carga de respuestas parciales, deferred y SSR.
- Métricas: latencia, fallbacks SSR, errores de serialización, version mismatches y rechazos CSRF.
- Límites configurables de tamaño de prop/HTML/SSR response y manejo de sobrecarga.
- Benchmarks versionados para evitar regresiones de serialización y resolución lazy.

### Distribución y soporte

- Verificar publicación e instalación desde Maven Central (o repositorio objetivo) en proyectos externos Spring Boot y Quarkus.
- Probar JAR empaquetado, Quarkus JVM y, si se ofrece soporte, imagen nativa.
- README por adaptador: inicio mínimo, tabla de propiedades, SSR, CSRF, upgrade, compatibilidad Java/framework y troubleshooting.
- Changelog con cambios incompatibles y ejemplos completos ejecutables.

## 9. Orden recomendado de ejecución

1. Fase 0: matriz ejecutable y fixtures de contrato.
2. Fase 1: JSON seguro y shared props, porque afectan ambos adaptadores.
3. Fase 2: Spring; dejar su semántica parcial y de redirect correcta.
4. Fase 3: Quarkus; unificar selección parcial antes de añadir más features.
5. Fase 4: navegador y clientes oficiales; corregir diferencias reveladas.
6. Fase 5: cobertura, seguridad, carga, empaquetado y release gates.
7. Reauditoría independiente: ejecutar todas las puertas en un clon limpio y publicar el informe con versiones exactas.

## 10. Definición de terminado

La mejora estará terminada únicamente cuando:

- no quede ningún hallazgo P0 o P1 abierto;
- cada fila aplicable de la matriz tenga pruebas existentes y PASS en ambos adaptadores;
- Spring, Quarkus JAX-RS y Quarkus reactivo pasen la misma suite E2E con clientes oficiales;
- no haya fallos ni flakiness en ejecuciones repetidas de CI;
- SSR tenga límites correctos y fallback seguro;
- la documentación, ejemplos y artefactos publicados hayan sido validados desde consumidores externos;
- una reauditoría sin participación del implementador confirme **100/100 de protocolo** y **10/10 de madurez**.

Hasta cumplir esas condiciones, las puntuaciones de la auditoría actual siguen siendo: Spring 83/100 y 7.6/10; Quarkus 74/100 y 6.8/10.
