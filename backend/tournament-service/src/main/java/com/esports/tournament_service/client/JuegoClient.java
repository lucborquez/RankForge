package com.esports.tournament_service.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JuegoClient {

    @Value("${game.service.url}")
    private String gameServiceUrl;

    private final RestTemplate restTemplate;

    public JuegoClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(4000);
        this.restTemplate = new RestTemplate(factory);
    }

    public boolean juegoExiste(Long juegoId) {
        try {
            String url = gameServiceUrl + "/api/juegos/" + juegoId;
            Map<?, ?> juego = restTemplate.getForObject(url, Map.class);
            return juego != null && Boolean.TRUE.equals(juego.get("estado"));
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar el catálogo de juegos", ex);
        }
    }
}
