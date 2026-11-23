package com.esports.resultservice.client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class MatchClient {
    @Value("${match.service.url}")
    private String matchServiceUrl;
    private final RestTemplate restTemplate;
    public MatchClient() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }
    public MatchClient(RestTemplate restTemplate) { this.restTemplate = restTemplate; }
    public record Partida(Long id, Long torneoId, Long participanteAId, Long participanteBId, String estado) {}
    public Partida obtener(Long partidaId) {
        Partida partida;
        try {
            partida = restTemplate.getForObject(matchServiceUrl + "/api/partidas/" + partidaId, Partida.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo verificar la partida");
        }
        if (partida == null || !java.util.Objects.equals(partidaId, partida.id()) || partida.torneoId() == null
                || partida.torneoId() <= 0 || partida.estado() == null || partida.estado().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "La fuente devolvió una partida incompleta o distinta");
        }
        return partida;
    }
}
