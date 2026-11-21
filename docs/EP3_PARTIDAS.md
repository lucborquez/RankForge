# Reglas de programación de partidas

Revisión del 20 de noviembre de 2026.

- Se exige inscripción APROBADA (o `ACEPTADA` en registros históricos). PENDIENTE y CANCELADA no habilitan participación.
- La consulta de inscripciones tiene límites de conexión y lectura de 3 y 5 segundos. Una caída o respuesta nula produce 503, sin convertir un fallo de servicio en una supuesta ausencia de inscripción.
- Crear una partida exige IDs positivos de torneo y participantes, dos participantes distintos, ronda no vacía y fecha/hora ISO válida.
- Se rechaza el mismo enfrentamiento en la misma ronda también con A/B invertidos.
- Reprogramar sólo admite partidas PROGRAMADA, no cambia torneo ni participantes y comprueba enfrentamientos duplicados en la ronda de destino.

Evidencia: 13 pruebas aprobadas (5 del cliente de inscripciones, 4 de reglas y 4 de seguridad). Clientes y repositorios simulados; no sustituyen pruebas con MySQL y servicios activos.

Pendientes: serializar creación simultánea de enfrentamientos, comprobar estado del torneo en cada mutación y coordinar cancelación/reprogramación con resultados ya registrados. La comprobación de duplicados actual es de aplicación y no garantiza exclusión concurrente.
