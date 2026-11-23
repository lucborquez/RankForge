# Premios de torneos

Implementación del 20 de noviembre de 2026, rama `feature/lucas-ep3`.

## Contrato

- `GET /api/torneos/{torneoId}/premios`: consulta pública, ordenada por posición. Devuelve una lista vacía antes de FINALIZADO y 404 si el torneo no existe.
- `POST /api/torneos/{torneoId}/premios`: exige JWT válido con rol ADMIN y devuelve 201. Cuerpo: `{"posicion":1,"participanteId":25,"descripcion":"Trofeo"}`.
- Posición y participante deben ser positivos; descripción obligatoria de hasta 500 caracteres. El torneo debe estar FINALIZADO y el participante debe ocupar esa posición en ranking-service. Un puesto admite un premio descriptivo. No representa una transferencia de dinero.
- Un conflicto devuelve 409. Si no se puede consultar ranking-service, devuelve 503 y no asigna el premio.

## Persistencia y autorización

Entidad `Premio` en tournament-service, restricción única por torneo y posición. La asignación bloquea la fila del torneo durante la transacción para serializar asignaciones simultáneas. Esta propiedad aún necesita prueba de concurrencia con MySQL real.

`TorneoAuthorizationFilter` verifica firma HS256, expiración, identidad y rol de cada escritura directa bajo `/api/torneos`. Las cabeceras de rol del cliente no conceden permisos. Se requiere `JWT_SECRET` común con autenticación. `ranking.service.url` admite configurar el destino, por defecto localhost:8089.

## Evidencia y límites

Revisión del 22 de noviembre: la edición de la configuración del torneo se rechaza con 409 desde EN_CURSO y también en FINALIZADO o CANCELADO. Así no se altera el juego, las fechas o la modalidad de un torneo que ya terminó mediante el endpoint de edición. La suite seleccionada de torneos suma 19 pruebas aprobadas, incluidas tres restricciones de estado y una edición válida de borrador. Esto no resuelve por sí solo las correcciones de resultados posteriores al cierre entre servicios.

Diez pruebas unitarias de premios y seguridad aprobadas el 20 de noviembre. Cubren ocultamiento antes del cierre, rechazo de asignación anticipada, posición incorrecta, duplicado, asignación válida y acceso por rol/token. Repositorios y cliente de clasificación están simulados.

Frontend ofrece consulta en el detalle finalizado y asignación en Organización sólo para administradores. El servidor vuelve a comprobar permisos y clasificación.

Pendiente: recorrido integrado con MySQL y servicios activos, prueba concurrente y política coordinada ante correcciones de resultados después de asignar premios. La verificación de clasificación es una consulta entre servicios, no una transacción distribuida. No afirmar que el proyecto completo está listo basándose en estas pruebas.

El 23 de noviembre se modificó el cierre. Al pasar de EN_CURSO a FINALIZADO, el servicio de torneos solicita una reconstrucción con el JWT del administrador. Cada partida vigente debe tener exactamente un resultado validado. Una clasificación vacía o una partida pendiente produce 409. Si la consulta falla, el torneo sigue EN_CURSO. El cierre guarda los puestos y el aviso en la misma transacción. Los premios de torneos cerrados mediante este flujo utilizan los puestos guardados. Los torneos finalizados en versiones anteriores conservan la consulta de clasificación que ya utilizaban.

Se ejecutaron 18 pruebas de ranking y 21 de torneos y se generaron ambos JAR. Se comprobaron el rechazo de partidas pendientes o sin resultado, la clasificación de una victoria, la conservación del estado tras un cierre rechazado y la comprobación del premio contra el puesto guardado. Falta probar con los servicios activos una modificación de resultado iniciada simultáneamente con el cierre. La comprobación HTTP entre servicios y sus transacciones locales dejan abierta esa ventana.
