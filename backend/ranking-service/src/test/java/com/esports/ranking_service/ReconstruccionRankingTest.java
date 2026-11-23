package com.esports.ranking_service;
import com.esports.ranking_service.client.ResultadosClient;
import com.esports.ranking_service.repository.*;
import com.esports.ranking_service.service.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class ReconstruccionRankingTest {
    ResultadosClient fuente = mock(ResultadosClient.class);
    RankingRepository rankings = mock(RankingRepository.class);
    RankingTorneoLockRepository bloqueos = mock(RankingTorneoLockRepository.class);
    ReconstruccionRanking service = new ReconstruccionRanking(fuente, rankings, bloqueos);
    @Test void falloDeFuenteNoBorraTablaAnterior() {
        when(fuente.obtener(1L)).thenThrow(new IllegalStateException("Sin conexión"));
        assertThrows(IllegalStateException.class, () -> service.recalcular(1L));
        verifyNoInteractions(rankings);
    }
    @Test void anularTodosLosResultadosReconstruyeTablaVacia() {
        when(fuente.obtener(1L)).thenReturn(List.of(new CalculadoraRanking.Marcador(1L, 1L, 7L, 8L, 2, 1, "ANULADO")));
        service.recalcular(1L);
        verify(rankings).borrarPorTorneo(1L); verify(rankings).saveAll(List.of());
    }
    @Test void reintentosSustituyenEnLugarDeSumarSobreAnterior() {
        when(fuente.obtener(1L)).thenReturn(List.of(new CalculadoraRanking.Marcador(1L, 1L, 7L, 8L, 2, 1, "VALIDADO")));
        service.recalcular(1L); service.recalcular(1L);
        verify(rankings, times(2)).borrarPorTorneo(1L);
        verify(rankings, times(2)).saveAll(argThat(filas -> {
            var lista = (List<com.esports.ranking_service.model.Ranking>) filas;
            return lista.size() == 2 && lista.getFirst().getPuntos() == 3;
        }));
    }
}
