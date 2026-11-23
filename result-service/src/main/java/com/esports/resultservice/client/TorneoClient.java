package com.esports.resultservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TorneoClient {


    @Value("${tournament.service.url}")
    private String tournamentServiceUrl;

    private final RestTemplate restTemplate;

    public TorneoClient() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public TorneoClient(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    public record TorneoEstado(Long id, String estado) {}

    public void exigirModificable(Long torneoId) {
        TorneoEstado torneo;
        try {
            torneo = restTemplate.getForObject(tournamentServiceUrl + "/api/torneos/" + torneoId, TorneoEstado.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Torneo no encontrado");
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "No se pudo comprobar el estado del torneo");
        }
        if (torneo == null || !java.util.Objects.equals(torneoId, torneo.id()) || torneo.estado() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Estado del torneo no disponible");
        }
        if (!java.util.Set.of("ABIERTO", "CERRADO", "EN_CURSO").contains(torneo.estado())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "El estado del torneo no permite modificar resultados");
        }
    }

}

