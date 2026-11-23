package com.esports.ranking_service;

import com.esports.ranking_service.service.CalculadoraRanking;
import com.esports.ranking_service.service.CalculadoraRanking.Marcador;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraRankingTest {
    Marcador victoria = new Marcador(1L, 10L, 7L, 8L, 2, 1, "VALIDADO");
    @Test void reconstruccionRepetidaNoDuplicaPuntos() {
        var primera = CalculadoraRanking.calcular(3L, List.of(victoria));
        var segunda = CalculadoraRanking.calcular(3L, List.of(victoria, victoria));
        assertEquals(primera, segunda); assertEquals(3, segunda.getFirst().getPuntos());
        assertEquals(1, segunda.getLast().getDerrotas());
    }
    @Test void ignoraPendientesYAnulados() {
        var pendiente = new Marcador(2L, 11L, 8L, 9L, 9, 0, "PENDIENTE");
        var anulado = new Marcador(3L, 12L, 8L, 9L, 9, 0, "ANULADO");
        assertEquals(CalculadoraRanking.calcular(3L, List.of(victoria)), CalculadoraRanking.calcular(3L, List.of(victoria, pendiente, anulado)));
    }
    @Test void empateDaUnPuntoYDesempateDeterministaPorId() {
        var lista = CalculadoraRanking.calcular(3L, List.of(new Marcador(2L, 11L, 8L, 7L, 1, 1, "VALIDADO")));
        assertEquals(7L, lista.getFirst().getParticipanteId()); assertEquals(1, lista.getFirst().getPuntos());
        assertEquals(0, lista.getFirst().getVictorias()); assertEquals(2, lista.getLast().getPosicion());
    }
    @Test void rechazaResultadosConflictivosDeUnaPartida() {
        var otro = new Marcador(2L, 10L, 7L, 8L, 0, 1, "VALIDADO");
        assertThrows(IllegalArgumentException.class, () -> CalculadoraRanking.calcular(3L, List.of(victoria, otro)));
    }
}
