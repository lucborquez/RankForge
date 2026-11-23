# Identidad y equipos — avance EP3

## Contratos implementados

- `POST /api/usuarios/me`: asegura un perfil único por el subject del JWT firmado. No recibe identidad ni rol del formulario. Reutiliza perfiles existentes y rechaza los desactivados.
- `GET /api/usuarios/me`: obtiene el perfil y su ID de jugador; requiere el mismo JWT. El rol de la respuesta procede del token validado.
- `PUT /api/usuarios/me`: actualiza exclusivamente nombre y apodo. El email, estado y rol no son editables en este contrato. Apodo ocupado: HTTP 409.
- El microservicio de usuarios verifica firma HS256, caducidad, subject y rol incluso si se accede sin Gateway. Configurar `JWT_SECRET` con el mismo secreto Base64 usado por autenticación.
- Las operaciones generales de usuarios quedan reservadas a ADMIN. La consulta booleana `GET /api/usuarios/{id}/activo` sigue disponible para los servicios internos.
- Crear equipos obtiene el capitán del perfil autenticado; ignora un capitán suplantado en el body. Editar, agregar integrantes y desactivar exigen capitán o administrador.
- `DELETE /api/equipos/{equipoId}/miembros/{miembroId}` elimina por ID del registro de miembro. Verifica pertenencia, equipo activo y permisos, y no permite quitar al capitán.
- La edición no transfiere la capitanía. Los nuevos integrantes solo pueden incorporarse como JUGADOR.

## Verificación realizada

`PerfilSecurityTest` y `PerfilServiceTest`: 8 pruebas aprobadas. `TeamMembersTest` y `TeamAuthorizationTest`: 9 pruebas aprobadas. Ejecución con Maven y JDK 26, sin iniciar servidores ni requerir MySQL para estas pruebas unitarias.

## Límites actuales

Pendiente prueba integrada con JWT emitido por auth-service y persistencia MySQL. Las pruebas usan repositorios y clientes simulados; no constituyen evidencia E2E. Las demás operaciones del proyecto requieren revisión propia de permisos, reglas y concurrencia. Este documento no certifica que EP3 esté listo para entrega.
