# Autorización de operaciones en microservicios

Además del Gateway, match-service, result-service, sanction-service y ranking-service verifican localmente el JWT en las mutaciones de sus recursos. Exigen firma HS256, caducidad futura, subject no vacío y rol ADMIN u ORGANIZADOR. Una cabecera X-Role no concede permisos. Ausencia o invalidez de token devuelve 401; rol JUGADOR devuelve 403. GET, HEAD y OPTIONS mantienen su comportamiento de lectura/preflight.

Cada servicio requiere `JWT_SECRET` con el mismo secreto Base64 que auth-service. No hay un valor de producción predeterminado en el repositorio. Cualquier llamada futura entre servicios que modifique estos recursos también deberá autenticarse; no introducir bypass de autorización para el ranking automático.

Los filtros se prueban sin servidores con JWT realmente firmado: solicitud anónima, jugador con cabecera falsificada, organizador válido y token expirado. Eso no demuestra por sí solo el despliegue, CORS, configuración de secretos, ni el flujo real entre servicios.

Pendientes: revisar permisos locales del catálogo y los demás recursos, propiedad cuando corresponda, concurrencia y pruebas E2E. La autorización por rol no sustituye las reglas de negocio de cada operación ni garantiza que la entrega esté completa.
