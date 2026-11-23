# Comprobación del bloqueo MySQL por torneo

Fecha: 23 de noviembre de 2026. Motor MySQL 8.4.3 local, iniciado por el usuario en Laragon. Esta comprobación usa dos conexiones SQL independientes y una base temporal aislada; no inicia los servicios Spring ni utiliza tablas del proyecto.

Se creó una tabla InnoDB con clave primaria torneo_id y se utilizó el mismo upsert de PartidaTorneoLockRepository. La primera conexión abrió una transacción READ COMMITTED, adquirió la fila de bloqueo, insertó un registro de prueba y mantuvo la transacción abierta durante tres segundos. La segunda conexión intentó el mismo bloqueo y consultó los registros después de adquirirlo.

Resultado observado: la segunda conexión esperó 2,938 segundos y luego vio el registro confirmado por la primera. El conteo final fue uno. La base temporal se eliminó después de la comprobación; no se modificaron las bases del proyecto.

Esto verifica en MySQL el mecanismo de exclusión de la fila y la visibilidad de la escritura confirmada bajo READ COMMITTED. No demuestra que Spring aplique correctamente los límites transaccionales, que JPA cree el esquema esperado ni que el controlador impida solicitudes duplicadas. Esas verificaciones requieren ejecutar la aplicación y enviar solicitudes concurrentes reales; siguen pendientes.

La prueba tampoco valida las carreras entre servicios ni el cierre simultáneo de un torneo. No debe sustituir los casos de aceptación integrada.
