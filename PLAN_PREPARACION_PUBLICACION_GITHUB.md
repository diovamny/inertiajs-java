# Plan de preparación para publicación en GitHub

**Estado:** plan de trabajo; no autoriza cambios de licencia, borrados ni publicación por sí mismo.  
**Ámbito:** `inertiajs` (adaptadores Inertia v3 para Spring MVC y Quarkus).  
**Objetivo:** preparar un repositorio público mantenible, verificable y profesional, comparable en estructura editorial con `inertiajs/inertia-laravel` e `inertiajs/inertia-rails`, listo para una primera versión estable y publicación en Maven Central.

## 1. Decisiones de referencia

| Tema | Laravel | Rails | Estado actual de Java | Decisión propuesta |
|---|---|---|---|---|
| Licencia | MIT | MIT | Apache-2.0 | Adoptar MIT, **solo** si el titular de todos los aportes puede relicenciarlo. |
| README | Breve, enlaza a la documentación oficial | Producto, instalación, ejemplos y enlaces | Técnico y parcialmente bilingüe | README público en inglés, orientado a adopción, con enlaces a guías detalladas. |
| Comunidad | Contribución, conducta, seguridad y soporte | Conducta y plantilla de PR | Contribución básica; faltan políticas completas | Añadir políticas GitHub completas y datos de contacto reales. |
| Automatización | CI y publicación | CI y publicación | CI, nativo y release ya existen | Convertirlos en requisitos de publicación, con pruebas E2E realmente ejecutables. |

Laravel y Rails usan el texto estándar MIT. La migración de Apache-2.0 a MIT no consiste en copiar el aviso de otro proyecto: debe contener el año y titular reales de este repositorio.

## 2. Principios que no se deben vulnerar

1. No usar el nombre, la organización, el logotipo ni la identidad de `inertiajs` como si el adaptador fuera oficialmente mantenido por ese proyecto sin autorización expresa.
2. No eliminar avisos de copyright, atribuciones de terceros, licencias transitivas ni avisos obligatorios.
3. No reescribir autores, firmas o historial para aparentar un proceso de desarrollo distinto al real. Si se inicia un repositorio nuevo, debe hacerse con autorización de todos los titulares y con atribución correcta.
4. La ausencia de material interno no debe convertirse en una afirmación falsa como “desarrollado sin herramientas de asistencia”. La política adecuada es publicar documentación final, técnica y verificable; no publicar borradores, prompts, registros ni artefactos internos.
5. Ninguna afirmación de compatibilidad, cantidad de pruebas o soporte nativo se publicará sin una ejecución reproducible que la respalde.

## 3. Fase 0 — congelar y clasificar el contenido público

### Acciones

1. Crear una rama de preparación para lanzamiento y registrar el SHA de partida.
2. Elaborar un inventario de cada archivo versionado con estas clasificaciones: `producto`, `documentación pública`, `ejemplo`, `infraestructura`, `generado`, `interno` y `tercero`.
3. Revisar `git log --all`, autores, copyright de cabeceras y procedencia de fragmentos copiados antes de cualquier cambio de licencia o de repositorio.
4. Ejecutar una búsqueda de secretos, rutas locales, direcciones personales, URL `file:`, archivos de configuración privada y datos de prueba que parezcan reales.
5. Definir el repositorio definitivo, organización propietaria, URL canónica, correo de seguridad y canal de soporte antes de editar README, POM o workflows.

### Material que no debe ir al repositorio público

Revisar y mover a un archivo privado controlado —o retirar en un cambio explícito y revisable— el directorio `Prompt/`, `implementation_plan.md`, `PLAN_100_PROTOCOLO_Y_MADUREZ.md` y `PLAN_DE_MEJORA_INERTIA.md`. Contienen planificación interna, rutas locales y detalles de trabajo que no son documentación para usuarios. No deben destruirse sin conservar una copia privada si tienen valor histórico o contractual.

También deben estar ignorados y ausentes del índice: `target/`, informes temporales, `effective-pom*.xml`, `*.log`, `.tmp-*`, resultados de Playwright, artefactos nativos, cobertura, cachés de Maven/Node y archivos de IDE. Verificar el resultado con `git status --ignored` y con un clon limpio.

### Criterio de salida

- Inventario aprobado por el titular.
- No hay secretos ni rutas locales en archivos destinados a publicación.
- Existe una lista explícita de archivos que se conservarán privados y de archivos que se publicarán.

## 4. Fase 1 — titularidad, licencia y metadatos de paquetes

### 4.1 Confirmación legal previa

Antes de modificar `LICENSE`, obtener por escrito estas confirmaciones:

1. Quién es el titular de copyright de cada contribución propia.
2. Que cada colaborador relevante acepta MIT o ya contribuyó bajo un acuerdo compatible.
3. Que ningún archivo de terceros impone Apache-2.0, GPL, copyleft u obligaciones incompatibles con el cambio.
4. Si se requieren archivos `NOTICE`, `THIRD_PARTY_NOTICES` o atribuciones de ejemplos/activos.

Si una de estas condiciones falla, mantener Apache-2.0 y explicar en README que es una elección consciente. No se debe mezclar una licencia MIT de raíz con código que exija otra licencia.

### 4.2 Cambios posteriores a la autorización

1. Sustituir el contenido de `LICENSE` por el texto MIT estándar con el titular y año correctos; conservar el nombre `LICENSE` para una detección simple de GitHub.
2. Actualizar en el POM raíz y en los módulos: `licenses`, URL, `scm`, distribución, desarrollador/organización y correo. El POM actual usa enlaces provisionales `github.com/dg/inertia-java`; deben apuntar al repositorio publicado real.
3. Verificar que cada JAR incluye `META-INF/LICENSE` y las atribuciones requeridas.
4. Alinear los metadatos de Maven Central con el repositorio, el nombre de artefactos, la descripción, el SCM y la licencia MIT.
5. Publicar un `THIRD_PARTY_NOTICES.md` solo si la revisión de licencias identifica material que lo requiere; no generarlo como documento vacío.

### Criterio de salida

- La licencia del archivo raíz, los POM y Maven Central coinciden.
- El responsable legal valida el cambio y las atribuciones.
- Un `mvn verify` comprueba los artefactos publicados localmente.

## 5. Fase 2 — documentación de producto

### 5.1 Reescritura del README

Reemplazar el README actual por una página de entrada concisa, en inglés técnico claro, con esta estructura:

1. Nombre neutral del proyecto y una frase precisa: adaptadores de servidor Inertia v3 para Spring MVC y Quarkus.
2. Badges reales: CI, versión de Maven Central cuando exista, licencia MIT y compatibilidad. No añadir badges de cobertura, descargas o calidad hasta que tengan fuente activa.
3. Enlaces a documentación, ejemplos, releases, incidencias, discusiones y política de seguridad.
4. Matriz de compatibilidad: Java, Spring Boot, Quarkus, clientes Inertia verificados y plataformas nativas.
5. Instalación independiente para Spring y Quarkus con las coordenadas Maven definitivas.
6. Un ejemplo mínimo y compilable de cada framework: configuración, controlador/recurso y render de una página.
7. Cómo ejecutar los ejemplos y la suite de pruebas.
8. Estado de compatibilidad expresado con evidencia: enlazar a la matriz de conformidad, sin declarar 100% hasta que los contratos E2E estén verdes en CI.
9. Enlaces a contribución, seguridad, soporte, changelog y licencia.

Eliminar afirmaciones no verificadas. En particular, no mantener el conteo “359 Java tests”: la documentación debe calcularlo en CI o actualizarse desde un informe de una ejecución concreta.

### 5.2 Sitio y guías

Conservar `docs/` como fuente inicial y normalizarla a inglés, con enlaces relativos y títulos coherentes. Publicar estas guías antes de la versión `1.0.0`:

- `docs/getting-started/spring.md` y `docs/getting-started/quarkus.md`.
- `docs/configuration.md`: propiedades, valores por defecto, seguridad, SSR y versionado de assets.
- `docs/props.md`: shared, optional, deferred, once, merge y scroll.
- `docs/testing.md`: DSL, contratos de protocolo y E2E.
- `docs/ssr.md` y `docs/native-image.md`, con limitaciones conocidas y comandos reproducibles.
- `docs/migration.md`: cambios incompatibles y guía desde una versión anterior, si aplica.
- `docs/protocol-compatibility.md`: matriz que diferencie “implementado”, “probado por contrato” y “pendiente”.

No copiar el texto, marca ni diseño de la documentación de Laravel/Rails. Usar enlaces a Inertia para explicar el protocolo y escribir contenido propio para la integración Java.

### 5.3 Ejemplos que demuestran el producto

1. Declarar qué demos se mantienen oficialmente: mínimo un ejemplo Spring y uno Quarkus; especificar cliente React/Vue/Svelte y versión.
2. Cada demo debe tener instrucciones de arranque desde un clon limpio, datos de prueba, credenciales ficticias y captura o recorrido verificable.
3. Separar claramente ejemplos de producción: nunca presentar sus configuraciones de seguridad, almacenamiento o credenciales como recomendaciones completas.

### Criterio de salida

- Una persona puede instalar cada adaptador y ejecutar una primera página siguiendo únicamente la documentación.
- Todos los fragmentos del README y de guías se compilan o se prueban en CI.
- Los enlaces no apuntan a rutas locales, documentación privada ni ramas inexistentes.

## 6. Fase 3 — comunidad y gobernanza GitHub

Crear o completar estos archivos, usando las políticas de Laravel/Rails como referencia estructural, no copiando sus datos de contacto:

| Archivo | Contenido mínimo |
|---|---|
| `CODE_OF_CONDUCT.md` | Contributor Covenant vigente, responsable y mecanismo de reporte real. |
| `CONTRIBUTING.md` | Entorno, ramas, estilo, pruebas obligatorias, convención de commits, revisión y DCO/CLA si se adopta. |
| `.github/SECURITY.md` | Versiones soportadas, correo o formulario de seguridad real, prohibición de divulgar vulnerabilidades antes de la corrección y tiempos orientativos de respuesta. |
| `.github/SUPPORT.md` | Qué se resuelve en issues, qué va a Discussions/Discord y cómo pedir ayuda sin publicar secretos. |
| `.github/PULL_REQUEST_TEMPLATE.md` | Resumen, prueba añadida, documentación, compatibilidad, changelog y checklist de seguridad. |
| `.github/ISSUE_TEMPLATE/config.yml` | Enlaces de soporte y seguridad, para evitar usar issues públicos para vulnerabilidades. |
| `.github/dependabot.yml` | Actualizaciones programadas para Maven, GitHub Actions y npm donde corresponda. |
| `GOVERNANCE.md` | Mantenedores, decisiones, versiones soportadas y proceso de relevo; requerido si habrá más de un mantenedor. |

No incluir una dirección de correo, cuenta de patrocinio o canal comunitario que aún no esté controlado. Si no hay canal de soporte, declararlo honestamente y limitar inicialmente el soporte a GitHub Issues.

## 7. Fase 4 — higiene editorial y material interno

### Objetivo legítimo

El repositorio público debe contener documentación final que ayude a usuarios y colaboradores. Debe evitar rastros operativos que no aportan al producto —prompts, planes de generación, logs, evaluaciones internas, rutas de estaciones de trabajo y borradores repetidos— sin falsear procedencia, autoría o historia.

### Procedimiento

1. Sustituir documentos de planificación por una hoja de ruta pública breve (`ROADMAP.md`) que indique alcance, prioridades y estado verificable.
2. Mantener los detalles de diseño que sí son necesarios en ADRs curados dentro de `docs/adr/`, cada uno con problema, decisión, consecuencias y fecha; no transcripciones de sesiones de trabajo.
3. Revisar el tono: frases concretas, ejemplos ejecutables, decisiones justificadas y ausencia de superlativos o promesas sin medición.
4. Revisar metadatos de archivos, comentarios, mensajes de release y plantillas por datos personales o instrucciones internas.
5. Conservar la atribución de cualquier dependencia, tutorial, ejemplo o contribución externa; documentar la procedencia cuando exista una obligación de licencia.

### Prohibiciones

- No falsificar autores de commits, fechas, firmas o declaraciones de copyright.
- No borrar avisos obligatorios para ocultar procedencia.
- No declarar que el proyecto careció de asistencia automatizada si eso no puede afirmarse con veracidad.
- No publicar prompts, registros internos o archivos con datos personales para demostrar un proceso de desarrollo.

## 8. Fase 5 — calidad de ingeniería y evidencia de compatibilidad

La primera publicación debe cerrar los hallazgos de la auditoría de protocolo:

1. Arreglar el job E2E: el workflow debe iniciar el demo correspondiente, esperar un endpoint de salud, ejecutar Playwright y detener el proceso incluso si falla la prueba.
2. Reemplazar el smoke test de `body` visible por contratos de navegador para Spring y Quarkus: visita Inertia, cabeceras, page object, recarga parcial, `only`/`except`, props deferred, mismatch de versión, redirección `303`, errores de validación, SSR y fallback SSR.
3. Crear una suite contractual común. La misma lista de casos debe ejecutarse contra los dos adaptadores para impedir divergencias entre Spring y Quarkus.
4. Convertir la matriz de conformidad en resultado verificable: cada fila debe enlazar a caso/método de prueba y una tarea de CI debe fallar si no existe o no se ejecuta.
5. Endurecer las puertas de calidad gradualmente: Checkstyle/SpotBugs deben fallar ante hallazgos definidos; la cobertura y mutación deben tener umbrales incrementales; OWASP Dependency Check no debe estar omitido en releases.
6. Ejecutar el binario nativo de Spring y Quarkus en CI, realizar una petición HTTP real y publicar solo resultados que hayan completado ese smoke test.
7. Corregir errores silenciosos o conversiones que oculten props en Quarkus y documentar cualquier excepción deliberadamente tolerada.

### Criterio de salida

- CI verde desde un clon limpio en Linux.
- Pruebas unitarias, integración, demos, E2E y nativo reportan resultados separados y conservan artefactos útiles cuando fallan.
- La documentación de compatibilidad coincide exactamente con la evidencia generada.

## 9. Fase 6 — publicación Maven Central y GitHub

1. Registrar y verificar el namespace de Maven Central que pertenezca al titular real, por ejemplo `io.github.<cuenta-controlada>`; no usar un namespace ajeno.
2. Ajustar versiones semánticas: publicar `0.x` mientras la API cambie libremente; reservar `1.0.0` para una API pública estable y la matriz contractual verde.
3. Validar `mvn clean verify`, generación de fuentes/Javadoc, firma GPG, SBOM y publicación de prueba al portal de Central.
4. Configurar secretos exclusivamente en GitHub Actions: claves de firma, credenciales de Central y tokens mínimos. Nunca dejarlos en POM, ejemplos o archivos locales.
5. Configurar el workflow de release para publicar desde tags protegidos `vX.Y.Z`, generar notas revisadas por un mantenedor y adjuntar checksums/SBOM cuando correspondan.
6. Configurar en GitHub: descripción, URL de documentación, topics, licencia detectada, rama protegida, revisiones requeridas, CI obligatoria, Dependabot y advisories de seguridad.
7. Crear una release candidata, instalar los dos artefactos desde Maven Central en proyectos vacíos Spring y Quarkus y repetir los quickstarts del README.

## 10. Secuencia de ejecución y responsables

| Orden | Entregable | Responsable que debe aprobar |
|---:|---|---|
| 1 | Inventario, escaneo de secretos y clasificación de material privado | Propietario del repositorio |
| 2 | Decisión y validación de licencia | Titular legal/copyright |
| 3 | Limpieza de índice, `.gitignore` y documentación pública | Mantenedor técnico |
| 4 | README, guías, políticas y plantillas GitHub | Mantenedor + revisor externo |
| 5 | E2E, suite contractual, calidad y nativo reproducibles | Mantenedor técnico |
| 6 | Metadatos Maven, firma y release candidata | Titular de Central |
| 7 | Revisión desde clon limpio y primera publicación | Dos revisores si están disponibles |

## 11. Lista final de aceptación

- [ ] MIT validada legalmente y consistente en repositorio, artefactos y Maven Central.
- [ ] No hay secretos, rutas locales, prompts, logs ni planes internos en el contenido publicado.
- [ ] README y documentación pública están en inglés claro, tienen ejemplos compilables y enlaces válidos.
- [ ] Hay política de seguridad, conducta, contribución, soporte, issues y PR.
- [ ] La afirmación de compatibilidad se deriva de CI; no hay reclamos de 100% sin E2E contractual verde.
- [ ] Spring y Quarkus pasan la misma suite de protocolo, demos y pruebas nativas reales.
- [ ] CI, Dependabot, protección de rama y publicación firmada están configurados.
- [ ] La release candidata se instaló desde Central en dos proyectos limpios.
- [ ] No se ha falseado la autoría, el historial ni la procedencia del código.

## 12. Información que debe definirse antes de implementar

1. Cuenta u organización GitHub que será titular del repositorio y URL final.
2. Titular legal que aparecerá en MIT y en Maven Central.
3. Correo/canal real para vulnerabilidades y soporte.
4. Política de contribuciones: DCO, CLA o contribución bajo MIT implícita.
5. Demos y clientes frontend que se comprometen a mantener en cada release.
6. Umbrales iniciales de calidad y definición de versiones soportadas.

