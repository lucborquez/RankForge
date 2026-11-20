package com.esports.ranking_service.client;

import com.esports.ranking_service.service.CalculadoraRanking.Marcador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Component
public class ResultadosClient {
    private final RestTemplate http;
    private final String partidasUrl;
    private final String resultadosUrl;
    public ResultadosClient(@Value("${match.service.url:http://localhost:8087}") String partidasUrl,
            @Value("${result.service.url:http://localhost:8088}") String resultadosUrl) {
        this.partidasUrl = partidasUrl; this.resultadosUrl = resultadosUrl;
        var factory = new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        http = new RestTemplate(factory);
    }
    public record Partida(Long id, Long torneoId, Long participanteAId, Long participanteBId, String estado) {}
    public record Resultado(Long id, Long partidaId, Integer puntajeA, Integer puntajeB, String estadoValidacion) {}
    public List<Marcador> obtener(Long torneoId) {
        Partida[] partidas = http.getForObject(partidasUrl + "/api/partidas/torneo/" + torneoId, Partida[].class);
        if (partidas == null) throw new IllegalStateException("No se pudieron consultar las partidas");
        List<Marcador> marcadores = new ArrayList<>();
        for (Partida p : partidas) {
            if (p == null || p.id() == null || !torneoId.equals(p.torneoId())) throw new IllegalStateException("Partida inconsistente");
            if ("CANCELADA".equals(p.estado())) continue;
            Resultado[] resultados = http.getForObject(resultadosUrl + "/api/resultados/partida/" + p.id(), Resultado[].class);
            if (resultados == null) throw new IllegalStateException("No se pudieron consultar los resultados");
            for (Resultado r : resultados) {
                if (r == null || !p.id().equals(r.partidaId())) throw new IllegalStateException("Resultado inconsistente");
                marcadores.add(new Marcador(r.id(), p.id(), p.participanteAId(), p.participanteBId(), r.puntajeA(), r.puntajeB(), r.estadoValidacion()));
            }
        }
        return marcadores;
    }
}
