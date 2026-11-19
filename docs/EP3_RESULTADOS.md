# Resultados: reglas y autorización

Las escrituras de `/api/resultados` requieren JWT HS256 firmado, vigente, con subject y rol ADMIN u ORGANIZADOR. El filtro vive en result-service y protege también el acceso directo, sin depender únicamente del Gateway. Configurar `JWT_SECRET` con el mismo secreto Base64 de autenticación. Lecturas y preflight permanecen disponibles.

La creación y edición comprueban puntajes no negativos, dos participantes distintos, partida no cancelada y correspondencia entre marcador y ganador. Empate requiere ganador nulo. La edición solo admite PENDIENTE y no cambia la partida. Antes de validar se vuelven a comprobar las reglas, incluidos resultados antiguos. Anular requiere justificación no vacía.

Once pruebas unitarias aprobadas: siete de reglas y cuatro de seguridad. No se ejecutó integración con servidores activos ni MySQL. Faltan control concurrente de edición/validación, unicidad transaccional por partida, actualización automática e idempotente de ranking y prueba del flujo completo. No se certifica la entrega final con estas pruebas.
