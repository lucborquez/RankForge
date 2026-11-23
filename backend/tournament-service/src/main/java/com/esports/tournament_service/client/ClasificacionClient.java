package com.esports.tournament_service.client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
@Component
public class ClasificacionClient {
    private final RestTemplate http;
    private final String url;
    public ClasificacionClient(@Value("${ranking.service.url:http://localhost:8089}") String url) {
        this.url = url; var factory = new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(3000); factory.setReadTimeout(5000); http = new RestTemplate(factory);
    }
    public record Posicion(Long participanteId, Integer posicion) {}
    public java.util.Map<Integer, Long> cerrar(Long torneoId, String authorization) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", authorization);
        Posicion[] tabla;
        try {
            tabla = http.postForObject(url + "/api/rankings/torneo/" + torneoId + "/recalcular?cierre=true",
                    new org.springframework.http.HttpEntity<Void>(headers), Posicion[].class);
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cierre requiere resultados validados para todas las partidas vigentes");
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo confirmar la clasificación final");
        }
        var puestos = new java.util.HashMap<Integer, Long>();
        var participantes = new java.util.HashSet<Long>();
        if (tabla == null || tabla.length == 0) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Clasificación final no disponible");
        for (Posicion fila : tabla) {
            if (fila == null || fila.posicion() == null || fila.posicion() < 1 || fila.posicion() > tabla.length
                    || fila.participanteId() == null || fila.participanteId() <= 0 || !participantes.add(fila.participanteId())
                    || puestos.put(fila.posicion(), fila.participanteId()) != null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Clasificación final inconsistente");
            }
        }
        return puestos;
    }
    public boolean coincide(Long torneoId, Integer posicion, Long participanteId) {
        Posicion[] tabla;
        try { tabla = http.getForObject(url + "/api/rankings/torneo/" + torneoId, Posicion[].class); }
        catch (org.springframework.web.client.RestClientException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo comprobar la clasificación"); }
        if (tabla == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Clasificación no disponible");
        return Arrays.stream(tabla).anyMatch(p -> p != null && posicion.equals(p.posicion()) && participanteId.equals(p.participanteId()));
    }
}
