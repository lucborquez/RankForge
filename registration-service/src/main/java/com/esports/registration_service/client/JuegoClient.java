package com.esports.registration_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class JuegoClient {
    @Value("${game.service.url:http://localhost:8081}") private String url;
    private final RestTemplate http = new RestTemplate();
    public record Juego(Long id, Integer jugadoresPorEquipo, Boolean estado) {}
    public Juego obtener(Long id) {
        try {
            Juego juego = http.getForObject(url + "/api/juegos/" + id, Juego.class);
            if (juego == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo verificar el juego");
            return juego;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo verificar el juego");
        }
    }
}
