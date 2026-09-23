# Plan de ejecución hacia 100 % de Inertia v3 — Java (Spring y Quarkus)

- **Estado:** propuesta ejecutable
- **Fecha:** 2026-09-23
- **Base revisada:** Inertia Java 0.0.4, commit auditado f25fb5a
- **Entradas consolidadas:** auditorías históricas de Fase 0–5, planes v2–v4 y [auditoría comparativa del 2026-09-23](AUDITORIA_INERTIA_JAVA_2026-09-23.md).

## Resultado que persigue este plan

El objetivo no es volver a escribir el adaptador ni sumar checklists que se
marquen solos. El resultado es poder afirmar, con evidencia reproducible:

> Inertia Java implementa el alcance declarado de Inertia v3 para Spring MVC
> y Quarkus REST; cada requisito relevante está probado contra clientes
> oficiales, el release es reproducible y los límites del producto están
> documentados sin ambigüedades.

Quarkus Reactive Routes solo entra en esa afirmación cuando cumpla su propio
gate de estabilidad. Hasta entonces seguirá siendo experimental.

No existe un «100 %» permanente: Inertia, Java y los frameworks cambian. En
este documento, 100 % significa conformidad con una instantánea congelada de
la documentación oficial v3 al comenzar el ciclo, más todos los requisitos
que el proyecto anuncie como soportados. Cada actualización importante de
Inertia abre una nueva instantánea y vuelve a medir la conformidad.

Las fuentes normativas son el [protocolo v3](https://inertiajs.com/docs/v3/core-concepts/the-protocol),
[validación](https://inertiajs.com/docs/v3/the-basics/validation),
[merge de props](https://inertiajs.com/docs/v3/data-props/merging-props),
[instant visits](https://inertiajs.com/docs/v3/the-basics/instant-visits) y
[SSR](https://inertiajs.com/docs/v3/advanced/server-side-rendering).

## Decisiones de alcance que eliminan ambigüedad

| Decisión | Regla para 1.0 | Motivo |
|---|---|---|
| Cliente Inertia v2 | No anunciarlo como soportado. El soporte será Inertia v3 hasta que exista una ruta v2 separada y E2E real. | El bootstrap v1/v2 con div[data-page] está excluido deliberadamente. |
| Spring MVC y Quarkus REST | Son los transportes estables que deben alcanzar C100, I100 y R100. | Son los transportes con mejor evidencia actual. |
| Quarkus Reactive Routes | Experimental hasta que complete M7. No se promedia con los estables para declarar 1.0. | Evita que una etiqueta «soportado» esconda riesgo de concurrencia y sesión. |
| DevTools y scaffolding | Track de madurez, no bloqueo de protocolo. Antes de lanzar, verificar interoperabilidad con la extensión oficial y redactar secretos. | Son importantes para la experiencia, pero no son una respuesta HTTP que el adaptador deba inventar. |
| API legacy | No introducir un modo legacy solo para elevar una cifra. Si se pide v2, abrir una iniciativa con compatibilidad, migración y E2E propios. | Mantiene el objetivo v3 acotado y honesto. |
| Refactorización | No extraer código al núcleo antes de fijar el comportamiento con contratos. | Evita convertir una mejora arquitectónica en una regresión de protocolo. |

## Las tres mediciones que deben llegar a 100

No se volverá a usar una única cifra como sustituto de todo lo demás.

| Indicador | Qué significa 100 % | Transporte mínimo |
|---|---|---|
| **C100 — contrato** | Cada requisito normativo de la instantánea v3 tiene estado explícito, prueba directa y ninguna excepción no declarada. | Spring MVC y Quarkus REST; Reactive cuando sea estable. |
| **I100 — interoperabilidad** | Cada escenario de usuario de la matriz E2E funciona con los clientes oficiales Vue, React y Svelte, no solo mediante peticiones HTTP fabricadas. | Las 6 combinaciones estables: 3 clientes × 2 transportes. |
| **R100 — release** | Checkout limpio reproducible, calidad y seguridad bloqueantes, artefactos verificables, documentación coherente y revisión externa. | Todo el reactor y todos los artefactos publicados. |

Un requisito que solo tiene prueba unitaria será «probado en servidor», no
«certificado». Una prueba TCK no sustituye una navegación real del cliente.
Una ejecución local antigua no sustituye una ejecución limpia en CI.

### Escalera de evidencia

La matriz generada debe utilizar estas categorías, en lugar de mezclar
«implementado» con «verificado».

| Estado | Evidencia mínima | Puede contarse para C100 | Puede contarse para I100 |
|---|---|---:|---:|
| NO_EVALUADO | No hay decisión ni prueba. | No | No |
| PARCIAL | Hay una parte funcional pero falta una semántica anunciada. | No | No |
| IMPLEMENTADO | Código y prueba unitaria directa. | No | No |
| TCK_VERIFICADO | Misma especificación ejecutada en todos los transportes objetivo. | Sí | No |
| E2E_VERIFICADO | TCK o integración más cliente oficial real en todas las celdas objetivo. | Sí | Sí |
| NO_APLICA | Justificación vinculada a la documentación oficial; no es una omisión. | Sí | Sí |

Para cada fila se guardarán: fuente oficial, versión o fecha de la
instantánea, transporte, prueba, artefacto de CI, cliente E2E aplicable,
limitación y responsable de cierre. El generador debe fallar si falta alguno
de esos campos.

## Línea base real y correcciones inmediatas

Las Fases 0–5 documentan trabajo valioso: bootstrap v3 puro, escape seguro,
CSRF uniforme, TCK, matriz YAML, seguridad base, sitio de documentación,
arquetipos y native smoke. Deben conservarse. Sin embargo, sus auditorías son
evidencia interna; no equivalen por sí solas a certificación independiente.

La auditoría comparativa más reciente sitúa el estado aproximado en 84/100:
Spring MVC 86, Quarkus REST 84 y Reactive 76. También encontró estas
contradicciones que se deben corregir antes de implementar nada nuevo:

| Hallazgo | Estado actual que no debe ocultarse | Corrección de M0 |
|---|---|---|
| Cliente 2.x | README anuncia 2.x/3.x, pero el proyecto excluye el bootstrap v1/v2. | Declarar solo v3 y enlazar a la política de soporte. |
| Errores múltiples | PROTO-053 dice TESTED, mientras APIs y mapeadores conservan un mensaje por campo. | Dividir la fila: bags básicos y múltiples mensajes; el segundo queda PARCIAL. |
| append() | PROTO-054 sigue IMPLEMENTADO y no expone la API explícita ni rutas anidadas completas. | Mantenerlo abierto; no contar para C100 ni paridad de API. |
| Instant visits | El campo sharedProps se emite, pero no existe prueba observada por cliente oficial. | Separar emisión del campo de comportamiento de cliente; el segundo queda IMPLEMENTADO. |
| Reactive | El propio README recomienda no usarlo aún en producción. | Mostrarlo como experimental en todas las tablas de soporte. |
| PIT y SCA | PIT no bloquea y el análisis de dependencias puede omitirse. | R100 queda abierto hasta convertir ambos en gates efectivos. |
| SSR remoto | La documentación pide prudencia, pero no hay prueba de rechazo temprano de destinos inseguros. | Abrir M5 como requisito de seguridad. |
| Maven Wrapper | Los workflows y algunas guías asumen mvnw, pero el checkout no lo contiene. | Añadirlo en M1 y eliminar la dependencia de Maven global. |

## Secuencia y dependencias

La siguiente secuencia permite trabajar en paralelo sin hacer una
refactorización riesgosa antes de tener pruebas suficientes.

    M0 Verdad y alcance
            |
    M1 Reproducibilidad y plataforma de evidencia
       /        |          \
    M2 Errores  M3 Merge    M5 SSR seguro
       \        |          /
             M4 E2E oficial
                    |
          M6 Núcleo de protocolo compartido
                    |
          M7 Graduación de Reactive Routes
                    |
          M8 Gates, RC y certificación 1.0

M2, M3 y M5 pueden ser ramas o pull requests distintos después de M1. M6 no
se inicia hasta que M4 haya fijado resultados observables. M7 no se declara
terminado solo porque compile: requiere tiempo de estabilización.

## M0 — Restablecer la verdad pública

- **Prioridad:** P0
- **Tamaño relativo:** S

**Objetivo:** que ninguna página, badge o matriz prometa más de lo que se
puede demostrar.

### Trabajo

1. Crear una política de compatibilidad, por ejemplo
   docs/COMPATIBILITY_POLICY.md, que defina C100, I100, R100, la instantánea
   de Inertia v3, la escalera de evidencia y el proceso de actualización.
2. Actualizar README.md, docs/NOT_SUPPORTED.md, ROADMAP.md y la portada del
   sitio de documentación:
   - reemplazar «2.x / 3.x» por la versión v3 validada;
   - Escalera de evidencia
   - no presentar 58/59 como certificación externa;
   - enlazar a la matriz generada y a las limitaciones.
3. Evolucionar specs/inertia-v3-compliance.yaml y su generador:
   - dividir PROTO-053 en bags/redirect y múltiples mensajes por campo;
   - dividir PROTO-054 entre wire metadata existente y ergonomía append
     pendiente;
   - dividir PROTO-057 entre serialización server-side y comportamiento de
     instant visit en cliente;
   - registrar cliente, transporte y URL de evidencia por fila.
4. Añadir una instantánea versionada de requisitos, por ejemplo
   specs/inertia-v3-baseline-YYYY-MM-DD.yaml. Cada nueva capacidad oficial
   comienza como NO_EVALUADO, nunca como soportada por inferencia.
5. Crear issues o tarjetas P100-01 a P100-18 usando los identificadores de
   este plan. Cada tarjeta tendrá hipótesis, superficie API, casos de prueba,
   riesgo de compatibilidad y criterio de aceptación.

### Criterio de salida

- No queda mención de soporte v2 sin una prueba E2E v2 enlazada.
- La matriz ya no marca como totalmente probados los errores múltiples ni
  instant visits.
- Toda fila tiene estado explícito y una fuente oficial o justificación de
  NO_APLICA.
- Un lector externo puede distinguir contrato, E2E y calidad de release en
  menos de cinco minutos.

## M1 — Reproducibilidad y plataforma de evidencia

- **Prioridad:** P0
- **Tamaño relativo:** M
- **Dependencia:** M0

**Objetivo:** que cualquier persona pueda producir el mismo resultado desde
un checkout limpio, en Windows y Linux, sin instalar Maven manualmente.

### P100-02 — Maven Wrapper y versiones bloqueadas

1. Añadir .mvn/wrapper, mvnw y mvnw.cmd, con distribución de Maven fijada y
   checksum verificable.
2. Cambiar todos los workflows de mvn a ./mvnw o ./mvnw.cmd según plataforma.
   Corregir en particular el desfase entre native-tests.yml, que ya presupone
   el wrapper, y el checkout actual, que no lo tiene.
3. Declarar la versión mínima de Java, Maven y Node en:
   - Maven Enforcer y, si hace falta, toolchains;
   - package.json engines y un archivo de versión de Node;
   - README y CONTRIBUTING.
4. Definir comandos únicos y documentados:

       npm ci
       npm run verify:metadata
       ./mvnw -B clean test
       ./mvnw -B verify -Pquality-gates
       npm run test:e2e

   Si se crea npm run verify:all, debe invocar esos pasos y propagar el
   primer fallo. No debe ocultar tests detrás de flags de skip.
5. Añadir CI de checkout limpio en Windows y Ubuntu. La prueba debe usar
   solo herramientas versionadas por el repo; la caché puede acelerar, pero
   no ser requisito de corrección.

### P100-03 — Evidencia de prueba trazable

1. Guardar Surefire, Failsafe, Playwright, matriz generada, SBOM y reportes
   de análisis como artefactos de CI cuando fallen y como resumen enlazable
   para releases.
2. Separar perfiles rápidos de PR, completos de main y estrés nocturno,
   sin dejar que un perfil rápido sea la única evidencia de release.
3. Crear specs/e2e-compliance.yaml para mapear cada escenario E2E a:
   cliente, transporte, demo/fixture, prueba Playwright y estado.
4. Hacer que el generador rechace referencias a clases inexistentes, suites
   no ejecutadas o escenarios que no tengan celda de transporte.

### Criterio de salida de M1

- Un clone nuevo en Windows y Ubuntu ejecuta la suite de unidad e integración
  mediante el wrapper.
- CI no depende de Maven instalado en la imagen.
- Toda afirmación de pruebas tiene un artefacto o job de CI identificable.
- El resultado de la auditoría deja de depender de informes Surefire viejos.

## M2 — Cerrar la brecha P1 de validación

- **Prioridad:** P0
- **Tamaño relativo:** L
- **Dependencia:** M1

**Objetivo:** soportar de forma deliberada uno o varios mensajes por campo,
en error bag por defecto, bags nombrados y Precognition.

### Diseño antes de tocar APIs

Java no permite sobrecargar dos métodos que solo difieren en
Map<String, String> frente a Map<String, List<String>>, porque ambos se
borran a Map. La solución debe aprobarse mediante un ADR corto y una prueba
de compatibilidad binaria antes de implementarse.

La opción recomendada es introducir un valor inmutable común,
ValidationErrors, que preserve el orden y permita valores String o
List<String>. Mantener los métodos existentes para el caso simple y añadir
una entrada no ambigua, por ejemplo withValidationErrors(ValidationErrors) o
withErrorMessages(Map<String, ? extends Collection<String>>). No cambiar
silenciosamente la semántica de un método público sin pasar japicmp y pruebas
de compilación de consumidores.

La configuración puede exponer inertia.validation.all-errors, inicialmente
false para conservar el wire actual. Al activarla, el resultado debe ser
arreglo de mensajes sin perder el caso de un mensaje ni los bags anidados.
La documentación debe explicar ambas formas.

### Superficies a modificar

- Spring: api/InertiaResponse.java, api/InertiaRender.java,
  api/InertiaRedirect.java, validation/PrecognitionHandler.java,
  protocol/PageObjectBuilder.java, mvc/InertiaFilter.java y sus tests.
- Quarkus: api/InertiaResponse.java, api/InertiaRender.java,
  api/InertiaRedirect.java, protocol/PrecognitionExceptionMapper.java,
  protocol/PageObjectBuilder.java, vertx/InertiaVertxHandler.java y rutas
  Reactive.
- Núcleo: nuevo modelo ValidationErrors y serialización sin dependencia de
  Spring ni Quarkus.

### Casos obligatorios

| Caso | Spring MVC | Quarkus REST | Quarkus Reactive | Cliente E2E |
|---|---:|---:|---:|---:|
| Un mensaje por campo, modo legado | Sí | Sí | Sí | Sí |
| Dos mensajes ordenados por campo | Sí | Sí | Sí | Sí |
| Bag por defecto | Sí | Sí | Sí | Sí |
| Bag nombrado con dos mensajes | Sí | Sí | Sí | Sí |
| Redirect 303 y flash, seguida de GET | Sí | Sí | Sí | Sí |
| Precognition 422 | Sí | Sí | Sí | Sí |
| Filtro validate-only | Sí | Sí | Sí | Sí |
| Locale no inglés | Sí | Sí | Sí | Sí |

### Criterio de salida de M2

- No existe put o putIfAbsent que descarte mensajes por accidente.
- El TCK verifica la estructura completa, no solo la existencia de una llave.
- Las tres rutas producen exactamente la misma forma JSON para cada modo.
- La matriz divide y marca E2E_VERIFICADO los requisitos solo después de
  pasar las seis combinaciones estables de M4.

## M3 — API de merge con append y rutas anidadas

- **Prioridad:** P1
- **Tamaño relativo:** M
- **Dependencia:** M1

**Objetivo:** ofrecer una API Java legible equivalente en intención a
append/prepend de los adaptadores de referencia, sin romper la API merge
existente.

### Diseño recomendado

1. Modelar en inertia-core un MergePlan inmutable: valor, operaciones,
   rutas, matchOn y modo profundo. El modelo no conoce HttpServlet, Vert.x ni
   sesión.
2. Añadir una API nueva, no ambigua, que permita expresar una operación por
   ruta. Un ejemplo deseado es:

       mergeable(posts)
           .append("data")
           .prepend("pinned")
           .matchOn("data.id")

   La fachada Spring y la de Quarkus pueden ofrecer sintaxis idiomática
   equivalente, pero deben producir el mismo MergePlan y el mismo PageObject.
3. Conservar merge(key, value, rule, matchOn) durante 0.x; deprecar solo si
   existe una ruta migratoria y japicmp lo confirma.
4. Definir reglas para rutas duplicadas, rutas inválidas, orden de
   append/prepend, matchOn múltiple, deep merge y X-Inertia-Reset. No dejar
   que cada adaptador las interprete por separado.

### Casos obligatorios

- posts.data append y posts.pinned prepend en el mismo prop compuesto;
- matchOn en data.id y varios matchOn;
- merge, prepend y deep merge combinados;
- solo recarga parcial y reset selectivo de una ruta hija;
- metadata de scroll e intención prepend;
- serialización exacta de mergeProps, prependProps, deepMergeProps y
  matchPropsOn;
- interacción con deferred, once y props vacíos;
- navegación de cliente oficial que demuestre el resultado visual, no solo
  las listas de metadata.

### Criterio de salida de M3

- Existe append explícito documentado y probado, incluidas rutas anidadas.
- El TCK de merge corre en los tres transportes.
- La prueba E2E de M4 verifica que Vue, React y Svelte interpretan la
  metadata del mismo modo.
- PROTO-054 solo se convierte en E2E_VERIFICADO cuando se cubran sus dos
  partes: wire format y ergonomía pública anunciada.

## M4 — Interoperabilidad E2E con clientes oficiales

- **Prioridad:** P0
- **Tamaño relativo:** XL
- **Dependencia:** M2 y M3; M5 para los escenarios SSR

**Objetivo:** pasar de peticiones API correctas a aplicaciones reales que
navegan, hidratan y actualizan estado con los clientes oficiales.

La suite actual de Playwright valida principalmente HTML, JSON, una recarga
parcial y versionado para Spring y Quarkus. Es una buena base, pero no
demuestra la matriz completa.

### Matriz que se debe crear

| Grupo | Clientes | Transportes | Uso en CI |
|---|---|---|---|
| Estable obligatorio | Vue, React, Svelte | Spring MVC, Quarkus REST | PR relevante, main y release |
| Reactive de estabilización | Vue, React, Svelte | Quarkus Reactive | main y nightly; release cuando M7 lo permita |
| SSR | Vue, React, Svelte donde el SSR sea soportado por la fixture | Spring MVC, Quarkus REST, Reactive | main y release |

No hace falta crear nueve aplicaciones de negocio distintas. Se recomienda
una fixture mínima por cliente y rutas de kitchen sink compartidas, con
semántica idéntica. Las fixtures deben depender de versiones bloqueadas de
@inertiajs/vue3, @inertiajs/react y @inertiajs/svelte.

### Escenarios E2E mínimos

| Área | Aserción observada por navegador |
|---|---|
| Bootstrap | El script JSON v3 se parsea una vez, la aplicación monta y no hay errores de consola. |
| Navegación y versión | Visita Inertia, Vary, 409 por versión obsoleta y reload correcto. |
| Parciales | only, except, rutas con punto, always y reset actualizan solo el estado esperado. |
| Formularios | 303, flash, bag por defecto, bag nombrado, múltiples errores y Precognition. |
| Props modernas | Deferred, once, rescue, merge, append, prepend, deep merge, matchOn y scroll. |
| Instant visits | sharedProps persiste, se actualiza, se excluye cuando corresponde y resuelve colisiones. |
| Redirects | GET/POST, URL externa y fragmentos preservados. |
| Upload | Multipart real desde input file y manejo de error. |
| SSR | HTML renderizado por sidecar, hidratación sin mismatch y fallback CSR cuando el sidecar cae. |
| Seguridad visible | No hay breakout de script, no se filtran detalles SSR y CSRF falla con el contrato documentado. |

### Reglas contra falsos verdes

1. Una prueba que construye cabeceras manualmente cuenta para C100, no para
   I100.
2. Una captura de pantalla no basta; la prueba debe comprobar estado DOM,
   respuesta relevante y ausencia de errores de consola.
3. Los datos de cada fixture deben ser deterministas. No usar sleeps fijos
   donde se pueda esperar una respuesta, selector o evento.
4. Publicar trace, vídeo y log de aplicación cuando falle Playwright.
5. Añadir una prueba negativa por familia de riesgo: error de validación,
   sidecar caído, token CSRF inválido, versión desfasada y reset de metadata.

### Criterio de salida de M4

- Las seis celdas estables pasan todos los escenarios aplicables.
- Cada fila de la matriz de contrato enlaza, cuando corresponde, a una celda
  E2E real.
- sharedProps/instant visits deja de ser una afirmación server-side sin
  observación cliente.
- El reporte de release muestra cobertura por cliente y transporte, no un
  único «E2E verde».

## M5 — SSR remoto seguro y verificable

- **Prioridad:** P1 de seguridad
- **Tamaño relativo:** M
- **Dependencia:** M1

**Objetivo:** transformar la recomendación documental sobre SSR remoto en un
contrato validado al inicio de la aplicación.

### Política recomendada

1. Por defecto, permitir únicamente el sidecar local: localhost, 127.0.0.1
   y ::1, en HTTP local, con endpoint explícito.
2. Un destino remoto requiere simultáneamente:
   - inertia.ssr.remote-enabled=true;
   - URL HTTPS;
   - host presente en inertia.ssr.allowed-hosts;
   - ausencia de userinfo y fragmento;
   - cliente HTTP sin redirecciones automáticas hacia hosts no validados.
3. Rechazar en el arranque, con mensaje seguro, esquemas no HTTP(S), host
   vacío, metadata IP, rangos no permitidos, URL con credenciales y remoto
   sin allowlist.
4. Registrar solo esquema, host normalizado y puerto; nunca tokens,
   credenciales ni payload de página.
5. Mantener timeout, circuit breaker, límite de tamaño y fallback CSR; la
   mejora de validación no debe convertir una caída normal del sidecar en 500.

### Implementación

- Crear una política pura SsrEndpointPolicy en inertia-core.
- Invocarla desde el validador de propiedades Spring y la configuración
  Quarkus antes de crear SsrClient o SsrHandler.
- Añadir pruebas de URI, pruebas de integración de arranque en ambos
  frameworks y sidecar HTTP controlado para timeout, respuesta inválida,
  caída y redirección.
- Documentar configuración local, remota segura y frontera de confianza en
  docs/ssr-setup.md y docs/configuration.md.

### Criterio de salida de M5

- Una URL remota insegura no puede iniciar la aplicación.
- Los casos locales válidos siguen funcionando sin configuración adicional.
- Las pruebas cubren Spring, Quarkus REST y Reactive.
- M4 verifica SSR real y fallback CSR con el mismo contrato.

## M6 — Reducir deriva: núcleo de decisiones de protocolo

- **Prioridad:** P1
- **Tamaño relativo:** L
- **Dependencia:** M4

**Objetivo:** disminuir la duplicación que hoy hace que Spring y Quarkus
puedan divergir al evolucionar Inertia.

No se trata de mover archivos por estética. Se extraen únicamente decisiones
puras que hoy aparecen dos veces, después de haberlas bloqueado con TCK y
E2E.

### Orden seguro

1. Añadir pruebas de caracterización para cada respuesta actual de las
   suites TCK: page object, headers, status, Vary, redirect, flash,
   selección parcial y metadata.
2. Extraer a inertia-core modelos sin dependencia de frameworks:
   RequestIntent, PartialSelection, PageAssembly, RedirectDecision,
   MergePlan, ErrorPayload y SsrEndpointPolicy.
3. Mantener en los adaptadores solo:
   - conversión de request/response;
   - sesión, flash y seguridad propios del framework;
   - serialización e integración de plantillas;
   - hooks públicos específicos.
4. Migrar una familia por pull request pequeño: primero selección parcial y
   metadata, luego redirects, luego ensamblado de página. No combinar una
   migración con cambios de semántica.
5. Ejecutar japicmp y una prueba de compilación de consumidores en cada
   extracción de API pública.

### Criterio de salida de M6

- Las decisiones de protocolo tienen una fuente de comportamiento en
  inertia-core y pruebas comunes.
- Spring, Quarkus REST y Reactive producen las mismas salidas para el TCK.
- No hay cambio de salida no justificado por una nueva fila de especificación.
- La reducción de líneas duplicadas es una consecuencia medible, no el único
  criterio de éxito.

## M7 — Graduar o retirar Quarkus Reactive Routes

- **Prioridad:** P1
- **Tamaño relativo:** L y dependiente del tiempo
- **Dependencia:** M4, M5 y M6

**Objetivo:** tomar una decisión basada en evidencia: estable o experimental,
no una etiqueta optimista.

### Suite de estabilización

1. Crear un perfil reactive-stress ejecutado en nightly y bajo demanda.
   Debe repetir de manera concurrente y determinista:
   - sesiones y flash aislados;
   - CSRF válido e inválido;
   - redirects 303 y 409;
   - errores por bag y Precognition;
   - once, deferred, partial y reset;
   - SSR fallback;
   - requests simultáneos con identidades distintas.
2. Ejecutar las tres fixtures oficiales de M4 contra Reactive.
3. Capturar thread dump, logs de contexto y reportes de fallo sin datos
   sensibles.
4. Mantener una matriz de diferencias por transporte. Toda diferencia debe
   ser una fila NO_APLICA justificada o un bug abierto; no una nota escondida
   en documentación.

### Gate de graduación

Reactive pasa a estable solo si se cumplen todos:

- 30 días continuos de nightly verde después del último cambio de contexto,
  sesión o CSRF;
- dos releases candidate consecutivas sin regresión Reactive;
- 100 % de TCK y de las tres fixtures E2E aplicables;
- cero defectos P0/P1 abiertos específicos de Reactive;
- revisión de concurrencia por una persona que no escribió el cambio;
- documentación de operación no recomienda ya evitarlo en producción.

Si el gate no se cumple, el resultado correcto es mantenerlo experimental.
Eso no bloquea 1.0 de Spring MVC y Quarkus REST, pero sí bloquea afirmar
«tres transportes estables».

## M8 — Gates efectivos, release candidate y certificación

- **Prioridad:** P0
- **Tamaño relativo:** L
- **Dependencia:** M2–M7 según el alcance de lanzamiento

**Objetivo:** que una etiqueta 1.0 signifique que los mecanismos de calidad y
seguridad realmente pudieron impedir un release defectuoso.

### P100-14 — Calidad

1. Mantener Checkstyle y SpotBugs bloqueantes; cualquier excepción debe tener
   ticket, fecha de expiración y justificación.
2. Corregir el problema de PIT antes de afirmar que existe mutation testing:
   - reproducirlo en un proyecto mínimo y adjuntar diagnóstico;
   - probar una versión soportada de PIT y una configuración aislada de
     argLine/JDK en Linux;
   - cuando haya una ejecución real, eliminar continue-on-error y los skips
     de release.
3. Usar umbrales progresivos, no un salto ficticio a 100 % de cobertura:
   - primero, no permitir caer por debajo de la línea base medida;
   - objetivo de 1.0 para inertia-core y lógica de protocolo: 85 % lineal,
     75 % de ramas y 70 % de mutación;
   - objetivo de 1.0 para adaptadores: 75 % lineal, 65 % de ramas y 60 % de
     mutación;
   - cualquier módulo nuevo debe cumplir su umbral desde el primer merge.
4. Publicar los reportes; una cifra sin HTML/XML verificable no cuenta.

### P100-15 — SCA, SBOM y cadena de suministro

1. Elegir una fuente de vulnerabilidades autenticada y disponible para CI.
   Si Dependency Check requiere NVD_API_KEY, el job de release debe fallar
   cerrado cuando falte; no puede quedar silenciosamente omitido.
2. Ejecutar revisión de dependencias en pull requests y análisis completo en
   main, candidate y tag.
3. Generar SBOM CycloneDX por release, adjuntarlo a GitHub Release y
   escanear el resultado.
4. Bloquear vulnerabilidades críticas y altas sin excepción documentada; las
   excepciones deben tener propietario y fecha de vencimiento.
5. Mantener firma GPG, hashes, provenance y verificación de compatibilidad
   binaria como gates, no como pasos informativos.

### P100-16 — Candidate independiente

Antes de v1.0.0:

1. Construir una RC desde un clone nuevo en Windows y Linux, con el wrapper.
2. Ejecutar el reactor completo, las seis celdas E2E estables, SSR, native
   smoke, arquetipos y análisis de seguridad.
3. Publicar una RC draft en Maven Central con firma, fuentes, javadocs,
   SBOM y hashes.
4. Pedir revisión a una persona externa al cambio. Debe revisar especialmente
   C100, las APIs de errores/merge, SSR y los resultados Reactive.
5. Resolver o aceptar formalmente todos los hallazgos. No usar «no hubo
   revisor disponible» como cierre automático.
6. Comparar API pública contra la versión previa con japicmp y publicar
   migration notes.

### Definición de listo para 1.0

La versión 1.0.0 se puede etiquetar solo cuando se cumplan todas estas
condiciones:

- C100 y I100 son 100 % para Spring MVC y Quarkus REST.
- No hay requisitos PARCIAL, IMPLEMENTADO o NO_EVALUADO dentro del alcance
  anunciado.
- Errores múltiples, append/rutas anidadas e instant visits tienen pruebas
  E2E oficiales.
- M5 está cerrado y su política SSR se verifica en ambos frameworks.
- R100 está verde: wrapper, calidad, SCA, SBOM, firmas, compatibilidad,
  native smoke, arquetipos y artefactos de CI.
- README, matriz y sitio de docs dicen exactamente lo mismo.
- Existe revisión externa documentada y no quedan defectos P0/P1 abiertos.
- Reactive solo se anuncia estable si completó M7; si no, aparece
  explícitamente como experimental.

## Track de madurez: paridad de experiencia, no condición de protocolo

Este track puede correr en paralelo después de M4. Mejora la adopción frente a
Laravel y Rails, pero no debe retrasar la corrección de P0/P1.

| Tema | Acción | Gate |
|---|---|---|
| DevTools | Verificar la extensión oficial con las fixtures v3. Si no basta, implementar solo el protocolo documentado de recorder, con redacción de cookies, tokens y props sensibles. | Demostración E2E y prueba de redacción. |
| Observabilidad | Unificar métricas Micrometer para visita, partial, SSR fallback, tiempo de render y error de protocolo; documentar cardinalidad y PII. | Dashboard de ejemplo sin secretos. |
| Generación | Mantener los seis arquetipos y añadir smoke de actualización de dependencias y ejemplos completos. | Cada arquetipo genera, compila y pasa una visita Inertia real. |
| Guías | Añadir decisiones de arquitectura, migraciones y recetas de errores/SSR/Reactive. | Una persona nueva completa el quickstart en un checkout limpio. |
| Rendimiento | Repetir JMH y pruebas de carga solo después de fijar semántica; no usar un benchmark para ocultar un fallo de compatibilidad. | Línea base publicada y sin regresión acordada. |

## Registro de riesgos y respuestas

| Riesgo | Señal temprana | Respuesta |
|---|---|---|
| Cambio incompatible de API de errores | japicmp o consumidor de muestra falla. | ADR, nueva API con nombre distinto y migration guide; no sobrecargar Map por erasure. |
| E2E inestable | Fallos intermitentes o sleeps largos. | Fixtures deterministas, espera por eventos, trace y cuarentena temporal con ticket; no ignorar el fallo en release. |
| PIT sigue sin ejecutarse | Ningún mutante llega a correr. | Repro mínimo, actualización/aislamiento y gate alterno real; no llamarlo quality gate hasta que falle correctamente. |
| SCA depende de un servicio externo | Job omitido, 401 o base sin actualizar. | Credencial protegida, fuente alternativa aprobada y fail-closed en candidate/tag. |
| Refactor altera respuestas | Snapshot/TCK cambia sin nueva especificación. | Migraciones pequeñas, pruebas de caracterización y revisión de diff de wire format. |
| SSRF o DNS inseguro | URL remota sin allowlist, redirect o credenciales. | SsrEndpointPolicy central, validación temprana y cliente sin redirects no validados. |
| Reactive contamina contextos | Fallos solo bajo carga o entre sesiones. | Stress nightly, logs seguros, aislamiento de contexto y gate de 30 días. |
| Deriva upstream | Nueva guía de Inertia sin fila en la matriz. | Revisión programada por release upstream y nuevo requisito NO_EVALUADO por defecto. |

## Cadencia de ejecución y tablero

Cada pull request debe llevar un identificador P100 y completar esta tabla en
su issue o descripción. Esto evita repetir la situación de fases «100 %
completas» con trabajo aún pendiente.

| Campo | Requerido para cerrar |
|---|---|
| Identificador y alcance | P100-xx y requisito exacto de la instantánea. |
| Cambio de API | Sí/no; ADR y resultado de japicmp cuando aplique. |
| Pruebas | Unitarias, integración, TCK y celdas E2E afectadas. |
| Evidencia | Enlace al commit, job CI y artefactos. |
| Documentación | README, matriz generada, NOT_SUPPORTED y migración actualizados. |
| Riesgo | Seguridad, concurrencia, wire format o compatibilidad. |
| Revisión | Autor distinto del revisor para P0/P1. |

### Backlog canónico

| ID | Entregable | Hito | Prioridad |
|---|---|---|---|
| P100-01 | Política de soporte, instantánea v3 y matriz sin sobreafirmaciones. | M0 | P0 |
| P100-02 | Maven Wrapper, versiones bloqueadas y clones limpios Windows/Linux. | M1 | P0 |
| P100-03 | Modelo de evidencia, matriz E2E generada y artefactos CI. | M1 | P0 |
| P100-04 | ADR y modelo común ValidationErrors. | M2 | P0 |
| P100-05 | Errores múltiples en Spring, Quarkus REST/Reactive, TCK y migración. | M2 | P0 |
| P100-06 | ADR, MergePlan y API append/rutas anidadas. | M3 | P1 |
| P100-07 | TCK, documentación y regresiones de merge/reset/scroll. | M3 | P1 |
| P100-08 | Fixtures oficiales y navegación E2E base para las seis celdas estables. | M4 | P0 |
| P100-09 | E2E avanzado: formularios, instant visits, upload, props modernas y SSR. | M4 | P0 |
| P100-10 | SsrEndpointPolicy y validación de arranque segura. | M5 | P1 |
| P100-11 | Sidecar controlado, pruebas SSR/fallback y documentación de frontera. | M5 | P1 |
| P100-12 | Pruebas de caracterización de respuestas y decisiones de protocolo. | M6 | P1 |
| P100-13 | Extracción incremental de decisiones puras a inertia-core. | M6 | P1 |
| P100-14 | PIT real, umbrales progresivos y calidad bloqueante. | M8 | P0 |
| P100-15 | SCA fail-closed, SBOM y cadena de suministro verificable. | M8 | P0 |
| P100-16 | RC reproducible, revisión externa y candidatura 1.0. | M8 | P0 |
| P100-17 | Stress, paridad y observabilidad de Quarkus Reactive. | M7 | P1 |
| P100-18 | Decisión formal de graduar o mantener Reactive experimental. | M7 | P1 |

### Primer bloque de trabajo, en orden

1. Abrir P100-01: ejecutar M0 y corregir las afirmaciones de README/matriz.
2. Abrir P100-02: introducir Maven Wrapper, versiones bloqueadas y el job
   limpio Windows/Linux.
3. Abrir P100-03: crear la matriz E2E generada y sus fixtures mínimas.
4. Abrir P100-04: ADR y modelo ValidationErrors; después implementar M2.
5. Abrir P100-06: ADR/API MergePlan y TCK de M3.
6. Abrir P100-10: política SSR y pruebas de rechazo de M5.
7. Completar P100-05, P100-07 y P100-11; después ampliar M4 hasta completar las seis
   celdas estables.
8. Solo entonces abrir P100-12/P100-13, P100-17/P100-18 y el candidate M8.

## Qué no se debe volver a hacer

- No declarar una fase completa porque el código existe si falta una prueba de
  cliente, una configuración segura o un gate que realmente falle.
- No marcar una fila como TESTED cuando su nota describe una limitación que
  contradice el título.
- No usar un modo legacy v2 como atajo para afirmar más compatibilidad de la
  que se puede mantener.
- No hacer una gran reescritura Spring/Quarkus antes de tener contratos de
  respuesta bloqueados.
- No omitir PIT, SCA o E2E en un tag de release y después describirlos como
  bloqueantes.

## Resultado esperado por etapa

| Después de | Cambio visible para usuarios y mantenedores |
|---|---|
| M0 | La documentación deja de sobreprometer. |
| M1 | Cualquier persona reproduce pruebas con un checkout nuevo. |
| M2 | Formularios Java pueden entregar todos los errores que Inertia admite. |
| M3 | Merge tiene una API Java moderna y rutas anidadas claras. |
| M4 | La compatibilidad se observa en Vue, React y Svelte reales. |
| M5 | SSR remoto no queda abierto a configuración insegura por accidente. |
| M6 | Un cambio de protocolo se implementa una vez, no en tres sitios. |
| M7 | Reactive tiene evidencia de estabilidad o una etiqueta experimental honesta. |
| M8 | 1.0 representa una release auditable, reproducible y sostenible. |

---

Este plan reemplaza los planes v2–v4 como plan activo de ejecución; no borra
su valor histórico. Las Fases 0–5 quedan como evidencia de trabajo realizado,
pero solo las pruebas limpias, las matrices actualizadas y los gates de este
documento permiten cerrar un requisito hacia C100, I100 o R100.
