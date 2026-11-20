package com.esports.ranking_service.service;

import com.esports.ranking_service.dto.RankingResponseDTO;
import java.util.*;

/** Reconstruye desde resultados validados: nunca suma sobre un ranking anterior. */
public final class CalculadoraRanking {
    private CalculadoraRanking() {}
    public record Marcador(Long resultadoId, Long partidaId, Long participanteAId, Long participanteBId,
                           Integer puntajeA, Integer puntajeB, String estadoValidacion) {}
    public static List<RankingResponseDTO> calcular(Long torneoId, List<Marcador> marcadores) {
        Map<Long, Marcador> partidas = new HashMap<>();
        Map<Long, RankingResponseDTO> tabla = new HashMap<>();
        for (Marcador m : marcadores) {
            if (!"VALIDADO".equals(m.estadoValidacion())) continue;
            if (m.resultadoId() == null || m.partidaId() == null || m.participanteAId() == null || m.participanteBId() == null
                    || m.participanteAId().equals(m.participanteBId()) || m.puntajeA() == null || m.puntajeB() == null
                    || m.puntajeA() < 0 || m.puntajeB() < 0) throw new IllegalArgumentException("Resultado validado inconsistente");
            Marcador previo = partidas.putIfAbsent(m.partidaId(), m);
            if (previo != null) {
                if (!previo.equals(m)) throw new IllegalArgumentException("Múltiples resultados validados para una partida");
                continue;
            }
            var a = tabla.computeIfAbsent(m.participanteAId(), id -> nuevo(torneoId, id));
            var b = tabla.computeIfAbsent(m.participanteBId(), id -> nuevo(torneoId, id));
            int diferencia = m.puntajeA() - m.puntajeB();
            a.setDiferencia(Math.addExact(a.getDiferencia(), diferencia));
            b.setDiferencia(Math.subtractExact(b.getDiferencia(), diferencia));
            if (diferencia == 0) {
                a.setPuntos(Math.addExact(a.getPuntos(), 1)); b.setPuntos(Math.addExact(b.getPuntos(), 1));
            } else {
                var ganador = diferencia > 0 ? a : b; var perdedor = diferencia > 0 ? b : a;
                ganador.setPuntos(Math.addExact(ganador.getPuntos(), 3));
                ganador.setVictorias(Math.addExact(ganador.getVictorias(), 1));
                perdedor.setDerrotas(Math.addExact(perdedor.getDerrotas(), 1));
            }
        }
        var orden = new ArrayList<>(tabla.values());
        orden.sort(Comparator.comparing(RankingResponseDTO::getPuntos).reversed()
                .thenComparing(Comparator.comparing(RankingResponseDTO::getDiferencia).reversed())
                .thenComparing(Comparator.comparing(RankingResponseDTO::getVictorias).reversed())
                .thenComparing(RankingResponseDTO::getParticipanteId));
        for (int i = 0; i < orden.size(); i++) orden.get(i).setPosicion(i + 1);
        return orden;
    }
    private static RankingResponseDTO nuevo(Long torneoId, Long participanteId) {
        var r = new RankingResponseDTO(); r.setTorneoId(torneoId); r.setParticipanteId(participanteId);
        r.setPuntos(0); r.setVictorias(0); r.setDerrotas(0); r.setDiferencia(0); return r;
    }
}
