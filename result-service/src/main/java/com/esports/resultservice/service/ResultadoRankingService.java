package com.esports.resultservice.service;

import com.esports.resultservice.client.*;
import com.esports.resultservice.model.Resultado;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** La mutación se confirma antes de que ranking vuelva a consultar los resultados. */
@Service
public class ResultadoRankingService {
    private final ResultadoService resultados;
    private final MatchClient partidas;
    private final RankingClient ranking;
    public ResultadoRankingService(ResultadoService resultados, MatchClient partidas, RankingClient ranking) {
        this.resultados = resultados; this.partidas = partidas; this.ranking = ranking;
    }
    public Resultado validar(Long id, String authorization) {
        // El servicio comprueba el estado dentro del bloqueo; no cargar antes una entidad potencialmente obsoleta.
        Resultado actual = resultados.validarResultado(id);
        sincronizar(actual, authorization);
        return actual;
    }
    public Resultado anular(Long id, String justificacion, String authorization) {
        if (justificacion == null || justificacion.isBlank()) throw new IllegalArgumentException("La anulación requiere una justificación");
        Resultado actual = resultados.anularResultado(id, justificacion);
        sincronizar(actual, authorization);
        return actual;
    }
    private void sincronizar(Resultado resultado, String authorization) {
        try {
            var partida = partidas.obtener(resultado.getPartidaId());
            if (partida.torneoId() == null) throw new IllegalStateException("Torneo no disponible");
            ranking.recalcular(partida.torneoId(), authorization);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Resultado guardado; no se pudo actualizar el ranking. Reintenta la misma operación.");
        }
    }
}
