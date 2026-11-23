# Reglas de programación de partidas

Revisión del 20 de noviembre de 2026.

- Se exige inscripción APROBADA (o `ACEPTADA` en registros históricos). PENDIENTE y CANCELADA no habilitan participación.
- La consulta de inscripciones tiene límites de conexión y lectura de 3 y 5 segundos. Una caída o respuesta nula produce 503, sin convertir un fallo de servicio en una supuesta ausencia de inscripción.
- Crear una partida exige IDs positivos de torneo y participantes, dos participantes distintos, ronda no vacía y fecha/hora ISO válida.
- Se rechaza el mismo enfrentamiento en la misma ronda también con A/B invertidos.
- Reprogramar sólo admite partidas PROGRAMADA, no cambia torneo ni participantes y comprueba enfrentamientos duplicados en la ronda de destino.

Evidencia: 13 pruebas aprobadas (5 del cliente de inscripciones, 4 de reglas y 4 de seguridad). Clientes y repositorios simulados; no sustituyen pruebas con MySQL y servicios activos.

Actualización del 22 de noviembre: crear, reprogramar y cancelar consultan el estado del torneo. Sólo admiten ABIERTO, CERRADO o EN_CURSO; FINALIZADO, CANCELADO y BORRADOR se rechazan con 409. Fallos HTTP o respuestas incompletas producen 503 y no autorizan cambios. El cliente utiliza límites de conexión/lectura de 3/5 segundos. Se comprobaron 22 pruebas seleccionadas, incluidas las tres mutaciones bloqueadas ante un torneo finalizado.

Pendientes: serializar creación simultánea de enfrentamientos y coordinar cancelación/reprogramación con resultados ya registrados. La consulta del estado no constituye una transacción distribuida: un cierre simultáneo exige coordinación adicional. La comprobación de duplicados actual es de aplicación y no garantiza exclusión concurrente.

## Serialización de altas y reprogramaciones

Crear y reprogramar adquieren un bloqueo por torneo en la tabla partida_torneo_locks mediante un upsert MySQL/InnoDB, dentro de una transacción READ_COMMITTED. El bloqueo permanece hasta commit/rollback y precede a la consulta de enfrentamientos duplicados. Las operaciones del mismo torneo comparten esa fila, incluso al crear la primera partida. Así, la comprobación incluye las escrituras confirmadas por la operación anterior y compara ambos órdenes de participantes.

Las 23 pruebas seleccionadas de match-service pasan el 22 de noviembre; incluyen rechazo de escritura cuando no puede adquirirse el bloqueo. La verificación de dos solicitudes concurrentes sobre MySQL real sigue pendiente: los colaboradores de estas pruebas son simulados. Este bloqueo local no coordina el cierre del torneo, la cancelación simultánea de una partida ni los resultados de otro servicio. Las filas del bloqueo se conservan mientras exista el torneo; Hibernate crea la tabla con ddl-auto=update.
