# Auditoría comparativa — Inertia.js Java (Spring Boot y Quarkus)

**Fecha:** 2026-09-23  
**Repositorio auditado:** `diovamny/inertiajs-java`, checkout local `v0.0.4-7-gf25fb5a` (`f25fb5a`, 2026-09-22)  
**Referencias locales:** `inertiajs/inertia-laravel` rama `3.x` (CHANGELOG hasta `v3.3.2`) e `inertiajs/inertia-rails` `v3.22.0`.  
**Estándar:** [documentación y protocolo Inertia v3](https://inertiajs.com/docs/v3/core-concepts/the-protocol), contrastado con las implementaciones locales de Laravel y Rails.

## Veredicto

No cumple el **100%** de Inertia como producto ni como adaptador de referencia. Sí implementa de manera extensa el protocolo HTTP v3 y es un candidato serio para aplicaciones Spring MVC y Quarkus REST, pero aún es una versión `0.0.4`, con algunas brechas de compatibilidad, validación y madurez operativa que impiden certificarlo como totalmente equivalente a Laravel o Rails.

La cifra interna de **58/59 requisitos verificados (98,3%)** debe leerse como cobertura de su propia matriz, no como certificación independiente de compatibilidad total. La matriz reconoce una fila sin prueba dedicada y otras limitaciones que reducen la paridad real. Véanse [docs/protocol-compatibility.md](docs/protocol-compatibility.md) y [docs/NOT_SUPPORTED.md](docs/NOT_SUPPORTED.md).

### Puntuación comparativa

| Adaptador / transporte | Protocolo v3 | Paridad funcional | Seguridad | Pruebas y operación | Ecosistema y madurez | Nota global |
|---|---:|---:|---:|---:|---:|---:|
| Laravel 3.x (referencia) | 98 | 98 | 95 | 94 | 97 | **96/100** |
| Rails 3.22 | 96 | 95 | 94 | 92 | 95 | **94/100** |
| Java — Spring MVC | 94 | 82 | 88 | 78 | 82 | **86/100** |
| Java — Quarkus REST | 93 | 81 | 88 | 78 | 80 | **84/100** |
| Java — Quarkus Reactive Routes | 88 | 81 | 86 | 72 | 76 | **76/100** |

Estas notas no son un benchmark de rendimiento ni una certificación de seguridad; ponderan la evidencia de código, pruebas, documentación y facilidad de adopción disponible en los tres checkouts. El promedio recomendado para comunicar el proyecto es **84/100: beta avanzada / release candidate técnica**, no “100% compatible”.

## Lo que está bien hecho

- El contrato esencial está muy bien cubierto: HTML inicial con JSON seguro, `X-Inertia`, `Vary`, páginas JSON, recarga parcial (`only`, `except`, rutas con puntos), versionado y `409`, `302/303`, redirecciones externas, flash, errores, props `always`, opcionales, diferidas, `once`, merge/deep-merge/prepend, scroll e historial. Esto coincide con el núcleo que define el [protocolo oficial](https://inertiajs.com/docs/v3/core-concepts/the-protocol).
- Los dos adaptadores comparten un modelo de página explícito en `inertia-core`, serialización segura y guardia de tamaño. El escape de `<`, `>`, `&`, `/`, U+2028 y U+2029 en [SafeJsonEncoder.java](inertia-core/src/main/java/io/github/diovamny/inertia/core/security/SafeJsonEncoder.java) es una defensa correcta para JSON embebido en `<script>`; el protocolo exige escape de `/` y prohíbe HTML-entity encoding.
- La implementación respeta controles v3 importantes: el `409` de versionado lleva `X-Inertia-Location` y `X-Inertia-Version`, sin convertirlo en página Inertia; el protocolo exige precisamente ese comportamiento para un `GET` desfasado.
- CSRF, cookies XSRF, modos de seguridad, límites de tamaño de página, validación de destinos de redirect, cabeceras `Vary`, SSR con timeout/circuit breaker y Micrometer dan una postura defensiva superior a la de muchos adaptadores comunitarios. Destacan [InertiaConfigValidator.java](spring-inertia/src/main/java/io/github/diovamny/spring/inertia/config/InertiaConfigValidator.java) y su equivalente de Quarkus.
- Hay buenas herramientas de adopción: seis arquetipos (Spring/Quarkus × Vue/React/Svelte), ejemplos, DSL de pruebas, TCK propio, compatibilidad con imagen nativa y documentación de SSR. Esto es una ventaja práctica frente a iniciar un adaptador Java desde cero.
- Los chequeos de integridad que sí se pudieron ejecutar en este entorno pasaron: la matriz resolvió **112 referencias** a clases de prueba y el chequeo de versiones confirmó `0.0.4`. Además, hay **104 informes Surefire** almacenados, con **616 pruebas** registradas y sin coincidencias de `failures`/`errors`; los más recientes datan de 2026-09-22. No los presento como ejecución fresca: Maven no está disponible en el `PATH` y el repositorio no contiene Maven Wrapper.

## Brechas y críticas priorizadas

### P1 — No anunciar soporte de cliente Inertia 2.x

El README anuncia `Inertia.js client 2.x / 3.x` en [README.md](README.md), pero la misma distribución declara deliberadamente no soportar la plantilla raíz v1/v2 (`<div data-page>`), en [docs/NOT_SUPPORTED.md](docs/NOT_SUPPORTED.md). El [upgrade oficial a v3](https://inertiajs.com/docs/v3/getting-started/upgrade-guide) confirma que el JSON inicial pasa a ser siempre un `<script type="application/json">` y que el enfoque legado `data-page` deja de estar soportado. La migración no es transparente para clientes v2.

**Impacto:** una aplicación que conserve el bootstrap v2 puede no iniciar aunque Maven resuelva el artefacto.  
**Corrección:** retirar `2.x` de la tabla hasta añadir un modo legado probado end-to-end, o declarar con precisión la versión mínima de `@inertiajs/*` v2 que sea compatible y demostrarla en CI.

### P1 — Errores múltiples por campo no están soportados, aunque la matriz dice que sí

La documentación oficial permite que cada campo devuelva un arreglo de mensajes al activar `withAllErrors`; véase [Validation: Multiple Errors Per Field](https://inertiajs.com/docs/v3/the-basics/validation). Laravel lo implementa con `protected $withAllErrors` en su middleware local y Rails mantiene el error como estructura Ruby serializable.

En Java, las APIs públicas restringen los errores a `Map<String, String>` ([Spring `InertiaRedirect`](spring-inertia/src/main/java/io/github/diovamny/spring/inertia/api/InertiaRedirect.java), [Quarkus `InertiaRedirect`](quarkus-inertia/src/main/java/io/github/diovamny/quarkus/inertia/api/InertiaRedirect.java)). Los mapeadores usan `putIfAbsent` o `put`, por lo que retienen solo un mensaje por campo: [PrecognitionHandler.java](spring-inertia/src/main/java/io/github/diovamny/spring/inertia/validation/PrecognitionHandler.java), [PrecognitionExceptionMapper.java](quarkus-inertia/src/main/java/io/github/diovamny/quarkus/inertia/protocol/PrecognitionExceptionMapper.java).

La fila 53 de [docs/protocol-compatibility.md](docs/protocol-compatibility.md) se marca `TESTED` para “multiple messages per field”, pero su propia nota dice “one message per field”; es una contradicción de documentación y una sobreafirmación.

**Corrección:** aceptar `Map<String, ?>`/`Map<String, List<String>>`, añadir `inertia.validation.all-errors` (por defecto `false` para mantener compatibilidad), preservar arreglos en flash, error bags y Precognition, y añadir contratos Spring, Quarkus REST y Reactive.

### P1 — Quarkus Reactive Routes no está listo para ser recomendado en producción

El proyecto avisa explícitamente que Reactive Routes está “stabilizing” y recomienda Spring MVC o Quarkus REST mientras madura ([README.md](README.md), [docs/NOT_SUPPORTED.md](docs/NOT_SUPPORTED.md)). Es una comunicación honesta, pero debe pesar en la nota y en la guía de selección.

**Riesgo:** el transporte reactivo tiene contexto de petición, sesión, filtros, CSRF y redirects distintos a JAX-RS; son zonas propensas a regresiones de concurrencia.  
**Corrección:** mantenerlo experimental, ejecutar pruebas de estrés repetibles en CI, publicar una matriz separada por transporte y exigir al menos un ciclo de release sin regresiones antes de llamarlo estable.

### P1 — Los quality gates no respaldan aún una afirmación fuerte de calidad

Los umbrales de PIT son muy bajos: mutación `5%` y cobertura `20%` en [pom.xml](pom.xml). Además, PIT es no bloqueante en release porque aborta en JDK 21; véase [.github/workflows/release.yml](.github/workflows/release.yml). Esto es comprensible como contingencia, pero no debe describirse como un gate efectivo.

El análisis de vulnerabilidades también es condicional a que exista `NVD_API_KEY`; si falta, una release puede continuar sin OWASP Dependency Check, pese al comentario de que no debe omitirse. En CI normal se omite expresamente. El log local [quality-gates.log](quality-gates.log) documenta además un fallo anterior del análisis por `401` de OSS Index.

**Corrección:** actualizar/aislar PIT hasta volverlo bloqueante, elevar umbrales gradualmente por módulo, ejecutar SCA en PR y release con una fuente autenticada fiable, fallar cerrado si la exploración requerida no pudo ejecutarse, y publicar el SBOM y resultados de scan como artefactos verificables.

### P2 — `append()` no tiene la ergonomía ni la expresividad de Laravel/Rails

Laravel implementa `append()` y rutas anidadas en [MergesProps.php](C:/Users/DiovamnyGarciaPeña/Desktop/inertiajs-laravel/src/MergesProps.php); Rails ofrece `append`/`prepend` por ruta en [prop_mergeable.rb](C:/Users/DiovamnyGarciaPeña/Desktop/inertiajs-rails/lib/inertia_rails/prop_mergeable.rb). Inertia Java solo expone `merge(..., MERGE|PREPEND|DEEP_MERGE, matchOn...)`, y su propia lista de no soportado reconoce que falta `append()` ([docs/NOT_SUPPORTED.md](docs/NOT_SUPPORTED.md)).

El wire format puede llegar a ser suficiente en casos simples, pero la API Java no expresa con claridad “hacer append solo de `posts.data` y prepend de `announcements`” dentro de un mismo prop compuesto.  
**Corrección:** introducir un builder inmutable por prop (`merge(value).append("data").prepend("...").matchOn("data.id")`) y TCK que valide rutas anidadas, varias operaciones y `reset`.

### P2 — Instant visits: existe el campo, falta la prueba observada por cliente

El protocolo usa `sharedProps` para que el cliente conserve props compartidas en instant visits. La matriz confirma que se emite el campo, pero a la vez reconoce que la población observada por cliente queda pendiente ([docs/protocol-compatibility.md](docs/protocol-compatibility.md)).

**Corrección:** añadir Playwright con el cliente oficial para una instant visit que demuestre persistencia, colisión, exclusión y actualización de `sharedProps`; hasta entonces clasificar la característica como “implementada, sin validación E2E”, no “verificada”.

### P2 — Riesgo de deriva por duplicación de lógica crítica

La lógica de ensamblaje de página tiene **718 líneas** en Spring y **1.028** en Quarkus; los procesadores de redirect tienen **167** y **433** líneas, respectivamente. Esa duplicación explica parte de las diferencias entre transports y eleva el coste de mantener el protocolo en cada cambio upstream.

**Corrección:** mover al núcleo puro de `inertia-core` las decisiones de protocolo (metadata, merge/reset, versionado, clasificación de redirect) y dejar en los módulos de framework solo la adaptación de request/response, sesión y serialización. El TCK debe ser la única especificación ejecutable compartida.

### P2 — SSR remoto se documenta como frontera de confianza, pero no se valida al iniciar

[docs/ssr-setup.md](docs/ssr-setup.md) recomienda no configurar `inertia.ssr-url` remoto sin validación explícita. Sin embargo, los validadores de configuración inspeccionados validan estrategia, seguridad, cookie y límites, no esquema, host ni allowlist de SSR. Es un riesgo de SSRF/configuración si la propiedad puede ser alterada por un tercero.

**Corrección:** por defecto aceptar solo `http://127.0.0.1`, `localhost` y socket local; exigir `https`, allowlist de host y una bandera explícita para destinos remotos. Registrar de forma segura el host efectivo y probar rechazos.

### P2 — Reproducibilidad local incompleta

El repositorio no incluye `mvnw`/`.mvn/wrapper`, y sus workflows invocan `mvn` directamente. En esta auditoría no fue posible ejecutar `mvn test` porque Maven no estaba instalado en la sesión. Los informes guardados sirven como evidencia histórica, no sustituyen una ejecución limpia.

**Corrección:** versionar Maven Wrapper, documentar JDK/Node/Maven exactos y añadir un comando único de verificación que ejecute matriz, pruebas, análisis y E2E de forma reproducible.

### P3 — Menor madurez de ecosistema frente a Laravel/Rails

Laravel 3.x aporta [DevTools](https://inertiajs.com/docs/v3/advanced/devtools) con redacción de datos, comandos SSR, transformación de página y la integración natural con su ciclo de middleware. Rails 3.22 aporta generadores/CRUD, integración de cache de Rails, instrumentación `ActiveSupport::Notifications`, plugin Puma para SSR y documentación amplia. Java tiene métricas Micrometer, arquetipos y ejemplos valiosos, pero el propio proyecto excluye DevTools ([docs/NOT_SUPPORTED.md](docs/NOT_SUPPORTED.md)) y aún no ofrece una capa de integración equivalente en depuración y generación para cada framework.

Esto no afecta la interoperabilidad básica, pero sí el coste diario de un equipo y explica la diferencia de madurez en la puntuación.

## Comparación funcional resumida

| Área | Java Spring / Quarkus REST | Laravel 3.x | Rails 3.22 | Juicio |
|---|---|---|---|---|
| Contrato HTTP y page object v3 | Muy amplio, con TCK propio | Referencia oficial | Muy amplio | Java está cerca en el núcleo |
| HTML inicial / JSON seguro / CSP | Sí | Sí | Sí | Paridad práctica |
| Versionado, `409`, redirects, fragmentos | Sí | Sí | Sí | Paridad alta |
| Props parciales, opcionales, diferidas, once | Sí | Sí | Sí | Paridad alta |
| Merge, deep merge, prepend, scroll | Sí, API menos rica | Sí, builder fluido | Sí, rutas anidadas | Brecha de ergonomía `append()` |
| Errores, bags, Precognition | Sí, un mensaje por campo | Sí, arreglo opcional de errores | Sí | Brecha funcional Java |
| `sharedProps` e instant visits | Campo presente; E2E pendiente | Maduro | Maduro | Evidencia Java insuficiente |
| SSR | Sidecar, cache, timeout y breaker | Integrado con tooling Laravel/Vite | Renderer y Puma plugin | Bueno, menos integrado |
| DevTools / trazabilidad de visitas | No módulo propio | Sí | Instrumentación Rails | Brecha de experiencia |
| Seguridad | Buena base, modos y guardas | Seguridad del framework | Seguridad del framework | Buena, requiere configuración consciente |
| Imagen nativa | Sí | N/A | N/A | Ventaja diferenciadora Java |
| Generación inicial | Seis arquetipos | Starter kits/comandos | Instalador y scaffolds | Java buena base, menos flujo cotidiano |

## ¿Cumple 100% con inertiajs.com?

**No.**

La forma precisa de comunicar el estado es:

> “Implementa y prueba la mayor parte del protocolo Inertia v3 en Spring MVC y Quarkus REST. La matriz interna cubre 58 de 59 requisitos definidos por el proyecto, pero no se declara compatibilidad total: faltan la API `append()`, errores múltiples por campo, validación E2E de instant visits/shared props, DevTools y estabilidad de Quarkus Reactive Routes. El cliente Inertia v2 no debe anunciarse como soportado sin un modo de bootstrap legado probado.”

El [protocolo oficial](https://inertiajs.com/docs/v3/core-concepts/the-protocol) es framework-agnóstico y permite adaptadores nuevos; que el núcleo HTTP sea correcto es lo esencial. Sin embargo, la documentación oficial también cubre el comportamiento de errores múltiples, redirects con fragmento, instant visits, SSR y herramientas. “100%” exige tanto la semántica como pruebas independientes con los clientes oficiales y soporte claro de todas las rutas declaradas.

## Plan recomendado, en orden

1. **Corregir la comunicación:** eliminar `2.x` del README o añadir modo v2 probado; cambiar “58/59 verified” por “58/59 en matriz interna” y reclasificar errores múltiples e instant visits.
2. **Cerrar compatibilidad funcional:** soportar arreglos de errores, `append()`/rutas anidadas y su TCK; añadir E2E real para instant visits, `sharedProps`, redirect con fragmento, prefetch y SSR.
3. **Estabilizar Reactive Routes:** matriz de estrés y regresión por transporte; mantener la etiqueta experimental hasta completar el ciclo de estabilización.
4. **Fortalecer release engineering:** Maven Wrapper, PIT bloqueante, umbrales realistas, SCA obligatoria y verificable, SBOM/attestation publicados.
5. **Reducir divergencia:** extraer decisiones de protocolo a `inertia-core`; dejar adaptadores delgados y probar la misma batería contra Spring, Quarkus REST y Reactive.
6. **Cerrar postura SSR:** allowlist de destinos, TLS para remoto y prueba de configuración insegura rechazada.
7. **Mejorar experiencia:** DevTools interoperable o al menos trazas/diagnóstico de visitas con redacción de secretos; asistentes de instalación/generación comparables a los adaptadores de referencia.

## Evidencia y límites de la auditoría

- Se inspeccionaron los tres checkouts locales sin modificar sus fuentes. Había cambios previos no relacionados en dos ejemplos del repositorio Java; no fueron tocados.
- Se ejecutaron `node scripts/check-conformance-matrix.mjs` y `node scripts/check-versions.mjs`, ambos exitosos. No se pudo ejecutar Maven fresco por ausencia de Maven y de Maven Wrapper en esta máquina.
- Los puntajes de Laravel y Rails representan referencias maduras a partir de sus fuentes locales; no constituyen una certificación de que dichos proyectos estén libres de defectos.
- Las afirmaciones sobre el protocolo y sus expectativas se contrastaron contra la [documentación oficial de Inertia v3](https://inertiajs.com/docs/v3/core-concepts/the-protocol), [validación](https://inertiajs.com/docs/v3/the-basics/validation), [merge de props](https://inertiajs.com/docs/v3/data-props/merging-props) y [SSR](https://inertiajs.com/docs/v3/advanced/server-side-rendering).
