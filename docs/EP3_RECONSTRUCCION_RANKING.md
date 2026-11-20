# Reconstrucción del ranking

`POST /api/rankings/torneo/{torneoId}/recalcular` requiere JWT de ORGANIZADOR o ADMIN. Consulta las partidas del torneo y sus resultados, considera únicamente VALIDADO y sustituye la clasificación dentro de una transacción. No incrementa una tabla anterior: repetir la solicitud conserva los mismos puntajes. Los IDs técnicos de las filas pueden cambiar.

Reglamento base implementado: victoria 3 puntos, empate 1, derrota 0. Orden: puntos, diferencia de marcador, victorias y, finalmente, ID de participante. Este puntaje es una decisión de implementación; la guía no especifica un sistema numérico. Resultados repetidos idénticos no se cuentan dos veces; resultados validados contradictorios de la misma partida bloquean la reconstrucción.

`ranking_torneo_lock` serializa reconstrucciones por torneo mediante un upsert MySQL/InnoDB, incluyendo torneos sin clasificación previa. La fuente remota se consulta después de adquirir el bloqueo. Una consulta fallida conserva la tabla anterior; los fallos SQL deben revertir la transacción. Configuración: `match.service.url` (por defecto localhost:8087) y `result.service.url` (localhost:8088).

Verificación: 11 pruebas unitarias aprobadas entre cálculo, reconstrucción y autorización. Los repositorios y fuentes son simulados; falta comprobar el bloqueo y rollback con MySQL real. Las modificaciones manuales del CRUD anterior todavía no comparten este bloqueo.

Pendiente conectar la validación/anulación de result-service a este endpoint con Authorization, gestionar reintentos y probar el flujo integrado. La existencia de este endpoint todavía no acredita actualización automática al validar resultados.
