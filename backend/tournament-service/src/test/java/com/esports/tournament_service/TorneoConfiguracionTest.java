package com.esports.tournament_service;
import com.esports.tournament_service.client.JuegoClient;
import com.esports.tournament_service.dto.TorneoRequestDTO;
import com.esports.tournament_service.model.Torneo;
import com.esports.tournament_service.repository.TorneoRepository;
import com.esports.tournament_service.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TorneoConfiguracionTest {
    TorneoRepository repo = mock(TorneoRepository.class);
    JuegoClient juegos = mock(JuegoClient.class);
    NotificacionTorneoService avisos = mock(NotificacionTorneoService.class);
    TorneoService service = new TorneoService(repo, juegos, avisos, mock(com.esports.tournament_service.client.ClasificacionClient.class));
    void rechaza(String estado) {
        Torneo t = new Torneo(); t.setEstado(estado); when(repo.buscarParaActualizar(1L)).thenReturn(Optional.of(t));
        var error = assertThrows(ResponseStatusException.class, () -> service.actualizar(1L, new TorneoRequestDTO()));
        assertEquals(409, error.getStatusCode().value()); verify(repo, never()).save(any()); verifyNoInteractions(juegos, avisos);
    }
    @Test void finalizadoConservaConfiguracion() { rechaza("FINALIZADO"); }
    @Test void canceladoConservaConfiguracion() { rechaza("CANCELADO"); }
    @Test void enCursoConservaConfiguracion() { rechaza("EN_CURSO"); }
    @Test void borradorPermiteCambiosValidos() {
        Torneo t = new Torneo(); t.setEstado("BORRADOR"); t.setJuegoId(2L);
        when(repo.buscarParaActualizar(1L)).thenReturn(Optional.of(t)); when(repo.save(any())).thenAnswer(c -> c.getArgument(0));
        TorneoRequestDTO d = new TorneoRequestDTO(); d.setNombre("Copa"); d.setJuegoId(2L); d.setCupoMaximo(8); d.setModalidad("1v1");
        d.setFechaCierreInscripcion("2026-12-01"); d.setFechaInicio("2026-12-02"); d.setFechaFin("2026-12-03");
        assertEquals("Copa", service.actualizar(1L, d).getNombre()); verify(repo).save(t);
    }
}
