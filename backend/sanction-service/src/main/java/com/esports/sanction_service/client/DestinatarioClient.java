package com.esports.sanction_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DestinatarioClient {
    private final RestTemplate http;
    @Value("${user.service.url:http://localhost:8082}") private String usuarios;
    @Value("${team.service.url:http://localhost:8084}") private String equipos;
    public DestinatarioClient() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        http = new RestTemplate(factory);
    }
    public DestinatarioClient(RestTemplate http) { this.http = http; }
    public record Equipo(Long id) {}
    public void exigirExistente(Long usuarioId, Long equipoId, String authorization) {
        if ((usuarioId == null) == (equipoId == null) || (usuarioId != null ? usuarioId : equipoId) <= 0)
            throw new IllegalArgumentException("Indica exactamente un destinatario con ID positivo");
        boolean existe;
        try {
            if (usuarioId != null) {
                var headers = new HttpHeaders(); headers.set("Authorization", authorization);
                Boolean respuesta = http.exchange(usuarios + "/api/usuarios/" + usuarioId + "/existe",
                        HttpMethod.GET, new HttpEntity<Void>(headers), Boolean.class).getBody();
                if (respuesta == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Existencia del usuario no disponible");
                existe = respuesta;
            } else {
                Equipo respuesta = http.getForObject(equipos + "/api/equipos/" + equipoId, Equipo.class);
                if (respuesta == null || !equipoId.equals(respuesta.id()))
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Existencia del equipo no disponible");
                existe = true;
            }
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinatario no encontrado");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo comprobar el destinatario");
        }
        if (!existe) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinatario no encontrado");
    }
}
