package com.esports.match_service;

import com.esports.match_service.client.InscripcionClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class InscripcionClientTest {
    RestTemplate http = mock(RestTemplate.class);
    InscripcionClient client = new InscripcionClient(http);
    void respuesta(String estado) {
        var dto = new InscripcionClient.InscripcionDTO(); dto.setEquipoId(12L); dto.setEstado(estado);
        when(http.getForObject(anyString(), eq(InscripcionClient.InscripcionDTO[].class))).thenReturn(new InscripcionClient.InscripcionDTO[]{dto});
    }
    @Test void pendienteNoPuedeJugar() { respuesta("PENDIENTE"); assertFalse(client.participanteInscrito(1L, 12L)); }
    @Test void canceladaNoPuedeJugar() { respuesta("CANCELADA"); assertFalse(client.participanteInscrito(1L, 12L)); }
    @Test void aprobadaPuedeJugar() { respuesta("APROBADA"); assertTrue(client.participanteInscrito(1L, 12L)); assertFalse(client.participanteInscrito(1L, 13L)); }
    @Test void falloDeRedEs503() {
        when(http.getForObject(anyString(), eq(InscripcionClient.InscripcionDTO[].class))).thenThrow(new ResourceAccessException("offline"));
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.participanteInscrito(1L, 12L)).getStatusCode().value());
    }
    @Test void respuestaNulaEs503() { assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.participanteInscrito(1L, 12L)).getStatusCode().value()); }
}
