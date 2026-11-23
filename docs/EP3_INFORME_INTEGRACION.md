# Informe de integración RankForge EP3

Revisión del 22 de noviembre de 2026. Responsable de la integración descrita: Lucas Borquez. Se conservan los aportes de Nicolás Lara y Jhon Olivares en las ramas de trabajo del equipo.

## Resultado y alcance

Se conectaron las pantallas operativas de React a los servicios Spring Boot y se corrigieron controles de identidad, permisos y reglas del torneo. El código incluye pruebas automatizadas seleccionadas y documentación de arranque. No se ha completado la aceptación con MySQL y todos los servicios activos, por lo que no se declara la entrega terminada.

Los repositorios de trabajo son Frontend-eSports-Arena-Manager de jzzzn2110 y game de lucborquez, ambos en `feature/lucas-ep3`. Los repositorios docentes GameHubStore son referencias. Main no se presenta como una integración EP3 aprobada.

## Cambios realizados

| Área | Cambio | Efecto esperado |
| --- | --- | --- |
| Identidad | JWT y perfil propio del servidor | Las operaciones usan el ID real del usuario autenticado |
| Perfil | Edición de nombre y apodo, correo y rol no editables | Evita modificar identidad o privilegios desde el formulario |
| Equipos | Permisos de capitán o administrador y eliminación individual de miembro | Preserva el capitán y limita modificaciones ajenas |
| Inscripciones | Plazo, cupo, duplicados, tamaño del equipo, modalidad y sanciones | Rechaza participantes no elegibles y evita IDs de demostración |
| Organización | Aprobación, programación y reprogramación de partidas | Permite operar inscripciones y calendario desde la interfaz |
| Resultados | Ganador coherente, validación, anulación justificada y bloqueo de edición | Controla el paso desde resultado pendiente a clasificación |
| Ranking | Reconstrucción desde resultados validados y rechazo de edición manual | Evita acumular puntos por reintento o editar estadísticas sin resultado |
| Recuperación | Distingue resultado guardado de ranking pendiente | Permite reintentar sincronización sin volver a crear resultados |
| Premios | Asignación ADMIN después de finalizar y comprobación del puesto | Publica premios asociados a la clasificación consultada |
| Avisos | Historial público persistido del torneo | Registra cambios de estado y asignación de premios |
| Seguridad | Verificación JWT también en servicios de operación y catálogo | Evita eludir permisos accediendo directamente a puertos internos |
| Dependencias | Vite 6.4.6 y renovación compatible del archivo de bloqueo | Corrige alertas altas detectadas en la instalación anterior |

## Evidencia ejecutada y sus límites

Frontend, 21 de noviembre: `npm ci`, `npm test` y `npm run build` correctos. Se aprobaron 149 pruebas; cobertura de líneas 71,34 %, sentencias 70,86 %, ramas 62,97 % y funciones 67,85 %. El build procesó 77 módulos. Los servicios se simulan en las pruebas de interfaz.

Backend: los informes Surefire de las suites seleccionadas registran pruebas de perfil, equipos, inscripciones, sanciones, resultados, ranking, juegos, partidas y torneos. Cada documento de área especifica sus casos y resultados. No se presenta ese conjunto como una ejecución de todos los contextos Spring ni como prueba de MySQL. Las pruebas recientes de partidas verifican rechazo ante torneos finalizados, cancelados o en borrador.

Empaquetado del 22 de noviembre, código `0beded1`: los doce módulos completaron `mvnw -DskipTests package` con JDK 26 y generaron sus JAR. Incluye Eureka, Gateway y los diez servicios de negocio. Este comando compila y empaqueta; omite ejecutar pruebas y no inicia los contextos ni comprueba conexiones a MySQL. Los resultados unitarios anteriores son evidencia separada.

Las llamadas HTTP fallidas se manejan explícitamente en los flujos corregidos. Los bloqueos y transacciones implementados necesitan verificación con SQL real y solicitudes concurrentes. La consistencia entre servicios no queda garantizada por una transacción local de JPA.

Auditoría npm del 21 de noviembre: dos avisos moderados ligados a minimatch y karma-webpack, sin avisos altos ni críticos. La actualización forzada de minimatch rompía glob y se descartó. Se conservó el árbol compatible y se repitieron instalación, pruebas y build. Véase EP3_DEPENDENCIAS del frontend.

## Ejecución de aceptación pendiente

La guía EP3_ARRANQUE_LOCAL explica MySQL, Java, variables, orden de servicios y frontend. El protocolo EP3_ACEPTACION_INTEGRADA define veinte casos positivos y negativos por rol. Todavía no hay evidencias de solicitudes HTTP reales ni capturas de ese recorrido; no se reutilizan las capturas de octubre para aparentar integración EP3.

Antes de cerrar la entrega deben verificarse con MySQL los bloqueos implementados para crear, reprogramar y cancelar partidas; también debe resolverse la coordinación del cierre simultáneo con resultados y premios. La [existencia del destinatario de sanciones](EP3_DESTINATARIO_SANCIONES.md) ya se consulta antes de crear y requiere aceptación integrada. Los resultados rechazan mutaciones cuando la consulta remota informa un torneo finalizado, sin garantía de atomicidad entre servicios. También deben comprobarse la persistencia al recargar, la expiración de sesión, los permisos por acceso directo y la recuperación del ranking tras fallos.

## Documentación y publicación

El frontend contiene ERS-V3-EP3 y un manual DOCX de revisión con cinco páginas. Su formato se comprobó visualmente; aún requiere capturas numeradas de la ejecución real. La [planilla XLSX de APIs](EP3_APIs/RankForge_APIs_EP3.xlsx) documenta 78 rutas con respuestas esperadas por código y aceptación HTTP pendiente. Falta incorporar evidencia observada y generar los dos ZIP sin secretos ni dependencias instaladas.

Los commits conservan autoría y hora reales, separados al menos diez minutos entre ambos repositorios. La integración final requiere revisar los PR y sus comprobaciones. No se debe fusionar main como entrega completa mientras los criterios pendientes anteriores sigan abiertos.
