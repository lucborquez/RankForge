package com.esports.registration_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class EquipoClient {
    @Value("${team.service.url}") private String url;
    private final RestTemplate http = new RestTemplate();
    public record Miembro(Long id, Long usuarioId, String rolDentroEquipo) {}
    public record Equipo(Long id, Long capitanId, Boolean estado, Long juegoPrincipalId, java.util.List<Miembro> miembros) {
        public Equipo(Long id, Long capitanId, Boolean estado) { this(id, capitanId, estado, null, null); }
    }
    public Equipo obtener(Long id) {
        try {
            Equipo equipo = http.getForObject(url + "/api/equipos/" + id, Equipo.class);
            if (equipo == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo verificar el equipo");
            return equipo;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo verificar el equipo");
        }
    }
}
