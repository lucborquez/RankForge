package com.esports.ranking_service;

import com.esports.ranking_service.client.ResultadosClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.junit.jupiter.api.Assertions.*;

class CierreResultadosTest {
    ResultadosClient cliente = new ResultadosClient("http://partidas","http://resultados");
    MockRestServiceServer servidor = MockRestServiceServer.bindTo((RestTemplate) ReflectionTestUtils.getField(cliente, "http")).build();

    void preparar(String resultados) {
        servidor.expect(requestTo("http://partidas/api/partidas/torneo/3"))
                .andRespond(withSuccess("[{\"id\":4,\"torneoId\":3,\"participanteAId\":1,\"participanteBId\":2,\"estado\":\"PROGRAMADA\"}]", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("http://resultados/api/resultados/partida/4"))
                .andRespond(withSuccess(resultados, MediaType.APPLICATION_JSON));
    }
    @Test void partidaSinResultadoImpideCierre() {
        preparar("[]");
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> cliente.obtener(3L,true)).getStatusCode().value());
        servidor.verify();
    }
    @Test void resultadoPendienteImpideCierre() {
        preparar("[{\"id\":9,\"partidaId\":4,\"estadoValidacion\":\"PENDIENTE\"}]");
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> cliente.obtener(3L,true)).getStatusCode().value());
    }
    @Test void resultadoValidadoParticipaEnClasificacionFinal() {
        preparar("[{\"id\":9,\"partidaId\":4,\"puntajeA\":2,\"puntajeB\":1,\"estadoValidacion\":\"VALIDADO\"}]");
        var filas = com.esports.ranking_service.service.CalculadoraRanking.calcular(3L,cliente.obtener(3L,true));
        assertEquals(1L, filas.get(0).getParticipanteId());
        assertEquals(3, filas.get(0).getPuntos());
    }
}
