# eSports Arena Manager

## Estado de la integración EP3

La rama `feature/lucas-ep3` incorpora identidad autenticada, permisos de equipos, reglas de inscripción, resultados con reconstrucción de ranking, premios y avisos públicos persistidos del torneo. Las pruebas unitarias seleccionadas pasan; el recorrido completo con MySQL y todos los servicios activos sigue pendiente. Esta rama todavía no constituye una entrega final verificada.

Consulta [el arranque local](docs/EP3_ARRANQUE_LOCAL.md), [las reglas de partidas](docs/EP3_PARTIDAS.md), [premios](docs/EP3_PREMIOS.md), [notificaciones](docs/EP3_NOTIFICACIONES.md) y [seguridad](docs/EP3_SEGURIDAD_OPERACIONES.md). Los documentos distinguen implementación, pruebas ejecutadas y limitaciones.

El [informe de integración](docs/EP3_INFORME_INTEGRACION.md) reúne los cambios, la evidencia disponible y lo que falta para aceptar la entrega.

El [catálogo de APIs](docs/EP3_APIs/README.md) incluye la planilla XLSX de 78 rutas con contratos, permisos y respuestas esperadas; la ejecución HTTP está pendiente.

La [colección Postman](docs/EP3_Postman/README.md) prepara 16 solicitudes de aceptación por rol. Deben ejecutarse manualmente con datos de prueba; aún no representan evidencia de integración.

eSports Arena Manager es una plataforma backend orientada a la administración de torneos competitivos de videojuegos.
El sistema fue desarrollado bajo una arquitectura de microservicios utilizando Spring Boot, permitiendo separar responsabilidades y mantener una solución modular, escalable y mantenible.

El objetivo principal del proyecto es gestionar torneos de eSports mediante servicios independientes encargados de usuarios, equipos, partidas, resultados, rankings, sanciones y procesos relacionados al flujo competitivo.

La solución fue diseñada aplicando buenas prácticas de desarrollo backend, persistencia relacional, validaciones, manejo de excepciones y comunicación REST entre microservicios.

## Integrantes
Lucas Borquez
Jhon Olivares

## Arquitectura General del Sistem
La solución fue diseñada bajo una arquitectura de microservicios independientes, donde cada servicio posee:

1.Responsabilidad específica
 2.Base de datos independiente
 3.CRUD propio
 4.Endpoints REST
 5.Reglas de negocio
 6.Validaciones
 7.Manejo de excepciones
 8.Comunicación REST con otros servicios


## Microservicios
| Servicio | Puerto | Base de datos | Responsabilidad |
| --- | --- | --- | --- |
| eureka-server | 8761 | No aplica | Registro y descubrimiento |
| api-gateway | 8080 | No aplica | Entrada HTTP y autorización |
| game-service | 8081 | db_game_service | Catálogo de juegos |
| user-service | 8082 | db_user_service | Perfiles e identidad |
| auth-service | 8083 | db_auth_service | Cuentas y JWT |
| team-service | 8084 | db_team_service | Equipos e integrantes |
| tournament-service | 8085 | db_tournament_service | Torneos, premios y avisos |
| registration-service | 8086 | db_registration_service | Inscripciones y cupos |
| match-service | 8087 | db_match_service | Partidas y calendario |
| result-service | 8088 | db_esports_resultados | Resultados y validación |
| ranking-service | 8089 | db_ranking_service | Clasificación y estadísticas |
| sanction-service | 8090 | db_sanction_service | Sanciones y vigencia |

## Tecnologias usadas
## Backend
1. JDK 26
2. Spring boot
3. Spring Data JPA
4. Hibernate
5. Maven
6. Bean Validation
7. REST API

## BASE DE DATOS
Para la persistencia se utilizó:
1. MySQL
2. Laragon
3. HeidiSQL
Laragon fue utilizado como entorno local de desarrollo debido a su facilidad de configuración, integración rápida con MySQL y administración eficiente de servicios locales.
HeidiSQL fue utilizado para administrar las bases de datos, permitiendo visualizar tablas, ejecutar consultas SQL y verificar relaciones de manera más profesional y práctica durante el desarrollo.

Para el entorno local, inicia MySQL antes de ejecutar los servicios. Cada servicio usa su propia base y MySQL la crea al conectar si el usuario tiene permiso. Si trabajas con un usuario sin ese permiso, crea las bases indicadas en los archivos `application.properties` antes del arranque.

Usa `.env.example` como referencia. Si tu instalación requiere otro host, puerto, usuario o contraseña, define `DB_HOST`, `DB_PORT`, `DB_USERNAME` y `DB_PASSWORD` en el entorno desde el que ejecutas Java. En PowerShell, por ejemplo, `$env:DB_USERNAME = 'mi_usuario'`. Inicia Eureka en el puerto 8761, luego los microservicios y finalmente el Gateway en el puerto 8080.

## Administración local y Swagger

Para probar la administración desde React, inicia `game-service`, `tournament-service` y `api-gateway` con `--spring.profiles.active=local-admin`. Los tres escucharán en `127.0.0.1`. El Gateway acepta peticiones de escritura del frontend local en el puerto 5173. Arranca los servicios en el mismo equipo junto a MySQL y Eureka.

Los endpoints administrativos de juegos y torneos están disponibles con ese perfil de desarrollo y exigen JWT válido con rol ADMIN, tanto en el Gateway como al acceder directamente a los servicios. Todos deben compartir `JWT_SECRET`. Esta configuración local no acredita un despliegue público.

| Servicio | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| Juegos | [Swagger en 8081](http://127.0.0.1:8081/swagger-ui/index.html) | [JSON en 8081](http://127.0.0.1:8081/v3/api-docs) |
| Torneos | [Swagger en 8085](http://127.0.0.1:8085/swagger-ui/index.html) | [JSON en 8085](http://127.0.0.1:8085/v3/api-docs) |

Las rutas actuales `/api/juegos` y `/api/torneos` se mantienen para el frontend. Un futuro `/api/v1` podrá convivir con ellas durante la migración de los clientes.

## Flujo principal
1. Crear videojuego        -> game-service
2. Crear usuarios          -> user-service
3. Crear equipo            -> team-service  (requiere usuario activo y juego)
4. Crear torneo            -> tournament-service  (requiere juego)
5. Abrir torneo            -> tournament-service  PATCH /api/torneos/{id}/estado con JSON {"estado":"ABIERTO"}
6. Inscribir equipo        -> registration-service  (valida sanciones y cupos)
7. Crear partida           -> match-service  (valida inscripciones)
8. Registrar resultado     -> result-service
9. Consultar ranking       -> ranking-service
10. Registrar sanción      ->sanction-service
(bloquea futuras inscripciones si la sanción está activa)



## Capa de Modelo y Anotaciones
Se utiliza el ecosistema de Spring Data JPA y Lombok para maximizar la productividad y garantizar la integridad de los datos:

Lombok: @Data, @AllArgsConstructor, @NoArgsConstructor, @Builder y @Getter/@Setter para reducir el código redundante.

JPA/Hibernate: @Entity, @Table, @Id, @GeneratedValue(strategy = GenerationType.IDENTITY) y @Column para el mapeo relacional preciso.

Jakarta Validation: @NotBlank, @NotNull, @Positive y @Size en los DTOs para la validación de entrada de datos.
## Patrón Arquitectónico CSR
Todos los microservicios fueron estructurados utilizando el patrón:

Controller → manejo de endpoints REST
Service → lógica de negocio
Repository → acceso a datos
Model → entidades JPA
DTO → validaciones y transferencia de datos

## Conclusión
El proyecto eSports Arena Manager permitió aplicar conocimientos relacionados con arquitectura distribuida basada en microservicios, persistencia relacional,
desarrollo REST, validaciones, manejo de excepciones y comunicación entre servicios.
