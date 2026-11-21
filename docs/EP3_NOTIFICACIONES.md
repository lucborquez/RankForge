# Notificaciones de torneos

Avisos públicos persistidos en tournament-service. `GET /api/torneos/{torneoId}/notificaciones` devuelve hasta 100 avisos, del más reciente al más antiguo, o 404 si el torneo no existe. Cada aviso contiene id, torneoId, tipo, mensaje y fecha UTC.

Se genera un aviso al cambiar el estado del torneo y al asignar un premio. El servicio de publicación exige una transacción existente: aviso y operación se guardan en la misma transacción. Si falla la persistencia, deben revertirse ambos. Esta garantía todavía requiere prueba con MySQL real.

No hay un endpoint público para crear avisos arbitrarios. El usuario consulta y actualiza la lista desde el detalle del torneo. Los mensajes se representan como texto, sin interpretar HTML. Es un historial público del torneo, no correo, notificación push ni bandeja privada por usuario.

Evidencia: 15 pruebas de tournament-service aprobadas el 20 de noviembre (5 premios, 5 seguridad y 5 notificaciones). Comprueban torneo inexistente, listado por torneo, fecha del aviso, emisión tras cambio válido, ausencia de emisión ante transición inválida y emisión al asignar premio. Los repositorios están simulados; no se ha probado todavía el recorrido con servicios activos.
