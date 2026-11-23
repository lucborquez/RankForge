package com.esports.resultservice;
import com.esports.resultservice.client.*;
import com.esports.resultservice.model.Resultado;
import com.esports.resultservice.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ResultadoRankingTest {
    ResultadoService resultados = mock(ResultadoService.class);
    MatchClient partidas = mock(MatchClient.class);
    RankingClient ranking = mock(RankingClient.class);
    ResultadoRankingService service = new ResultadoRankingService(resultados, partidas, ranking);
    Resultado preparar(String estado) {
        Resultado r = new Resultado(); r.setId(4L); r.setPartidaId(3L); r.setEstadoValidacion(estado);
        when(resultados.buscarPorId(4L)).thenReturn(r);
        when(resultados.validarResultado(4L)).thenReturn(r);
        when(resultados.anularResultado(eq(4L), anyString())).thenReturn(r);
        when(partidas.obtener(3L)).thenReturn(new MatchClient.Partida(3L, 2L, 7L, 8L, "PROGRAMADA"));
        return r;
    }
    @Test void validaAntesDeSincronizarYReenviaToken() {
        Resultado r = preparar("PENDIENTE"); when(resultados.validarResultado(4L)).thenReturn(r);
        service.validar(4L, "Bearer firmado");
        var orden = inOrder(resultados, ranking);
        orden.verify(resultados).validarResultado(4L); orden.verify(ranking).recalcular(2L, "Bearer firmado");
    }
    @Test void reintentaValidadoSinModificarResultado() {
        preparar("VALIDADO");
        doThrow(new IllegalStateException("Sin red")).doNothing().when(ranking).recalcular(2L, "Bearer firmado");
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> service.validar(4L, "Bearer firmado")).getStatusCode().value());
        assertDoesNotThrow(() -> service.validar(4L, "Bearer firmado"));
        verify(resultados, times(2)).validarResultado(4L); verify(ranking, times(2)).recalcular(2L, "Bearer firmado");
        verify(resultados, never()).buscarPorId(any());
    }
    @Test void anularTambienActualizaClasificacion() {
        Resultado r = preparar("VALIDADO"); when(resultados.anularResultado(4L, "Evidencia incorrecta")).thenReturn(r);
        service.anular(4L, "Evidencia incorrecta", "Bearer firmado");
        verify(resultados).anularResultado(4L, "Evidencia incorrecta"); verify(ranking).recalcular(2L, "Bearer firmado");
    }
    @Test void reintentarAnuladoConservaJustificacionAnterior() {
        preparar("ANULADO"); service.anular(4L, "Motivo original", "Bearer firmado");
        verify(resultados).anularResultado(4L, "Motivo original"); verify(ranking).recalcular(2L, "Bearer firmado");
        verify(resultados, never()).buscarPorId(any());
    }
}
