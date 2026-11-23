package com.esports.resultservice;
import com.esports.resultservice.client.MatchClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchClientTest {
    RestTemplate http = mock(RestTemplate.class);
    MatchClient client = new MatchClient(http);
    @Test void respuestaCoherenteConservaPartida() {
        var partida = new MatchClient.Partida(1L,2L,3L,4L,"PROGRAMADA");
        when(http.getForObject(anyString(), eq(MatchClient.Partida.class))).thenReturn(partida);
        assertSame(partida, client.obtener(1L));
    }
    @Test void respuestaIncompletaODistintaEs503() {
        for (var partida : new MatchClient.Partida[]{null, new MatchClient.Partida(8L,2L,3L,4L,"PROGRAMADA"),
                new MatchClient.Partida(1L,null,3L,4L,"PROGRAMADA"), new MatchClient.Partida(1L,2L,3L,4L,null)}) {
            when(http.getForObject(anyString(), eq(MatchClient.Partida.class))).thenReturn(partida);
            assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.obtener(1L)).getStatusCode().value());
        }
    }
    @Test void fuenteCaidaEs503() {
        when(http.getForObject(anyString(), eq(MatchClient.Partida.class))).thenThrow(new ResourceAccessException("offline"));
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.obtener(1L)).getStatusCode().value());
    }
    @Test void partidaInexistenteEs404() {
        when(http.getForObject(anyString(), eq(MatchClient.Partida.class))).thenThrow(
            HttpClientErrorException.create(HttpStatus.NOT_FOUND,"missing",HttpHeaders.EMPTY,new byte[0],null));
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> client.obtener(1L)).getStatusCode().value());
    }
}
