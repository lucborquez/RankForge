package com.esports.registration_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SancionClient {

    private static final Logger log = LoggerFactory.getLogger(SancionClient.class);

    @Value("${sanction.service.url}")
    private String sanctionServiceUrl;

    private final RestTemplate restTemplate;

    public SancionClient() {
        this.restTemplate = new RestTemplate();
    }

    public SancionClient(RestTemplate restTemplate, String sanctionServiceUrl) {
        this.restTemplate = restTemplate;
        this.sanctionServiceUrl = sanctionServiceUrl;
    }

    public record Sancion(Boolean estado) {}
    public boolean equipoSancionado(Long equipoId) {
        try {
            Sancion[] sanciones = restTemplate.getForObject(sanctionServiceUrl + "/api/sanciones/equipo/" + equipoId, Sancion[].class);
            if (sanciones == null) throw new IllegalStateException("Respuesta vacía");
            return java.util.Arrays.stream(sanciones).anyMatch(s -> s == null || !Boolean.FALSE.equals(s.estado()));
        } catch (Exception ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "No se pudieron comprobar las sanciones del equipo");
        }
    }

    // Pregunta al sanction-service si el usuario tiene sanción activa
    public boolean tieneSancionActiva(Long usuarioId) {
        try {
            String url = sanctionServiceUrl + "/api/sanciones/usuario/"
                    + usuarioId + "/activa";
            Boolean resultado = restTemplate.getForObject(url, Boolean.class);
            if (resultado == null) {
                throw new IllegalStateException("Respuesta de sanciones vacía");
            }
            return Boolean.TRUE.equals(resultado);
        } catch (Exception e) {
            log.error("Error consultando sanction-service para usuarioId {}: {}",
                    usuarioId, e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo comprobar si el participante está sancionado. Reintenta más tarde.");
        }
    }
}
