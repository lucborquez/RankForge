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
