# Inscripciones: reglas y permisos implementados

La creación individual utiliza el ID obtenido del perfil autenticado; no permite inscribir a otro jugador mediante un ID en el cuerpo. Para inscribir un equipo se exige capitán o administrador. La cancelación exige propietario, capitán u organizador/administrador. Cambiar estado y listar todas las inscripciones exige organizador/administrador, también al acceder directamente al microservicio. El historial de jugador está restringido a su propietario o al organizador/administrador. El listado por torneo permanece público.

Se comprueba torneo abierto, fecha de cierre en America/Santiago, cupo disponible, identificadores válidos y ausencia de inscripción vigente. Una inscripción cancelada permite volver a inscribirse. Los estados admitidos son PENDIENTE, APROBADA y CANCELADA.

Para equipos se verifica estado activo, juego compatible, cantidad exacta de integrantes según jugadoresPorEquipo del juego, ausencia de miembros repetidos, capitán incluido, integrantes activos y ausencia de sanciones activas tanto del equipo como de sus miembros. La indisponibilidad o respuesta vacía de sanciones bloquea la solicitud con HTTP 503; no se interpreta como ausencia de sanciones.

## Verificación y límites

32 pruebas unitarias aprobadas en registration-service: plazo (5), reglas de inscripción (6), autorización (5), reglas de equipo y tipo de participación (11) y cliente de sanciones (5). En sanction-service pasan otras 7 pruebas de vigencia temporal. Los servicios remotos se simulan: falta validación E2E con MySQL y servicios activos.

Las sanciones se consultan con vigencia temporal centralizada en sanction-service: estado abierto, fecha inicial y final inclusivas, y día local de America/Santiago. El endpoint `/api/sanciones/equipo/{id}/activa` evita que cada consumidor interprete las fechas de manera distinta. Sanciones futuras, vencidas o cerradas no bloquean; registros abiertos sin fechas requieren revisión y mantienen el bloqueo.

La creación incorpora una transacción READ_COMMITTED y una fila de bloqueo por torneo en `inscripcion_cupos`. El upsert MySQL/InnoDB bloquea esa fila hasta finalizar la transacción; el conteo, la comprobación de duplicados y el alta quedan dentro de ella. Los clientes remotos tienen límites de conexión de 3 segundos y lectura de 5 segundos para evitar esperas indefinidas durante la operación.

Esta protección de concurrencia está implementada pero todavía requiere prueba de solicitudes simultáneas contra MySQL: las pruebas unitarias simulan el repositorio de bloqueos. En la comprobación del 19/11, MySQL local no aceptó conexión en 127.0.0.1:3306. No se afirma haber verificado el bloqueo real ni la coordinación con modificaciones del torneo en otro microservicio.

El tipo de inscripción se determina por jugadoresPorEquipo del juego activo: 1 exige JUGADOR y más de 1 exige EQUIPO. No se interpreta el texto libre de modalidad del torneo (por ejemplo 5v5 o Battle Royale) como un enum de tipo de participante. La pantalla real de inscripción está conectada en frontend bc55a66, pero falta probar la integración con los servicios activos. Estas pruebas no certifican todavía el flujo completo ni la entrega final.
