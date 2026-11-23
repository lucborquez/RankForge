package com.esports.match_service.client;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
public class InscripcionClient {

    private static final Logger log = LoggerFactory.getLogger(InscripcionClient.class);

    @Value("${registration.service.url}")
    private String registrationServiceUrl;

    private final RestTemplate restTemplate;

    public InscripcionClient() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public InscripcionClient(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    // Verifica si un participante está inscrito en el torneo
    public boolean participanteInscrito(Long torneoId, Long participanteId) {
        try {
            String url = registrationServiceUrl
                    + "/api/inscripciones/torneo/" + torneoId;
            InscripcionDTO[] inscripciones = restTemplate
                    .getForObject(url, InscripcionDTO[].class);
            if (inscripciones == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Inscripciones no disponibles");

            return Arrays.stream(inscripciones).anyMatch(i ->
                    i != null && ("APROBADA".equals(i.getEstado()) || "ACEPTADA".equals(i.getEstado())) &&
                    (participanteId.equals(i.getJugadorId()) || participanteId.equals(i.getEquipoId())));
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Error consultando registration-service: {}", e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "No se pudieron comprobar las inscripciones", e);
        }
    }

    // DTO interno
    @Data
    public static class InscripcionDTO {
        private Long jugadorId;
        private Long equipoId;
        private String estado;

    }
}
