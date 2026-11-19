package com.esports.registration_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);

    // Lee la URL de user-service del application.properties
    @Value("${user.service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public UsuarioClient() {
        this.restTemplate = ClientesHttp.crear();
    }

    public record Actor(Long id, String rol, Boolean estado) {
        public boolean administrador() { return "ADMIN".equals(rol); }
    }

    /** La identidad procede de user-service, que verifica la firma JWT. */
    public Actor autenticar(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Inicia sesión");
        }
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", authorization);
        try {
            Actor actor = restTemplate.exchange(userServiceUrl + "/api/usuarios/me",
                    org.springframework.http.HttpMethod.GET, new org.springframework.http.HttpEntity<>(headers), Actor.class).getBody();
            if (actor == null || actor.id() == null || !Boolean.TRUE.equals(actor.estado())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "Perfil no disponible");
            }
            return actor;
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            throw new org.springframework.web.server.ResponseStatusException(ex.getStatusCode(), "Perfil no autorizado");
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "No se pudo validar la identidad");
        }
    }

    // Consulta a user-service si el usuario existe y está activo
    public boolean usuarioExisteYEstaActivo(Long usuarioId) {
        try {
            String url = userServiceUrl + "/api/usuarios/" + usuarioId + "/activo";
            Boolean activo = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(activo);
        } catch (Exception e) {
            log.error("Error consultando user-service para usuarioId {}: {}", usuarioId, e.getMessage());
            return false;
        }
    }
}

