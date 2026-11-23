# RankForge

Plataforma de torneos de eSports. El frontend React y los servicios Spring Boot se descargan juntos desde este repositorio.

## Ejecutar el proyecto

1. Clona el repositorio con `git clone https://github.com/lucborquez/RankForge.git`.
2. Inicia MySQL y sigue la [guía del backend](backend/docs/EP3_ARRANQUE_LOCAL.md) desde la carpeta `backend`. Allí están los puertos, el orden de arranque y las variables necesarias para compartir la sesión JWT.
3. En `frontend`, crea `.env.local` con `VITE_API_URL=http://127.0.0.1:8080`. Ejecuta `npm ci` y `npm run dev -- --host localhost`. Abre la dirección que indique Vite.

## Evaluación EP3

- [Manual de usuario](frontend/docs/EP3_Manual/Manual_RankForge_EP3_Revision.docx)
- [Requisitos y arquitectura](frontend/docs/ERS-V3-EP3.md)
- [Informe de integración](backend/docs/EP3_INFORME_INTEGRACION.md)
- [Catálogo de APIs](backend/docs/EP3_APIs/RankForge_APIs_EP3.xlsx)
- [Evidencia del recorrido por los cuatro roles](backend/docs/EP3_Postman/recorrido-cuatro-roles.json)

## Historial

Los dos historiales de Git se unieron conservando sus commits originales. Las revisiones previas se pueden consultar en el [PR del frontend](https://github.com/jzzzn2110/Frontend-eSports-Arena-Manager/pull/5) y el [PR del backend](https://github.com/lucborquez/game/pull/7).
