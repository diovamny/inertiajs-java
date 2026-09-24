# Reauditoría del plan C100 / I100 / R100 — `release-0.0.5`

**Fecha:** 2026-09-23  
**Rama y commit auditados:** `release-0.0.5` / `9f2ba77`  
**Base de comparación:** `docs/audits/PLAN_EJECUCION_100_PORCIENTO_INERTIA_V3.md` y la política de compatibilidad vigente.

## Dictamen

**No, el plan no está completado al 100 % y no es correcto anunciar compatibilidad total ni nueve celdas E2E verdes.**

La rama avanzó mucho respecto a `0.0.4`: hay una implementación real de errores múltiples, `MergePlan`, política SSR, TCK en tres transportes, endurecimiento de Reactive y automatización de release. El reactor Java completo pasó en una ejecución limpia. Sin embargo, las condiciones de salida que el propio plan define para interoperabilidad y release siguen abiertas.

| Dimensión | Resultado medible | Veredicto |
|---|---:|---|
| **C100 — contrato servidor** | 60 de 62 filas `TESTED` en la matriz (96,8 %) | Muy avanzado, pero no 100 %: dos filas siguen solo `IMPLEMENTED`. |
| **I100 — clientes oficiales** | La matriz E2E declara solo 14 de 90 pares escenario/celda como verificados; React, Svelte y SSR siguen sin cobertura completa. | No aprobado. |
| **R100 — release reproducible** | Wrapper, CI y SBOM existen; PIT no bloquea, SCA no se ejecuta en PR/main, no hay RC/revisión externa y existe un problema Windows de caché/ruta. | No aprobado. |

No se asigna una cifra global artificial: sumar una buena cobertura TCK a una interoperabilidad de navegador incompleta ocultaría el riesgo. Para poder declarar **100 %**, las tres dimensiones deben aprobarse simultáneamente.

## Evidencia ejecutada en esta auditoría

| Comprobación | Resultado |
|---|---|
| `npm run verify:metadata` | Correcto: versión `0.0.5` coherente; matriz generada `62 rows, 60/62 verified, 8 E2E`; 125 referencias de prueba resueltas. |
| `mvnw.cmd -version` | Correcto: Maven Wrapper 3.3.2 descarga Maven 3.9.11; JDK 21.0.2. |
| `mvnw.cmd clean test` con repositorio Maven temporal ASCII | **BUILD SUCCESS**. Core 107, TCK 7, Quarkus 331, Spring 213, Spring Security 14 y Quarkus Security 29: **701 pruebas, 0 fallos**. Duración 22m35s por primera descarga/arranque. |
| Inspección de matriz y Playwright | `specs/e2e-compliance.yaml` conserva estados `PARCIAL` en 9 de 10 escenarios y `NO_EVALUADO` para SSR. |
| Inspección de CI/release | Checkstyle y SpotBugs se ejecutan; PIT permanece como informe no bloqueante y Dependency-Check se omite en CI de PR/main. |

La ejecución Java anterior con un repositorio Maven bajo la ruta de usuario que contiene `ñ` falló antes de las pruebas con `AccessDeniedException` de `ZipFileSystem` al cerrar JARs válidos. La misma ejecución pasó al usar `C:\temp\inertia-audit-m2`. Esto no demuestra un defecto de protocolo, pero sí impide considerar probado el requisito de reproducibilidad Windows para la instalación real del mantenedor.

## Qué sí se completó correctamente

### M0 y M1: base de gobernanza y build

- Alcance v3-only, línea base congelada, política de compatibilidad y generación de matriz están presentes.
- Se añadieron Maven Wrapper, versiones verificables y un job de checkout limpio para Windows/Linux.
- La validación de metadatos detecta desalineación entre POM, README, CHANGELOG, ROADMAP, ejemplos y arquetipos.

### M2: errores de validación con múltiples mensajes

- `ValidationErrors` conserva orden y emite `Map<String, List<String>>` al activar `inertia.validation.all-errors`.
- Spring, Quarkus REST y Reactive lo cubren mediante TCK y pruebas específicas, incluidas `ValidationAllErrorsIntegrationTest` y `ValidationAllErrorsQuarkusTest`.
- La fila `PROTO-053B` ya puede contar para **C100**. Aún no cuenta para **I100**: la evidencia del navegador se limita a Vue/Spring y no comprueba todas las combinaciones ni todos los clientes oficiales.

### M3: API explícita de merge

- Existen `MergePlan`, `MergeableBuilder` y el DSL `mergeable(...).append(...).prepend(...).matchOn(...)`.
- El TCK `08-merge.yaml` cubre metadata de rutas anidadas en los tres transportes.
- La fila `PROTO-054B` puede contar para **C100**. Su evidencia de navegador es solo el append observable de Vue/Spring; faltan las rutas anidadas, prepend/deep merge/matchOn y el resto de celdas para **I100**.

### M5: frontera SSR más segura

- `SsrEndpointPolicy` y los validadores de configuración restringen destinos SSR y prueban el fallback.
- Las suites `SsrEndpointPolicyTest`, `SsrEndpointValidatorTest`, `SsrClientSidecarTest` y equivalentes Quarkus pasan.
- Esto cierra la parte de política/servidor; **no** sustituye una prueba de sidecar real, HTML SSR e hidratación de cliente. `E2E-09-ssr` sigue explícitamente `NO_EVALUADO`.

### M7: mejoras reales, graduación no demostrada

- El fix de aislamiento de contexto y `ReactiveStressTest` son avances valiosos; la suite Quarkus completa pasó con 331 pruebas.
- La documentación explica la divergencia de ruta local del harness y añade un workflow nocturno.
- No obstante, esta evidencia no satisface el gate original de M7: M4 y M6 eran dependencias, se pedían 30 días de nightly verde y dos RC consecutivas. `AUDIT_RELEASE_0.0.5_M7.md` cambió unilateralmente esa condición por un “ciclo intensivo”. Eso puede ser una decisión de producto, pero no permite afirmar que se completó el plan original.

## Hallazgos abiertos, en orden de prioridad

### P0 — impiden I100/R100 y cualquier afirmación de 100 %

1. **M4 no está completado.** La propia fuente de verdad dice que React y Svelte son `TBD M4b`; `E2E-09-ssr` no fue evaluado; los otros escenarios son `PARCIAL`. La suma declarada de `verified_cells` es 14/90, no una matriz completa. El ticket `TICKET_M4b_QUARKUS_E2E_LOGIN.md` documenta que el login del kitchen-sink Quarkus deja el cliente en blanco tras el POST.
2. **La documentación pública contradice esa evidencia.** `ROADMAP.md` afirma “Playwright 9-cell contracts” y M4 verde, mientras `specs/e2e-compliance.yaml` y el ticket M4b indican lo contrario. `docs/NOT_SUPPORTED.md` conserva además texto viejo que dice que `append()` y los errores múltiples aún no existen. README/roadmap/matriz no dicen lo mismo, incumpliendo M0 y la condición final del plan.
3. **PIT no es un gate.** `release.yml` ejecuta primero con `-DskipMutationAnalysis=true`; después ejecuta PIT con `continue-on-error: true`. Los umbrales siguen siendo 5 % de mutación y 20 % de cobertura. R100 exige un gate efectivo, no un informe que pueda fallar sin detener el release.
4. **No existe la certificación R100.** Faltan una RC reproducible, pruebas E2E completas, native smoke de la RC, publicación draft comprobable, revisión externa documentada y cierre formal de P0/P1.

### P1 — riesgo técnico o de exactitud que debe resolverse antes de graduar

1. **Reactive está anunciado como estable sin cumplir su gate.** `COMPATIBILITY_POLICY.md`, `ROADMAP.md`, `NOT_SUPPORTED.md` y `reactive-parity.md` lo llaman estable, pero no hay 30 días ni dos RC. Durante la suite apareció además un aviso real de Vert.x: el event loop quedó bloqueado aproximadamente 2,7 s mientras `QuteSerializer` serializaba JSON. No es un fallo de prueba, pero exige perfilar y mover/evitar trabajo bloqueante antes de vender Reactive como estable.
2. **La matriz confunde contrato con interoperabilidad.** El generador dibuja el mismo ✅ para `TESTED` y coloca la columna Spring/Quarkus como satisfecha aunque la política aclara que TCK cuenta para C100, no para I100. Debe generar dos indicadores separados y fallar si alguien declara “I100” sin E2E oficial enlazado.
3. **La evidencia Quarkus E2E es internamente inconsistente.** Algunas celdas Quarkus-Vue aparecen como verificadas en YAML, mientras el ticket M4b dice que no debe marcarse ninguna celda Quarkus E2E hasta resolver login. Elegir una sola verdad y regenerar la evidencia antes de publicar.
4. **Build reproducible en Windows con ruta Unicode.** La ejecución normal de esta auditoría falló sobre JARs correctos con `AccessDeniedException`; solo pasó con una caché fuera de la ruta con `ñ`. Añadir una prueba/guía reproducible con el perfil del usuario real, fijar la causa (bloqueo antivirus, ZipFS, concurrencia o codificación) y no depender de una ruta alternativa oculta.
5. **Split package Quarkus.** El build avisó que `io.github.diovamny.quarkus.inertia.security` está en `quarkus-inertia` y `quarkus-inertia-security`. Evitar paquetes divididos: mover la API de bridge a un paquete propio o mantenerla en un único artefacto. Puede causar conflictos de classpath, modularidad y native image.
6. **SCA aún no cubre PR/main.** El job principal invoca `-DskipDependencyCheck=true`. La estrategia fail-closed para tags mejora el release, pero R100 pedía análisis continuo, evidencia de una ejecución exitosa y política de respuesta; aún no existen esas pruebas aquí.

### P2 — deuda que conviene cerrar durante M6/M8

1. M6 solo registró un inventario de decisiones; no hay extracción incremental de lógica duplicada a `inertia-core` ni pruebas de caracterización que prueben que cada cambio de protocolo se implementa una sola vez.
2. Las pruebas muestran configuraciones CSRF obsoletas y Mockito se auto-adjunta dinámicamente. Ambos emiten avisos con JDK 21 y el segundo dejará de estar permitido por defecto en un JDK futuro. Modernizar configuración de tests y declarar el agente de Mockito.
3. Los errores esperados se escriben con stack traces de nivel `ERROR` en pruebas de excepciones. Reducir ese ruido o etiquetarlo claramente evita que un fallo real quede oculto en logs de CI.
4. La primera inicialización de Quarkus y Spring Security es costosa (Quarkus tardó ~339 s para el primer arranque y Spring Security ~113 s); conservar cachés/artefactos y publicar tiempos de referencia para detectar regresiones, sin convertirlo todavía en un gate de rendimiento.

## Plan de cierre, en el orden correcto

1. **Corregir M4b-01.** Reproducir el POST de login Quarkus en HTTP de prueba, validar cookie/CSRF/sesión y añadir una prueba Playwright que falle antes y pase después. No marcar la celda hasta que pase.
2. **Crear fixtures oficiales React y Svelte.** Cada fixture debe usar el paquete oficial v3, no una emulación. Conectar Spring MVC, Quarkus REST y Reactive a cada cliente.
3. **Hacer de la matriz E2E un gate, no una declaración.** Parametrizar 10 escenarios × 9 celdas, adjuntar trace/video/log de fallos y calcular I100 solo a partir de los resultados de Playwright. Añadir sidecar SSR, hidratación y fallback para `E2E-09`.
4. **Restaurar la verdad pública.** Mientras 1–3 no estén verdes, cambiar ROADMAP, política, Not Supported y la auditoría M7 para decir: M4 en curso, I100 no aprobado y Reactive experimental. Si se desea cambiar el gate de M7, modificar el plan con nueva fecha, justificación y aceptación explícita; no reescribir el resultado como si el gate anterior se hubiera cumplido.
5. **Resolver los dos riesgos Quarkus.** Perfilar el bloqueo del event loop y eliminar el split package. Añadir pruebas de carga que fallen ante bloqueo de event loop y una verificación de packaging/nativo.
6. **Cerrar M6.** Extraer decisiones puras compartidas con pruebas de caracterización antes/después y una ADR por cada decisión que permanezca específica de framework.
7. **Cerrar M8.** Obtener un PIT funcional en Linux, aumentar gradualmente sus umbrales, eliminar `continue-on-error`, ejecutar SCA en PR/main y tag, y exigir resultados antes de `deploy`.
8. **Certificar R100.** Ejecutar una RC en clones nuevos Windows/Linux —incluida una ruta Unicode—, E2E estable completo, SSR, arquetipos, native smoke, SBOM, firmas/hashes/provenance y revisión de una persona ajena al cambio. Solo entonces cerrar la candidatura.

## Condición objetiva para una próxima reauditoría verde

- 62/62 requisitos C100 en `TCK_VERIFICADO` o `E2E_VERIFICADO`, sin estados `IMPLEMENTED`, `PARCIAL` ni `NO_EVALUADO` dentro del alcance anunciado.
- Todos los escenarios E2E aplicables verdes con Vue, React y Svelte en Spring MVC, Quarkus REST y, si se anuncia estable, Quarkus Reactive; SSR probado extremo a extremo.
- Matrix, README, ROADMAP, sitio y Not Supported generados desde una única fuente y sin mensajes contradictorios.
- PIT y SCA bloqueantes, releases verificables y una RC revisada externamente.
- Reactive mantiene los criterios temporales/RC establecidos o se presenta honestamente como experimental.

## Referencias normativas

- [Protocolo Inertia v3](https://inertiajs.com/docs/v3/core-concepts/the-protocol)
- [Validación en Inertia v3](https://inertiajs.com/docs/v3/the-basics/validation)
- [Merging props en Inertia v3](https://inertiajs.com/docs/v3/data-props/merging-props)
- [Instant visits en Inertia v3](https://inertiajs.com/docs/v3/advanced/instant-visits)

