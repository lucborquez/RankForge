# Pruebas de aceptación integrada EP3

Estado al 21 de noviembre de 2026: pendientes de ejecución con MySQL y servicios activos. Esta lista es un protocolo de prueba, no un informe de casos aprobados.

Registrar en cada ejecución: fecha, commits de frontend/backend, rol, solicitud, respuesta HTTP, datos resultantes y captura sin tokens ni contraseñas. Usar un torneo de prueba y cuentas autorizadas; conservar los datos existentes del equipo.

| Caso | Acción | Resultado esperado | Estado |
| --- | --- | --- | --- |
| E2E01 | Visitante consulta catálogo y detalle | Lectura disponible, sin controles administrativos | Pendiente |
| E2E02 | Visitante intenta escribir juego directamente y por Gateway | 401; sin datos nuevos | Pendiente |
| E2E03 | Jugador inicia sesión, cambia apodo y recarga | Identidad y apodo persistidos; correo y rol no editables | Pendiente |
| E2E04 | Jugador crea equipo y agrega integrante activo | Equipo e integrante persistidos con capitán autenticado | Pendiente |
| E2E05 | Otro jugador intenta editar ese equipo | 403; equipo intacto | Pendiente |
| E2E06 | Capitán intenta retirar al propio capitán | Rechazo; membresía conservada | Pendiente |
| E2E07 | Administrador crea juego y torneo, después lo abre | Configuración persistida y aviso de cambio de estado | Pendiente |
| E2E08 | Jugador inscribe equipo compatible completo | Una inscripción PENDIENTE | Pendiente |
| E2E09 | Inscripción fuera de plazo, sin cupo o con sanción vigente | Rechazo sin inscripción nueva | Pendiente |
| E2E10 | Dos solicitudes simultáneas disputan el último cupo | Sólo una ocupa el cupo; comprobar filas MySQL | Pendiente |
| E2E11 | Organizador aprueba y programa dos participantes | Partida PROGRAMADA persistida | Pendiente |
| E2E12 | Organizador intenta usar inscripción pendiente o cancelada | Rechazo sin partida nueva | Pendiente |
| E2E13 | Organizador registra y valida resultado | Resultado VALIDADO y ranking reconstruido | Pendiente |
| E2E14 | Se repite la validación y la sincronización | No se duplican puntos ni resultados | Pendiente |
| E2E15 | Ranking no responde al validar | Error indica resultado guardado; reintento recupera clasificación | Pendiente |
| E2E16 | Anular resultado con motivo y reconstruir | Se excluye del ranking y conserva justificación | Pendiente |
| E2E17 | Administrador finaliza y asigna premio al puesto correcto | Premio visible y aviso persistido | Pendiente |
| E2E18 | Intentar premio antes de finalizar o para puesto incorrecto | 409; sin premio ni aviso de asignación | Pendiente |
| E2E19 | Recargar detalle del torneo | Premios y avisos recuperados del servidor | Pendiente |
| E2E20 | Cerrar sesión o usar JWT vencido | Escrituras rechazadas; sin cambio de identidad | Pendiente |

## Revisiones de consistencia todavía abiertas

Las partidas incorporan exclusión por torneo para altas, reprogramaciones y cancelaciones; falta probar solicitudes concurrentes con MySQL. Las mutaciones de resultados comprueban el estado remoto y rechazan torneos finalizados, pero la carrera entre esa comprobación y un cierre simultáneo debe coordinarse con premios y clasificación. Los endpoints manuales de escritura de ranking ya se rechazan con 409 para preservar la clasificación derivada; comprobar también ese rechazo en la ejecución integrada. No marcar estas condiciones como aprobadas sólo porque pasen los casos unitarios.

## Evidencia disponible

Las pruebas seleccionadas de game-service, match-service y tournament-service suman 34 casos aprobados en la revisión del 21 de noviembre: 6 de juegos, 13 de partidas y 15 de torneos/premios/avisos. Se simulan repositorios y conexiones HTTP. No son pruebas de concurrencia SQL ni evidencia de respuestas HTTP de una aplicación desplegada.
