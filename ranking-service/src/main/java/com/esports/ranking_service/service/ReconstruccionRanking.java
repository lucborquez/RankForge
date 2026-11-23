package com.esports.ranking_service.service;

import com.esports.ranking_service.client.ResultadosClient;
import com.esports.ranking_service.model.Ranking;
import com.esports.ranking_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.util.List;

@Service
public class ReconstruccionRanking {
    private final ResultadosClient fuente;
    private final RankingRepository rankings;
    private final RankingTorneoLockRepository bloqueos;
    public ReconstruccionRanking(ResultadosClient fuente, RankingRepository rankings, RankingTorneoLockRepository bloqueos) {
        this.fuente = fuente; this.rankings = rankings; this.bloqueos = bloqueos;
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Ranking> recalcular(Long torneoId) {
        return recalcular(torneoId, false);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Ranking> recalcular(Long torneoId, boolean cierre) {
        if (torneoId == null || torneoId <= 0) throw new IllegalArgumentException("Torneo inválido");
        bloqueos.bloquear(torneoId);
        var marcadores = cierre ? fuente.obtener(torneoId, true) : fuente.obtener(torneoId);
        var tabla = CalculadoraRanking.calcular(torneoId, marcadores);
        if (cierre && tabla.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "El torneo carece de resultados validados");
        }
        var filas = tabla.stream().map(r -> new Ranking(null, torneoId, r.getParticipanteId(), r.getPuntos(),
                r.getVictorias(), r.getDerrotas(), r.getDiferencia(), r.getPosicion())).toList();
        // Consultas fallidas no borran la clasificación anterior; un fallo SQL revierte toda la transacción.
        rankings.borrarPorTorneo(torneoId);
        return rankings.saveAll(filas);
    }
}
