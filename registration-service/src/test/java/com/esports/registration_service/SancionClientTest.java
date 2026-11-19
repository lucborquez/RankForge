package com.esports.registration_service;

import com.esports.registration_service.client.SancionClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SancionClientTest {
    RestTemplate http = mock(RestTemplate.class);
    SancionClient client = new SancionClient(http, "http://sanciones.test");
    String url = "http://sanciones.test/api/sanciones/usuario/7/activa";
    @Test void consultaVigenciaDeEquipoEnServidor() {
        when(http.getForObject("http://sanciones.test/api/sanciones/equipo/3/activa", Boolean.class)).thenReturn(false, true);
        assertFalse(client.equipoSancionado(3L)); assertTrue(client.equipoSancionado(3L));
    }
    @Test void falloDeEquipoNoHabilitaInscripcion() {
        when(http.getForObject("http://sanciones.test/api/sanciones/equipo/3/activa", Boolean.class)).thenReturn(null);
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.equipoSancionado(3L)).getStatusCode().value());
    }
    @Test void diferenciaParticipanteSancionadoDeHabilitado() {
        when(http.getForObject(url, Boolean.class)).thenReturn(true, false);
        assertTrue(client.tieneSancionActiva(7L));
        assertFalse(client.tieneSancionActiva(7L));
    }
    @Test void caidaDeSancionesNoHabilitaInscripcion() {
        when(http.getForObject(url, Boolean.class)).thenThrow(new ResourceAccessException("Desconectado"));
        assertEquals(503, assertThrows(ResponseStatusException.class,
                () -> client.tieneSancionActiva(7L)).getStatusCode().value());
    }
    @Test void respuestaVaciaNoHabilitaInscripcion() {
        when(http.getForObject(url, Boolean.class)).thenReturn(null);
        assertEquals(503, assertThrows(ResponseStatusException.class,
                () -> client.tieneSancionActiva(7L)).getStatusCode().value());
    }
}
