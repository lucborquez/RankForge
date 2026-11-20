package com.esports.resultservice;
import com.esports.resultservice.client.MatchClient;
import com.esports.resultservice.model.Resultado;
import com.esports.resultservice.model.dto.ResultadoRequestDTO;
import com.esports.resultservice.repository.ResultadoRepository;
import com.esports.resultservice.service.ResultadoService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResultadoReglasTest {
    ResultadoRepository repository = mock(ResultadoRepository.class);
    MatchClient partidas = mock(MatchClient.class);
    ResultadoService service = new ResultadoService(repository, partidas,
        mock(com.esports.resultservice.repository.ResultadoPartidaLockRepository.class));
    ResultadoRequestDTO dto = new ResultadoRequestDTO(3L, 7L, 2, 1, null);
    void partida() { when(partidas.obtener(3L)).thenReturn(new MatchClient.Partida(3L, 1L, 7L, 8L, "PROGRAMADA")); }
    @Test void aceptaGanadorCoherente() {
        partida(); when(repository.save(any())).thenAnswer(c -> c.getArgument(0));
        assertEquals(7L, service.crearResultado(dto).getGanadorId());
    }
    @Test void rechazaGanadorAjeno() {
        partida(); dto.setGanadorId(999L);
        assertThrows(IllegalArgumentException.class, () -> service.crearResultado(dto)); verify(repository, never()).save(any());
    }
    @Test void empateNoAdmiteGanador() {
        partida(); dto.setPuntajeB(2);
        assertThrows(IllegalArgumentException.class, () -> service.crearResultado(dto));
    }
    @Test void resultadoValidadoNoSeEdita() {
        Resultado r = new Resultado(); r.setEstadoValidacion("VALIDADO");
        when(repository.buscarParaModificar(4L)).thenReturn(Optional.of(r));
        assertThrows(IllegalStateException.class, () -> service.actualizarResultado(4L, dto)); verify(repository, never()).save(any());
    }
    @Test void anulacionRequiereMotivo() {
        assertThrows(IllegalArgumentException.class, () -> service.anularResultado(4L, "  ")); verifyNoInteractions(repository);
    }
    @Test void noValidaResultadoAntiguoConGanadorIncoherente() {
        partida();
        Resultado r = new Resultado(); r.setPartidaId(3L); r.setEstadoValidacion("PENDIENTE");
        r.setPuntajeA(2); r.setPuntajeB(1); r.setGanadorId(8L);
        when(repository.buscarParaModificar(4L)).thenReturn(Optional.of(r));
        assertThrows(IllegalArgumentException.class, () -> service.validarResultado(4L));
        assertEquals("PENDIENTE", r.getEstadoValidacion()); verify(repository, never()).save(any());
    }
    @Test void noValidaResultadoDePartidaCancelada() {
        when(partidas.obtener(3L)).thenReturn(new MatchClient.Partida(3L, 1L, 7L, 8L, "CANCELADA"));
        Resultado r = new Resultado(); r.setPartidaId(3L); r.setEstadoValidacion("PENDIENTE");
        r.setPuntajeA(2); r.setPuntajeB(1); r.setGanadorId(7L);
        when(repository.buscarParaModificar(4L)).thenReturn(Optional.of(r));
        assertThrows(IllegalArgumentException.class, () -> service.validarResultado(4L));
        verify(repository, never()).save(any());
    }
    @Test void validacionRepetidaNoVuelveAGuardar() {
        Resultado r = new Resultado(); r.setEstadoValidacion("VALIDADO");
        when(repository.buscarParaModificar(4L)).thenReturn(Optional.of(r));
        assertSame(r, service.validarResultado(4L));
        verify(repository, never()).save(any()); verifyNoInteractions(partidas);
    }
    @Test void anulacionRepetidaConservaMotivoOriginal() {
        Resultado r = new Resultado(); r.setEstadoValidacion("ANULADO"); r.setJustificacionAnulacion("Original");
        when(repository.buscarParaModificar(4L)).thenReturn(Optional.of(r));
        assertEquals("Original", service.anularResultado(4L, "Otro motivo").getJustificacionAnulacion());
        verify(repository, never()).save(any());
    }
}
