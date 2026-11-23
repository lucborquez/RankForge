# Reconstrucción del ranking

`POST /api/rankings/torneo/{torneoId}/recalcular` requiere JWT de ORGANIZADOR o ADMIN. Consulta las partidas del torneo y sus resultados, considera únicamente VALIDADO y sustituye la clasificación dentro de una transacción. No incrementa una tabla anterior: repetir la solicitud conserva los mismos puntajes. Los IDs técnicos de las filas pueden cambiar.

Reglamento base implementado: victoria 3 puntos, empate 1, derrota 0. Orden: puntos, diferencia de marcador, victorias y, finalmente, ID de participante. Este puntaje es una decisión de implementación; la guía no especifica un sistema numérico. Resultados repetidos idénticos no se cuentan dos veces; resultados validados contradictorios de la misma partida bloquean la reconstrucción.

`ranking_torneo_lock` serializa reconstrucciones por torneo mediante un upsert MySQL/InnoDB, incluyendo torneos sin clasificación previa. La fuente remota se consulta después de adquirir el bloqueo. Una consulta fallida conserva la tabla anterior; los fallos SQL deben revertir la transacción. Configuración: `match.service.url` (por defecto localhost:8087) y `result.service.url` (localhost:8088).

Verificación: 11 pruebas unitarias aprobadas entre cálculo, reconstrucción y autorización. Los repositorios y fuentes son simulados; falta comprobar el bloqueo y rollback con MySQL real. Las modificaciones manuales del CRUD anterior todavía no comparten este bloqueo.

La validación y anulación de result-service llaman ahora a este endpoint después de guardar el cambio, reenviando Authorization. Configurar `ranking.service.url` (por defecto localhost:8089). La operación exterior no mantiene una transacción abierta mientras ranking vuelve a consultar los resultados.

Si falla la sincronización, result-service responde 503 indicando que el resultado ya quedó guardado. Repetir PATCH validar sobre VALIDADO, o PATCH anular sobre ANULADO con justificación no vacía, vuelve a sincronizar sin modificar nuevamente el resultado ni su justificación. El resultado no se revierte por una caída de otro servicio. No hay entrega automática diferida: una caída requiere reintentar o invocar la reconstrucción autorizada.

Result-service pasa 15 pruebas unitarias, incluidas cuatro de orden de llamadas, reenvío de autorización, fallo/reintento y anulación. Falta comprobar el flujo HTTP y persistencia reales, concurrencia entre edición y validación, y recuperación desde la interfaz después de recargar la página. No se afirma que la integración desplegada esté verificada.
# Clasificación derivada y escrituras heredadas

Actualización del 21 de noviembre de 2026: la clasificación sólo se modifica reconstruyendo resultados validados mediante `POST /api/rankings/torneo/{torneoId}/recalcular`. Los antiguos endpoints de creación, edición, reinicio y eliminación manual conservan sus rutas, pero rechazan la operación con 409 y explican cómo recalcular. La autorización JWT se aplica antes de llegar a esa regla.

Así se evita que una escritura manual compita con la reconstrucción o introduzca puntos sin resultado validado. Para corregir la clasificación se corrige o anula el resultado mediante el flujo autorizado y se reconstruye el torneo. Esta modificación no cambia los endpoints de lectura ni el cálculo de puntos. No existen llamadas a los antiguos endpoints de escritura desde las pantallas EP3.

Quince pruebas de ranking aprobadas: cuatro de cálculo, cuatro de seguridad, tres de reconstrucción y cuatro que comprueban el rechazo de escrituras manuales sin acceder al repositorio. Las pruebas siguen utilizando repositorios y clientes simulados. La consistencia con premios después de finalizar y las pruebas SQL reales continúan pendientes.
