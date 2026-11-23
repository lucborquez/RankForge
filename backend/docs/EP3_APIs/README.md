# Catálogo de APIs EP3

La planilla RankForge_APIs_EP3.xlsx contiene 79 combinaciones de método y ruta extraídas de los controladores del backend, revisadas el 22 de noviembre de 2026 sobre el código b585b63. Incluye servicio, operación, datos de entrada, respuesta esperada, acceso, autenticación, roles, observaciones y archivo con línea de origen.

Los códigos HTTP representan respuestas esperadas por lectura del código, no respuestas observadas. La aceptación con servicios activos, ejemplos completos de solicitudes y capturas de Postman/Swagger sigue pendiente según EP3_ACEPTACION_INTEGRADA. Las rutas de escritura manual de ranking se conservan documentadas con 409; las rutas administrativas de juegos y torneos requieren local-admin. El registro público siempre asigna JUGADOR.

Los campos JSON enumerados identifican el contrato; consultar el DTO fuente para tipos, obligatoriedad y restricciones. Los parámetros entre llaves forman parte de la ruta. Los roles descritos requieren JWT válido y las reglas de propiedad del servicio. La planilla no sustituye la validación de negocio ni enumera todos los errores posibles.

Se verificaron 79 claves método/ruta sin duplicados, ausencia de valores de error de hoja y vistas renderizadas de rutas, contratos, observaciones y la entrada JSON más extensa. No contiene fórmulas, credenciales ni datos personales. Se usó un formato propio de revisión porque la plantilla docente original no está disponible.
