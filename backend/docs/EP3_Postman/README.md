# Colección Postman de aceptación manual

Importar RankForge_EP3.postman_collection.json en Postman mediante Import. Contiene 16 solicitudes preparadas y aserciones de código HTTP; ninguna se ha ejecutado contra la aplicación. Complementa los veinte casos del protocolo EP3_ACEPTACION_INTEGRADA, no los sustituye.

1. Iniciar los servicios según EP3_ARRANQUE_LOCAL. Usar un torneo y cuentas de prueba propios.
2. Iniciar sesión con cada cuenta mediante POST /api/auth/login y JSON email/password. Configurar localmente tokenJugador, tokenOrganizador y tokenAdmin con los JWT correspondientes, sin el prefijo Bearer. No publicar tokens ni exportar valores privados.
3. Completar torneoId, resultadoId y ganadorId con IDs reales. El resultado debe pertenecer al torneo, tener marcador coherente y estar PENDIENTE. El ganador debe ocupar la primera posición calculada. Los prerrequisitos incluyen participantes aprobados y partida válida.
4. Ejecutar cada solicitud manualmente, leer su descripción y conservar estado/cuerpo de la respuesta. Las solicitudes de validación, reconstrucción, finalización y premio modifican datos. No ejecutar toda la colección con Runner sobre datos de presentación.
5. Antes de finalizar, resolver resultados pendientes y comparar la clasificación. Tras repetir validación, comprobar que puntos y filas no se duplican. Los tests incluidos sólo comprueban el código HTTP: no prueban por sí solos idempotencia, contenido, persistencia ni concurrencia.
6. Registrar fecha, commits de ambos repositorios, rol, solicitud, respuesta y resultado del caso. Capturas sin cabeceras Authorization, contraseñas ni información personal. Un test fallido debe investigarse; no cambiar el código esperado para ocultarlo.

La finalización requiere un estado previo que permita la transición. Asignar premio exige torneo FINALIZADO y posición todavía libre. La prueba negativa de anulación requiere resultado VALIDADO: uno ya ANULADO devuelve su estado por idempotencia. Un 503 al validar puede indicar que el resultado quedó guardado pero falló la reconstrucción; seguir el mensaje y comprobar ambos servicios.

Verificación del archivo: JSON generado correctamente, 16 nombres únicos y scripts JavaScript sintácticamente válidos. Importación y ejecución en Postman todavía pendientes. No contiene credenciales ni evidencia de éxito inventada.
