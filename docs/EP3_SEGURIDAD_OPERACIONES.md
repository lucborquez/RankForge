# Autorización de operaciones en microservicios

Además del Gateway, match-service, result-service, sanction-service y ranking-service verifican localmente el JWT en las mutaciones de sus recursos. Exigen firma HS256, caducidad futura, subject no vacío y rol ADMIN u ORGANIZADOR. Una cabecera X-Role no concede permisos. Ausencia o invalidez de token devuelve 401; rol JUGADOR devuelve 403. GET, HEAD y OPTIONS mantienen su comportamiento de lectura/preflight.

Cada servicio requiere `JWT_SECRET` con el mismo secreto Base64 que auth-service. No hay un valor de producción predeterminado en el repositorio. Cualquier llamada futura entre servicios que modifique estos recursos también deberá autenticarse; no introducir bypass de autorización para el ranking automático.

Los filtros se prueban sin servidores con JWT realmente firmado: solicitud anónima, jugador con cabecera falsificada, organizador válido y token expirado. Eso no demuestra por sí solo el despliegue, CORS, configuración de secretos, ni el flujo real entre servicios.

Sanciones también valida exactamente un destinatario con ID positivo, motivo no vacío, severidad LEVE/MODERADA/GRAVE y fechas completas en orden. La edición no transfiere la sanción a otro participante. Sus 16 pruebas unitarias pasan: 4 de autorización, 7 de vigencia y 5 de estas reglas. La existencia real del participante sigue requiriendo validación de integración y una regla específica del servicio.

Pendientes: revisar permisos locales del catálogo y los demás recursos, propiedad cuando corresponda, concurrencia y pruebas E2E. La autorización por rol no sustituye las reglas de negocio de cada operación ni garantiza que la entrega esté completa.
# Actualización: catálogo y torneos, 20 de noviembre

game-service y tournament-service también verifican JWT de forma local. Las escrituras bajo `/api/juegos` y `/api/torneos` requieren ADMIN; organizadores y jugadores reciben 403, y tokens ausentes o expirados reciben 401. Se ignoran cabeceras de rol no firmadas. Ambos requieren `JWT_SECRET` configurado de forma coherente con autenticación. Las consultas públicas permanecen disponibles.

Se aprobaron cinco pruebas de seguridad de juegos y cinco de torneos. Son pruebas del filtro con solicitudes simuladas; falta la comprobación con todos los servicios activos.

