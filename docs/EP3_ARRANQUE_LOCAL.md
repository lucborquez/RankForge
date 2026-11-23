# Arranque local de RankForge EP3

Estas instrucciones preparan una ejecución manual para comprobar la integración. No son evidencia de que los servicios ya hayan iniciado. Utiliza las ramas de trabajo EP3 del equipo, no el main histórico de EP2.

## Requisitos y preparación

1. Inicia MySQL desde Laragon y confirma el puerto 3306. Conserva las bases existentes; no es necesario borrarlas.
2. Usa JDK 26 y Maven Wrapper incluido en el repositorio. El frontend requiere Node 20.19 o superior, npm y Chrome o Edge para las pruebas.
3. En cada terminal PowerShell, sitúate en la raíz del backend y configura estas variables con los valores de tu instalación:

```powershell
$env:JAVA_HOME = "C:\ruta\al\jdk-26"
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "TU_CONTRASENA_MYSQL"
$env:JWT_SECRET = "TU_CLAVE_BASE64_COMUN"
$env:SPRING_PROFILES_ACTIVE = "local-admin"
```

Si MySQL realmente no tiene contraseña en tu instalación local, usa una cadena vacía para DB_PASSWORD. No copies los marcadores literales como credenciales. Las variables pertenecen a esa terminal; abrir otra no las copia automáticamente.

Si aún no tienes JWT_SECRET, genera una clave una sola vez en una terminal local:

```powershell
$claveBytes = New-Object byte[] 32
$generador = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$generador.GetBytes($claveBytes)
$generador.Dispose()
[Convert]::ToBase64String($claveBytes)
```

Copia el resultado a JWT_SECRET en todas las terminales del backend. No lo publiques, no lo incluyas en capturas ni en Git. Si cambias la clave, los tokens anteriores dejarán de ser válidos y tendrás que iniciar sesión otra vez. El frontend sólo recibe la URL pública, nunca esta clave.

## Iniciar los servicios

Mantén una terminal por servicio. Primero inicia Eureka:

```powershell
.\team-service\mvnw.cmd -f .\eureka-server\pom.xml spring-boot:run
```

Espera el mensaje `Started` y abre http://localhost:8761. Después, desde otras terminales configuradas, usa el siguiente comando, reemplazando `auth-service` por cada nombre de la tabla:

```powershell
.\team-service\mvnw.cmd -f .\auth-service\pom.xml spring-boot:run
```

El wrapper de team-service puede ejecutar el pom de los demás proyectos, incluido api-gateway, que no tiene wrapper propio.

| Orden | Servicio | Puerto | Base de datos predeterminada |
| --- | --- | --- | --- |
| 1 | auth-service | 8083 | db_auth_service |
| 2 | user-service | 8082 | db_user_service |
| 3 | game-service | 8081 | db_game_service |
| 4 | team-service | 8084 | db_team_service |
| 5 | tournament-service | 8085 | db_tournament_service |
| 6 | sanction-service | 8090 | db_sanction_service |
| 7 | registration-service | 8086 | db_registration_service |
| 8 | match-service | 8087 | db_match_service |
| 9 | result-service | 8088 | db_esports_resultados |
| 10 | ranking-service | 8089 | db_ranking_service |
| 11 | api-gateway | 8080 | No utiliza base propia |

El perfil local-admin habilita la administración de juegos y torneos y restringe servicios de catálogo al equipo local. No sustituye la autenticación: las escrituras administrativas requieren un token ADMIN. Hibernate utiliza `ddl-auto=update`; el usuario de MySQL debe poder crear o actualizar las tablas del proyecto.

## Iniciar el frontend

En la raíz del frontend, crea `.env.local` con esta única configuración pública:

```text
VITE_API_URL=http://127.0.0.1:8080
```

Después ejecuta:

```powershell
npm ci
npm run dev -- --host localhost
```

Abre http://localhost:5173. Usa localhost en el navegador, ya que es el origen local admitido por defecto en el Gateway. Reinicia Vite si cambias el archivo de entorno.

## Comprobar y diagnosticar

- Confirma `Started` en cada terminal y registro en Eureka. Que Eureka responda no prueba que MySQL o las operaciones del negocio funcionen.
- Consulta http://localhost:8080/api/juegos. Debe responder con datos o una lista vacía; una lista vacía no significa fallo de conexión.
- Si aparece `Access denied` de MySQL, revisa usuario y contraseña. Si aparece `Connection refused`, revisa que MySQL esté activo y el puerto sea correcto.
- Si aparece `Could not resolve placeholder JWT_SECRET`, configura la variable en esa terminal antes de reiniciar el servicio.
- Un 401 exige iniciar sesión con un token vigente. Un 403 indica que el rol no permite esa operación. No cambies cabeceras de rol para intentar conceder permisos.
- Un 404/405 en creación de juegos o torneos puede indicar que falta el perfil local-admin. Un 503 del Gateway puede indicar que el servicio aún no se registró o no está disponible.
- El registro público sólo crea jugadores. Para probar administrador y organizador deben existir cuentas con esos roles gestionadas de forma autorizada; no se incluyen contraseñas ni cuentas predefinidas en esta guía.

## Evidencias pendientes

Completar un recorrido por visitante, jugador, organizador y administrador; comprobar persistencia al recargar; guardar requests y respuestas exitosas y negativas en Postman/Swagger; verificar ranking tras validación y reintento; finalizar torneo, asignar premio y consultar el aviso. Registrar fecha, rama y commit de ambas aplicaciones. No sustituir estas evidencias por capturas históricas de EP2.
