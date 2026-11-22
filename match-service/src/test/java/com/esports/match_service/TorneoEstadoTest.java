package com.esports.match_service;
import com.esports.match_service.client.TorneoClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TorneoEstadoTest {
    RestTemplate http = mock(RestTemplate.class);
    TorneoClient client = new TorneoClient(http);
    void respuesta(String estado) { when(http.getForObject(anyString(), eq(TorneoClient.TorneoEstado.class))).thenReturn(new TorneoClient.TorneoEstado(1L, estado)); }
    @Test void finalizadoNoPermiteCambiarPartidas() { respuesta("FINALIZADO"); assertEquals(409, assertThrows(ResponseStatusException.class, () -> client.exigirModificable(1L)).getStatusCode().value()); }
    @Test void canceladoNoPermiteCambiarPartidas() { respuesta("CANCELADO"); assertThrows(ResponseStatusException.class, () -> client.exigirModificable(1L)); }
    @Test void borradorNoPermiteCambiarPartidas() { respuesta("BORRADOR"); assertThrows(ResponseStatusException.class, () -> client.exigirModificable(1L)); }
    @Test void estadosOperativosPermitenProgramacion() { for (String estado : new String[]{"ABIERTO", "CERRADO", "EN_CURSO"}) { respuesta(estado); assertDoesNotThrow(() -> client.exigirModificable(1L)); } }
    @Test void respuestaNulaImpideEscritura() { assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.exigirModificable(1L)).getStatusCode().value()); }
    @Test void caidaDelServicioEs503() {
        when(http.getForObject(anyString(), eq(TorneoClient.TorneoEstado.class))).thenThrow(new ResourceAccessException("offline"));
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.exigirModificable(1L)).getStatusCode().value());
    }
}
