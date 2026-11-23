# Existencia del destinatario de una sanción

Antes de crear una sanción, el controlador comprueba que el usuario o equipo existe. La consulta usa el ID del perfil de jugador, no el ID de la cuenta de autenticación. Un destinatario ausente produce 404; una fuente remota caída o sin respuesta fiable produce 503. En ambos casos no se invoca la creación ni se guarda una sanción. La identidad debe ser exactamente usuario o equipo con ID positivo.

Para usuarios se incorpora GET /api/usuarios/{id}/existe: devuelve un booleano sin datos del perfil y exige JWT ADMIN u ORGANIZADOR. JUGADOR recibe 403 y sin sesión 401. El cliente reenvía el token del operador; no usa cabeceras de rol ni claves internas. Para equipos se consulta GET /api/equipos/{id}. Existencia no equivale a actividad: un participante inactivo sigue existiendo y puede ser sancionado. Actualizar conserva el destinatario original y no exige que continúe activo.

El cliente limita conexión a 3 segundos y lectura a 5 segundos. user.service.url y team.service.url permiten configurar las URLs, por defecto localhost:8082 y localhost:8084. La baja posterior del participante no está coordinada atómicamente con la creación en otro servicio; las bajas actuales son lógicas y conservan el ID.

Verificación del 22 de noviembre: user-service 10 pruebas y sanction-service 22 pruebas satisfactorias. Se comprueban roles, token reenviado, existencia falsa, equipo inactivo existente, 404 remoto, 503 remoto y ausencia de creación cuando falla la comprobación. El cliente HTTP usa servidor simulado; no demuestra una ejecución real con MySQL. La planilla API anterior refleja 78 rutas sobre 4770687; esta ruta nueva se debe incorporar en su siguiente revisión.
