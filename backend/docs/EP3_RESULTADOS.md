# Resultados: reglas y autorización

Las escrituras de `/api/resultados` requieren JWT HS256 firmado, vigente, con subject y rol ADMIN u ORGANIZADOR. El filtro vive en result-service y protege también el acceso directo, sin depender únicamente del Gateway. Configurar `JWT_SECRET` con el mismo secreto Base64 de autenticación. Lecturas y preflight permanecen disponibles.

La creación y edición comprueban puntajes no negativos, dos participantes distintos, partida no cancelada y correspondencia entre marcador y ganador. Empate requiere ganador nulo. La edición solo admite PENDIENTE y no cambia la partida. Antes de validar se vuelven a comprobar las reglas, incluidos resultados antiguos. Anular requiere justificación no vacía.

Diecisiete pruebas unitarias aprobadas: nueve de reglas, cuatro de seguridad y cuatro de sincronización del ranking. No se ejecutó integración con servidores activos ni MySQL. Crear usa una fila de bloqueo por partida en resultado_partida_lock antes de comprobar duplicados; editar, validar y anular bloquean la fila del resultado mediante PESSIMISTIC_WRITE. Estas operaciones mantienen una transacción READ_COMMITTED. Validar/anular repetidamente conserva el estado y la justificación. La orquestación no precarga la entidad antes del bloqueo y sincroniza el ranking después del commit. Falta verificar estos bloqueos, la unicidad y el flujo completo contra MySQL real. Las pruebas actuales simulan los repositorios; no demuestran concurrencia SQL. No se certifica la entrega final con estas pruebas.

## Resultados y cierre del torneo (22 de noviembre)

Crear, editar, validar por primera vez y anular por primera vez un resultado consulta el estado del torneo en tournament-service. Sólo ABIERTO, CERRADO y EN_CURSO permiten la mutación. FINALIZADO, CANCELADO y BORRADOR producen 409 antes de guardar. Una consulta sin datos fiables o una caída produce 503; un torneo inexistente, 404. El cliente limita conexión a 3 segundos y lectura a 5 segundos, con URL configurable mediante TOURNAMENT_SERVICE_URL.

Repetir una validación ya aplicada o una anulación ya aplicada conserva su idempotencia y permite reintentar la sincronización del ranking. No cambia el resultado ni el motivo original. La consulta remota no bloquea simultáneamente el cierre en otro servicio: la carrera distribuida entre comprobación y guardado permanece pendiente de solución y de aceptación real. Tampoco sustituye el control de finalización con resultados pendientes.

Verificación: 24 pruebas seleccionadas de result-service pasan (reglas, seguridad, sincronización y estado remoto), incluyendo rechazo de las cuatro mutaciones sin guardar ni alterar el estado ante torneo finalizado. Son pruebas unitarias con colaboradores simulados; no constituyen evidencia HTTP/MySQL.

## Errores al consultar partidas

MatchClient distingue partida inexistente (404) de servicio caído o respuesta incompleta/inconsistente (503). Comprueba ID solicitado, torneo positivo y estado informado antes de aceptar la respuesta. Se retiró la consulta booleana antigua sin consumidores, que confundía inexistencia con indisponibilidad. Las 28 pruebas seleccionadas de result-service pasaron el 22 de noviembre a las 23:30; cuatro casos nuevos verifican respuesta coherente, respuesta incompleta/distinta, caída y 404. No se trata de ejecución integrada real.
