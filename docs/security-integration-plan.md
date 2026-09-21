# Plan de integración de seguridad para los adaptadores Inertia Java

## Objetivo

Entregar adaptadores Inertia para Spring y Quarkus que sean seguros por
defecto, respeten el protocolo oficial de Inertia v3 y se integren con la
seguridad nativa del framework cuando esté disponible.

La prioridad es **Spring Security**. La segunda integración es **Quarkus
Security**. Si una aplicación no incluye ninguna de esas bibliotecas, el
adaptador conservará una capa de protección básica, claramente documentada
como insuficiente para sustituir un framework de autenticación y autorización.

No es objetivo que Inertia autentique usuarios, emita JWT, mantenga un
directorio de usuarios ni decida permisos de negocio. Inertia debe conservar
las rutas, los controladores, la autenticación y la autorización en el
backend, tal como recomienda su documentación.

## Hallazgo que cambia el plan anterior

Los ejemplos del repositorio desactivan CSRF porque afirman que el cliente
`fetch` de Inertia v3 no envía `X-XSRF-TOKEN`. Esa premisa ya no es válida para
la versión de Inertia usada por el proyecto (`@inertiajs/core` 3.7.0): el
cliente oficial detecta la cookie `XSRF-TOKEN` y envía automáticamente su valor
en `X-XSRF-TOKEN`. Los nombres pueden cambiarse mediante la opción `http` de
`createInertiaApp`.

Por tanto, los adaptadores deben emitir una cookie compatible y las
aplicaciones de ejemplo deben **activar** CSRF. No se debe publicar el token
como prop de página salvo para clientes antiguos que no utilicen el cliente
oficial de Inertia.

Fuentes de diseño:

- [Protocolo de Inertia v3](https://inertiajs.com/docs/v3/core-concepts/the-protocol)
- [CSRF en Inertia v3](https://inertiajs.com/docs/v3/security/csrf-protection)
- [Spring Security](https://spring.io/projects/spring-security/)
- [CSRF de Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Quarkus Security](https://quarkus.io/guides/security-overview/)
- [CSRF de Quarkus](https://quarkus.io/guides/security-csrf-prevention)

## Validación frente a los adaptadores oficiales

La comparación se realizó contra los checkouts locales oficiales, no contra una
descripción genérica: `inertia-laravel` en el commit `5f5bf68` (2026-09-02) e
`inertia-rails` 3.22.0 en el commit `d74f3b9` (2026-08-15).

La conclusión es: **el plan es correcto después de las precisiones de esta
sección, pero no debe afirmar ser globalmente “mejor” antes de implementarse y
pasar la misma matriz de pruebas**. Los adaptadores oficiales no reemplazan la
seguridad del framework; se apoyan en Laravel y Rails. Ese es también el
principio correcto para Java.

| Área | Laravel y Rails actuales | Requisito para los adaptadores Java |
| --- | --- | --- |
| Autenticación y permisos | Delegados al framework y a la aplicación | Delegarlos a Spring Security o Quarkus Security; nunca inventar un sistema paralelo |
| CSRF | Laravel lo resuelve mediante su middleware nativo; Rails emite `XSRF-TOKEN`, copia `X-XSRF-TOKEN` a `X-CSRF-Token` y valida con Rails | En modo framework, delegar al mecanismo nativo y mantener los nombres del cliente Inertia |
| Renovación XSRF | Rails cubre primera visita, token obsoleto, `304`, logout y modo de renovación perezosa | Alcanzar al menos esa cobertura antes de declarar paridad |
| Redirecciones | Normalizan redirecciones tras mutaciones y convierten redirecciones externas Inertia en `409` con ubicación | Respetar exactamente esas semánticas del protocolo; no crear respuestas JSON ad hoc |
| Diagnóstico | Laravel redacta cookies, autorización y cabeceras CSRF en DevTools | Redactar esos valores en logs, trazas, páginas de error y herramientas de depuración |
| Sin framework de seguridad | No pretenden proporcionar autenticación/roles alternativos | Ofrecer únicamente defensas básicas y una opción para bloquear el arranque en producción |

El valor diferencial viable del plan no es competir contra la madurez de
Laravel/Rails, sino proporcionar una integración explícita y probada con **dos
ecosistemas Java**, un contrato Inertia uniforme y un guardrail que impida
desplegar accidentalmente el fallback básico donde se exige un framework de
seguridad.

## Contrato común

El siguiente contrato será idéntico para Spring y Quarkus.

| Caso | Respuesta HTML inicial | Visita Inertia (`X-Inertia: true`) |
| --- | --- | --- |
| Usuario anónimo en ruta protegida | Redirección al inicio de sesión | `409` con `X-Inertia-Location` hacia el inicio de sesión o al desafío OIDC |
| Usuario autenticado sin permiso | `403` | Página Inertia de error `403`, con `X-Inertia: true` |
| Token CSRF ausente o inválido | Error del framework o redirección segura | `303` de vuelta a la página anterior con mensaje flash “La página expiró” |
| Operación válida | Respuesta normal | Respuesta Inertia normal; los `302` tras una mutación se normalizan a `303` |
| Cambio de versión de assets u OIDC externo | No aplica | `409` y `X-Inertia-Location` para una navegación completa |

El uso de `409` está reservado a las respuestas de control del protocolo de
Inertia: no lleva `X-Inertia` y fuerza un `window.location`. Una respuesta de
página válida lleva `X-Inertia: true` y `Vary: X-Inertia`. No se deben
convertir indiscriminadamente todos los `302` de seguridad en JSON.

Para un fallo CSRF, un `419` sin cuerpo de página abriría el modal de excepción
del cliente. El comportamiento preferido es redirigir con `303` y un mensaje
flash: es la estrategia recomendada por Inertia para que el usuario pueda
reintentar sin un error técnico. El adaptador seguirá pudiendo exponer `419`
en el modo legado para compatibilidad, pero no será el flujo recomendado.

## Arquitectura y modos

Se reemplazará la propiedad ambigua `inertia.csrf-enabled` por una configuración
de seguridad explícita:

```properties
# auto es el valor recomendado
inertia.security.mode=auto
# auto | framework | adapter | disabled
inertia.security.fail-on-fallback=false
```

- `framework`: exige la integración de Spring Security o Quarkus Security; la
  aplicación no arranca si no está disponible.
- `auto`: usa la integración nativa si está en el classpath; de lo contrario
  usa la capa básica del adaptador y registra una advertencia visible al
  arranque.
- `adapter`: fuerza la capa básica y evita cualquier segundo filtro CSRF.
  Está destinada a aplicaciones que aportan su propia autenticación.
- `disabled`: solo es aceptable para pruebas o APIs sin cookies. Se rechazará
  en el perfil de producción salvo una propiedad de confirmación explícita.

La propiedad anterior se mantendrá una versión como alias de migración,
marcada como deprecada. La documentación no recomendará nunca desactivar
CSRF por una limitación inexistente del cliente Inertia actual.

En todos los modos habrá **un solo propietario de CSRF**:

| Modo | Propietario de CSRF | Filtro CSRF propio del adaptador |
| --- | --- | --- |
| Spring Security | Spring Security | Deshabilitado |
| Quarkus Security | `quarkus-rest-csrf` | Deshabilitado |
| Adapter | Adaptador Inertia | Habilitado |
| Disabled | Ninguno | Deshabilitado |

## Runbook obligatorio para quien implemente el plan

Esta sección es normativa. Una IA o persona que ejecute el trabajo debe seguir
las fases en orden y no sustituir decisiones explícitas por alternativas
propias sin actualizar este documento y sus pruebas.

### 0. Reglas que no se pueden romper

1. No añadir autenticación, almacenamiento de usuarios, emisión de JWT ni
   roles al núcleo de `spring-inertia` o `quarkus-inertia`.
2. No desactivar CSRF globalmente para hacer que un ejemplo o una prueba pase.
   Una API bearer se separa por rutas y cadena de seguridad; las páginas con
   sesión conservan CSRF activo.
3. No habilitar `Access-Control-Allow-Origin: *` junto con credenciales. El
   adaptador no debe inventar una política CORS; esa decisión pertenece a la
   aplicación.
4. No publicar token CSRF, cookies, credenciales, claims completos o roles
   como mecanismo de autorización en props de Inertia.
5. No convertir un `401`, `403`, `302`, `303` o `419` en una respuesta Inertia
   sin verificar antes la semántica que exige el protocolo. En particular, un
   `409` de control no lleva `X-Inertia: true`.
6. No borrar, revertir ni reformatear cambios ajenos ya presentes en el árbol
   de trabajo. Cada cambio debe limitarse a los archivos de esta iniciativa.

### 1. Prevuelo obligatorio

Antes de editar código, completar estas comprobaciones y conservar su salida
en la descripción del cambio o PR:

```powershell
git status --short
mvn -pl spring-inertia test
mvn -pl quarkus-inertia test
rg -n "inertia\.csrf-enabled|csrf\.disable\(\)|XSRF-TOKEN|X-XSRF-TOKEN" .
```

Si una suite ya falla, no atribuir el fallo al cambio de seguridad. Registrar
el resultado de base, aislar la prueba nueva y continuar sin modificar código
no relacionado. Las pruebas de ejemplo se ejecutan después de compilar sus
frontends, por ejemplo:

```powershell
mvn -f examples/spring/spring-pingcrm-react/pom.xml test
mvn -f examples/quarkus/quarkus-pingcrm-svelte/pom.xml test
```

### 2. Decisiones congeladas de compatibilidad

Estas decisiones eliminan ambigüedad de implementación:

| Tema | Decisión obligatoria |
| --- | --- |
| Topología predeterminada | Monolito same-origin: HTML, JavaScript, sesión y controladores comparten origen. |
| Cliente | El cliente oficial Inertia v3 es quien lee `XSRF-TOKEN` y envía `X-XSRF-TOKEN`; no escribir un interceptor duplicado. |
| Cookie XSRF | `HttpOnly=false`, `Path=/`, `SameSite=Lax` por defecto, `Secure=true` en producción HTTPS. Es legible porque el cliente debe copiarla al header. |
| Cookie de sesión | La gestiona el framework; debe ser `HttpOnly`, `Secure` en producción y no se expone como prop. |
| Autorización | Siempre se decide en servidor; `auth.can.*` en props solo modifica la interfaz. |
| CSRF de framework | Es el único validador cuando está activo. El filtro CSRF propio debe quedar inactivo. |
| CSRF sin framework | El filtro propio puede validar token de sesión, pero no autentica ni autoriza al usuario. |
| Fallo CSRF Inertia | Redirección `303` al `Referer` **solo si es same-origin**; de lo contrario, a una ruta configurada y segura, por defecto `/`. Añadir flash genérico sin detalles internos. |
| Redirección a login/OIDC desde Inertia | `409` + `X-Inertia-Location`; sin `X-Inertia` y sin serializar una página. |

La precedencia de configuración será exactamente esta:

1. `inertia.security.mode` explícito siempre gana.
2. Si no existe y existe el flag legado `inertia.csrf-enabled`, `true` equivale
   temporalmente a `adapter` y `false` a `disabled`; registrar aviso de
   deprecación que muestre la propiedad nueva.
3. Si no se declara ninguna de las dos propiedades, usar `auto`.
4. En `auto`, la presencia del **artefacto de integración** y del framework
   correspondiente selecciona `framework`; en otro caso selecciona `adapter`.
5. `framework` falla al arrancar si falta el artefacto o la dependencia de
   seguridad necesaria. `auto` con `fail-on-fallback=true` falla si termina en
   `adapter`. `adapter` explícito es una decisión consciente y no falla por
   ese flag.
6. `disabled` solo se permite en perfiles de prueba. Para usarlo en producción
   hará falta `inertia.security.allow-disabled-in-production=true`, cuyo valor
   por defecto es `false`.

### 3. Mapa de archivos y responsabilidad

La persona implementadora debe empezar por estos archivos; no buscar una
solución en los ejemplos antes de completar el núcleo y sus pruebas.

| Área | Archivos actuales a modificar | Resultado esperado |
| --- | --- | --- |
| Propiedades Spring | `spring-inertia/.../config/InertiaProperties.java`, `InertiaConfigValidator.java` | Modo, guardrails de producción y configuración de cookie/fallo CSRF. |
| Registro Spring | `spring-inertia/.../config/InertiaAutoConfiguration.java`, `mvc/InertiaCsrfFilter.java` | Registrar el filtro propio solo en modo `adapter`; nunca en `framework`. |
| Propiedades Quarkus | `quarkus-inertia/.../config/InertiaConfig.java`, `InertiaConfigValidator.java` | Las mismas propiedades y validaciones que Spring. |
| Registro Quarkus | `quarkus-inertia/.../security/InertiaCsrfFilter.java`, `vertx/InertiaVertxHandler.java` | No ejecutar CSRF propio cuando lo aporta `quarkus-rest-csrf`. |
| Dependencias | `pom.xml` raíz y nuevos POM de integración | Añadir artefactos de integración sin hacer que el núcleo dependa transitivamente de seguridad. |
| Cliente y ejemplos | `examples/**/src/main/webui/**`, `archetypes/**/src/main/webui/**` | Conservar o configurar los nombres XSRF; quitar comentarios y flags que deshabilitan CSRF. |
| Documentación | `docs/configuration.md`, guías de inicio, READMEs de ejemplos y matriz de protocolo | Explicar el modo seleccionado, dependencias y migración. |

Las rutas abreviadas con `...` conservan su paquete actual
`io.github.diovamny.spring.inertia` o `io.github.diovamny.quarkus.inertia`.
Antes de crear una clase, confirmar el paquete con `rg --files`.

### 4. Contrato HTTP que se debe probar antes de integrar un framework

Usar una aplicación de prueba mínima que devuelva una página Inertia en
`GET /security/dashboard` y una mutación en `POST /security/submit`.

1. `GET` inicial sin `X-Inertia` devuelve HTML, `200`, el page object seguro y
   una cookie `XSRF-TOKEN` cuando el modo CSRF está activo.
2. `GET` posterior con `X-Inertia: true` devuelve JSON, `X-Inertia: true` y
   `Vary` que contenga `X-Inertia`.
3. El cliente Inertia v3 debe enviar `X-XSRF-TOKEN` con el valor de la cookie.
   Verificarlo en una prueba de navegador; no asumirlo solo por un test HTTP.
4. `POST` con cookie de sesión y token correcto llega al controlador exactamente
   una vez. `POST` sin token o con token incorrecto no lo alcanza.
5. Una respuesta `304` no debe dejar un token obsoleto ni forzar una cookie de
   sesión innecesaria. La política puede reemitir el XSRF si hace falta, pero
   debe tener prueba para ambos caminos.
6. Tras login, logout o invalidación de sesión, la siguiente carga obtiene un
   token válido para la nueva sesión.
7. Cualquier valor de `Referer` usado como destino se analiza como URI y se
   compara con scheme, host y puerto actuales; nunca se reenvía una URL externa
   ni una URL inválida.

### 5. Implementación exacta de Spring Security

Crear `spring-inertia-security` como artefacto Maven separado y añadirlo como
módulo de la raíz. Su dependencia de compilación es `spring-security-web`; el
artefacto `spring-inertia` permanece independiente de Spring Security.

Implementar en este orden:

1. Crear una auto-configuración condicional a `HttpSecurity` y a
   `SecurityFilterChain`. Debe registrar helpers, no crear una política
   `permitAll` ni una cadena de seguridad que cambie rutas sin aprobación de
   la aplicación.
2. Proveer un configurador que use `CookieCsrfTokenRepository` con cookie
   `XSRF-TOKEN` y cabecera `X-XSRF-TOKEN`. Usar el manejador SPA recomendado
   por la versión de Spring Security declarada en el BOM; verificar su API con
   una prueba de compilación, no copiar imports de una versión anterior.
   La configuración resultante debe ser semánticamente equivalente a llamar a
   `CookieCsrfTokenRepository.withHttpOnlyFalse()`, sin alterar la cookie de
   sesión ni deshabilitar `.csrf(...)`.
3. Definir un `RequestMatcher` que reconozca únicamente
   `X-Inertia: true`. No basarse en `X-Requested-With` para seguridad.
4. Implementar un `AuthenticationEntryPoint` para ese matcher. Debe devolver
   `409`, establecer solo `X-Inertia-Location` a una URL de login/OIDC validada
   y terminar la respuesta. El entry point normal de Spring se mantiene para
   HTML y APIs.
5. Implementar un `AccessDeniedHandler` para ese matcher. Si la excepción es
   de CSRF, guardar flash genérico y emitir `303 Location: <destino seguro>`;
   en cualquier otro caso producir una respuesta de página Inertia `403`.
   La respuesta `403` debe contener `X-Inertia: true`, `Vary: X-Inertia` y un
   componente configurable, por defecto `Errors/Forbidden`.
6. Desactivar el bean/registro del `InertiaCsrfFilter` del núcleo en
   `framework`. No colocar el filtro propio antes ni después de `CsrfFilter`.
7. Añadir, solo si se solicita, un contribuidor de props que exponga
   `{ id, name, roles, can }` ya calculados. Nunca devolver el objeto
   `Authentication`, credenciales o atributos sin lista permitida.
8. Actualizar los ejemplos Spring para eliminar `.csrf(csrf -> csrf.disable())`.
   Configurar rutas públicas de forma explícita, proteger el resto y habilitar
   autorización por método donde el ejemplo lo requiera.

Pruebas obligatorias en el módulo nuevo: `MockMvc` + `spring-security-test`
para usuario anónimo, usuario autenticado, dos roles, token válido, token
ausente, token alterado, sesión renovada, `403`, `409`, `303` y URL `Referer`
maliciosa. Añadir un test que demuestre que el controlador mutante no se
ejecuta cuando falla CSRF.

### 6. Implementación exacta de Quarkus Security

Crear `quarkus-inertia-security` como artefacto de integración separado. No
llamarlo una extensión de Quarkus si no se implementa también la estructura
runtime/deployment que Quarkus exige para una extensión; como mínimo debe ser
un JAR CDI documentado y probado.

Implementar en este orden:

1. Declarar `quarkus-security` y `quarkus-rest-csrf` como dependencias del
   artefacto de integración. La aplicación elige además un mecanismo real de
   identidad, por ejemplo `quarkus-oidc` o `quarkus-security-jpa`; incluir solo
   `quarkus-security` no autentica a nadie.
2. Configurar `quarkus-rest-csrf` para `XSRF-TOKEN` y `X-XSRF-TOKEN`, permitir
   que la cookie sea legible por JavaScript y forzar `Secure` en producción.
   No registrar simultáneamente `InertiaCsrfFilter`.

   La aplicación de ejemplo segura debe contener el equivalente de esta
   configuración; la clave HMAC llega por secreto de entorno, nunca desde el
   repositorio ni desde una prop Inertia:

   ```properties
   quarkus.rest-csrf.enabled=true
   quarkus.rest-csrf.cookie-name=XSRF-TOKEN
   quarkus.rest-csrf.token-header-name=X-XSRF-TOKEN
   quarkus.rest-csrf.cookie-http-only=false
   %prod.quarkus.rest-csrf.cookie-force-secure=true
   %prod.quarkus.rest-csrf.token-signature-key=${INERTIA_CSRF_HMAC_KEY}
   ```

   La comprobación de configuración debe rechazar en producción una clave
   ausente o de menos de 32 caracteres.
3. Migrar los ejemplos desde `config/AuthFilter.java` a políticas HTTP y
   anotaciones `@Authenticated`/`@RolesAllowed`. La identidad se obtiene de
   `SecurityIdentity`; no de una entrada creada manualmente en sesión.
4. Implementar el puente de respuesta en el punto que pueda observar los retos
   de Quarkus sin reemplazar su decisión: para una visita Inertia sin identidad,
   `409` + `X-Inertia-Location`; para una identidad sin rol, página Inertia
   `403`; para CSRF, `303` seguro con flash.
5. Confirmar el orden con recursos JAX-RS y rutas Vert.x. Si un filtro no ve la
   sesión o no puede transformar la respuesta, no añadir otro filtro a ciegas:
   escribir una prueba mínima, localizar el punto de extensión correcto y
   documentar el orden resultante.
  6. Limitar los props de identidad a una DTO permitida. Las anotaciones/políticas
     siguen siendo la barrera de autorización.
  7. Empaquetar el artefacto con `META-INF/beans.xml` (descubrimiento
     `annotated`): sin índice propio, ni los beans CDI ni los `@Provider`
     JAX-RS del módulo se registran, y el puente/filtros quedan
     silenciosamente inactivos. Verificado: el núcleo ya lo incluye; el
     módulo de seguridad fallaba sin él.
  8. Si la identidad de ejemplo vive en sesión Vert.x, registrar el
     `SessionHandler` con orden anterior a la autenticación (p. ej.
     `order(-1000)`): las políticas HTTP se evalúan antes que las rutas de
     usuario y el mecanismo necesita la sesión ya disponible.
  9. El contribuidor de identidad del módulo devuelve mapa vacío cuando
     `auth-props-enabled=false` (valor por defecto): nunca debe sustituir un
     `auth` propio de la aplicación. Las demos que exponen `auth` desde base
     de datos fijan `inertia.security.auth-props-enabled=false` explícito.

  Pruebas obligatorias: `@QuarkusTest` y `quarkus-test-security` para los mismos
casos de Spring, más una ruta JAX-RS y una ruta reactiva. Añadir una prueba de
configuración que falle cuando `mode=framework` carece de `quarkus-rest-csrf`
o de un mecanismo de autenticación elegido por el ejemplo.

### 7. Implementación delimitada del fallback `adapter`

El modo `adapter` solo es aceptable cuando el consumidor aporta por sí mismo
una identidad, autorización y gestión segura de sesión. El código del
adaptador puede:

- crear y validar un token CSRF de 256 bits o más, ligado a la sesión;
- comparar el token en tiempo constante;
- configurar cookie XSRF y aplicar el destino seguro de fallo CSRF;
- añadir `Vary: X-Inertia`, serialización segura y redacción de secretos;
- ofrecer interfaces de solo lectura para compartir identidad ya autenticada.

El código del adaptador no puede decidir que una ruta es privada solo porque
existe un prop `auth`, ni aceptar una identidad llegada desde el navegador.
Debe mostrar al inicio una advertencia con el modo activo y recomendar
`fail-on-fallback=true` para producción. Si no existe una sesión, no debe crear
una solamente para un GET estático sin que la política de la aplicación lo
requiera.

### 8. Entregables de cada fase y criterio de paso

| Fase | Entregable mínimo | No avanzar si falta |
| --- | --- | --- |
| Base/protocolo | Configuración, tests HTTP y E2E XSRF, documentación corregida | La cabecera no llega o el token se publica como prop por defecto |
| Spring | Nuevo artefacto, ejemplo seguro y suite `spring-security-test` verde | Hay dos validadores CSRF o `.csrf().disable()` permanece en ejemplos seguros |
| Quarkus | Nuevo artefacto, ejemplo con `SecurityIdentity`, JAX-RS y Vert.x probados | El ejemplo conserva `AuthFilter` manual como barrera principal |
| Fallback | Guardrails, advertencia, SPI solo lectura y pruebas de sesión | Se describe o prueba como sustituto de autenticación/autorización |
| Publicación | Guía de migración, matrices, SBOM y análisis de dependencias | No existe una ruta documentada para migrar el flag legado |

Al cerrar una fase, ejecutar las pruebas de esa fase, las dos suites de los
adaptadores y al menos un ejemplo por framework. Un fallo de seguridad no se
silencia con `@Disabled`, exclusiones amplias de rutas, `permitAll`, ni una
propiedad que apague CSRF.

Las secciones «Fase 1» a «Fase 5» que siguen conservan el contexto funcional
del plan. Si alguna formulación resumida parece contradecir el runbook, el
runbook de esta sección prevalece.

## Fase 1 — Base compatible con el frontend y el protocolo

1. Mantener los nombres por defecto `XSRF-TOKEN` y `X-XSRF-TOKEN`. Si se
   personalizan, generar en los arquetipos la configuración equivalente de
   `createInertiaApp({ http: { xsrfCookieName, xsrfHeaderName } })`.
2. Verificar con pruebas de navegador que `Link`, `router`, formularios y carga
   de archivos transmiten la cabecera. No añadir interceptores manuales salvo
   para clientes HTTP ajenos a Inertia.
3. Emitir la cookie de token al cargar el HTML inicial y renovarla después de
   autenticarse o invalidar sesión. La cookie XSRF debe ser legible por el
   cliente (`HttpOnly=false`); la cookie de sesión debe ser `HttpOnly=true`.
4. Añadir configuración de `Secure`, `SameSite`, dominio y path. En producción
   se exigirá HTTPS, `Secure=true`, `SameSite=Lax` como mínimo, y una lista de
   orígenes CORS restringida si la aplicación no es estrictamente same-origin.
5. Conservar el escape seguro del JSON dentro del `<script>` de la respuesta
   HTML; el protocolo exige evitar que datos de props cierren el script.

**Criterio de aceptación:** ninguna aplicación de ejemplo contiene
`inertia.csrf-enabled=false` por el supuesto de que Inertia no manda la
cabecera, y las pruebas E2E observan `X-XSRF-TOKEN` en cada mutación. La suite
debe cubrir además primera visita, token obsoleto, `304`, renovación tras
login/logout e invalidación de sesión, como mínimo al nivel de la suite de
`inertia-rails`.

## Fase 2 — Integración prioritaria con Spring Security

Crear el artefacto opcional `spring-inertia-security`. El núcleo
`spring-inertia` no debe depender transitivamente de Spring Security, pero el
artefacto de integración sí puede depender de `spring-security-web`.

### Responsabilidades

1. Proporcionar un configurador/documentación para `SecurityFilterChain`, no
   una cadena de seguridad opaca que habilite rutas sin consentimiento de la
   aplicación.
2. Configurar `CookieCsrfTokenRepository` con `XSRF-TOKEN` y
   `X-XSRF-TOKEN`, y el manejador de token para SPA de la versión compatible de
   Spring Security. Spring Security será quien cree, exponga y valide el
   token.
3. Suprimir la creación y el registro de `InertiaCsrfFilter` cuando el modo
   sea `framework`. Así se evita doble validación, tokens distintos y orden de
   filtros indeterminado.
4. Ofrecer un `AuthenticationEntryPoint` Inertia-aware: una petición normal
   usa la redirección de Spring; una visita Inertia recibe `409` con
   `X-Inertia-Location` a `/login` o al proveedor OIDC.
5. Ofrecer un `AccessDeniedHandler` que diferencie `AccessDeniedException` de
   `InvalidCsrfTokenException`/`MissingCsrfTokenException`. El primero devuelve
   un `403` Inertia; el segundo aplica el `303` con mensaje flash y permite
   que el siguiente GET emita un token nuevo.
6. Incluir un contribuidor de props opcional que lea
   `SecurityContextHolder` y publique únicamente `auth.user` y capacidades de
   presentación. El servidor seguirá evaluando `@PreAuthorize`, permisos y
   reglas de negocio; el prop `can` solo oculta o muestra controles de UI.
7. Documentar dos configuraciones de referencia: sesión/form login para el
   monolito Inertia y Authorization Code con OIDC. Las APIs con bearer tokens
   tendrán una `SecurityFilterChain` separada; no se desactivará CSRF para toda
   la aplicación por causa de esas APIs.

**Criterio de aceptación:** los ejemplos Spring utilizan Spring Security para
login, cierre de sesión, fijación/rotación de sesión, autorización por ruta y
método, y CSRF. Ninguno usa un token CSRF paralelo del adaptador.

## Fase 3 — Integración con Quarkus Security

Crear `quarkus-inertia-security`, con dependencia opcional en las extensiones
de Quarkus adecuadas. El módulo detectará el modo de seguridad en tiempo de
build y registrará solo los beans/filtros necesarios.

### Responsabilidades

1. Reemplazar los `AuthFilter` manuales de los ejemplos por Quarkus Security:
   `SecurityIdentity`, `@Authenticated`, `@RolesAllowed` y políticas HTTP.
   Para aplicaciones reales se documentarán `quarkus-oidc` (sesión/OIDC) y
   `quarkus-security-jpa` cuando la identidad provenga de una base de datos.
2. Adoptar `quarkus-rest-csrf` como única protección CSRF en modo framework,
   configurada con cookie `XSRF-TOKEN`, cabecera `X-XSRF-TOKEN`, cookie no
   `HttpOnly` y `cookie-force-secure=true` en producción. El filtro CSRF
   propio de `quarkus-inertia` no debe registrarse entonces.
3. Añadir un puente de desafío y de denegación que preserve las decisiones de
   Quarkus: `409` + `X-Inertia-Location` para autenticación necesaria en una
   visita Inertia, y página Inertia `403` para autorización denegada.
4. Compartir un resumen mínimo de `SecurityIdentity` mediante props; no
   compartir credenciales, access tokens, refresh tokens, atributos internos
   ni la identidad completa.
5. Probar también recursos REST reactivos y rutas Vert.x, pues ambas rutas
   existen en el adaptador actual.

**Criterio de aceptación:** el ejemplo Quarkus deja de decidir autenticación
en un filtro propio y ejerce RBAC real contra una identidad de Quarkus.

## Fase 4 — Capa básica del adaptador, cuando no existe un framework

Esta capa reduce riesgos, pero **no equivale** a Spring Security ni a Quarkus
Security y no se presentará como tal.

Incluye:

- token CSRF de alta entropía ligado a sesión, cookie `XSRF-TOKEN`, comparación
  de tiempo constante y renovación al invalidar sesión;
- cookies con `Secure`, `SameSite`, path y dominio configurables;
- cabeceras seguras configurables: `X-Content-Type-Options: nosniff`, política
  de referencias restrictiva, `Permissions-Policy` conservadora y CSP; HSTS
  solo bajo HTTPS;
- rechazo de orígenes cruzados no incluidos explícitamente y protección de
  redirecciones abiertas;
- serialización de props segura, sin detalles de excepción en producción;
- una SPI `InertiaIdentityResolver` **solo para presentación**: puede publicar
  un resumen de la identidad que la aplicación ya autenticó, pero no autoriza
  rutas ni sustituye una política del servidor.

No incluye almacén de usuarios, hashing de contraseñas, MFA, OIDC/OAuth2,
rotación de secretos, protección contra fuerza bruta, administración de roles
ni autorización declarativa. En producción, `fail-on-fallback=true` permite
exigir que la aplicación incorpore Spring Security o Quarkus Security.

La capa básica tampoco debe presentarse como una implementación general de
CORS. CORS y las políticas de cabeceras pertenecen a la aplicación anfitriona;
el adaptador podrá aportar configuración y validación de `Origin` para
solicitudes mutantes con cookie, pero no habilitará orígenes ni credenciales de
forma implícita. La protección CSRF seguirá siendo obligatoria cuando haya
sesiones basadas en cookies.

## Fase 5 — Pruebas, documentación y lanzamiento

1. Añadir suites de integración con `spring-security-test` y
   `quarkus-test-security`; no dar por válida la integración con tests que
   instancian únicamente el filtro del adaptador.
2. Probar esta matriz: anónimo, autenticado, rol insuficiente, rol permitido,
   token válido, ausente y inválido, primera visita, token obsoleto, respuesta
   `304`, rotación de sesión después de login, logout, expiración de sesión,
   OIDC externo y CORS no confiable.
3. Añadir Playwright para Vue, React y Svelte; debe comprobar la cabecera
   XSRF, el `409` de expiración, el `303` de CSRF y que la UI no convierte un
   permiso en autorización real.
4. Actualizar la matriz de compatibilidad y los arquetipos. Eliminar los
   comentarios que recomiendan apagar CSRF y sustituirlos por una guía de
   migración.
5. Redactar `Cookie`, `Set-Cookie`, `Authorization`, `Proxy-Authorization`,
   `X-XSRF-TOKEN`, `X-CSRF-TOKEN`, contraseñas y tokens de acceso/refresco en
   logs, trazas y herramientas de diagnóstico.
6. Ejecutar análisis de dependencias, SBOM y revisión de configuración en CI;
   mantener pruebas de imagen nativa de Quarkus para la integración nueva.

## Orden de entrega

1. Corregir documentación, ejemplos y E2E de cabecera XSRF.
2. Implementar y probar `spring-inertia-security` con sesión/form login.
3. Añadir OIDC para Spring y los controladores de errores Inertia.
4. Implementar `quarkus-inertia-security` con RBAC y `quarkus-rest-csrf`.
5. Añadir el modo básico, los guardrails de producción y la migración del
   flag legado.

La primera entrega debe ser Spring Security + Inertia con CSRF activo: es la
ruta que satisface el objetivo principal sin duplicar controles de seguridad.
