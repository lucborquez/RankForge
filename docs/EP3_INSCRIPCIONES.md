# Inscripciones: reglas y permisos implementados

La creación individual utiliza el ID obtenido del perfil autenticado; no permite inscribir a otro jugador mediante un ID en el cuerpo. Para inscribir un equipo se exige capitán o administrador. La cancelación exige propietario, capitán u organizador/administrador. Cambiar estado y listar todas las inscripciones exige organizador/administrador, también al acceder directamente al microservicio. El historial de jugador está restringido a su propietario o al organizador/administrador. El listado por torneo permanece público.

Se comprueba torneo abierto, fecha de cierre en America/Santiago, cupo disponible, identificadores válidos y ausencia de inscripción vigente. Una inscripción cancelada permite volver a inscribirse. Los estados admitidos son PENDIENTE, APROBADA y CANCELADA.

Para equipos se verifica estado activo, juego compatible, cantidad exacta de integrantes según jugadoresPorEquipo del juego, ausencia de miembros repetidos, capitán incluido, integrantes activos y ausencia de sanciones activas tanto del equipo como de sus miembros. La indisponibilidad o respuesta vacía de sanciones bloquea la solicitud con HTTP 503; no se interpreta como ausencia de sanciones.

## Verificación y límites

26 pruebas unitarias aprobadas: plazo (5), reglas de inscripción (6), autorización (5), reglas de equipo (7) y fallo del cliente de sanciones (3). Los servicios remotos se simulan: falta validación E2E con MySQL y servicios activos.

Pendiente garantizar cupos y duplicados ante solicitudes concurrentes, comprobar modalidad del torneo y revisar la vigencia temporal de sanciones, además de conectar la pantalla real de inscripción. Estas pruebas no certifican todavía el flujo completo ni la entrega final.
