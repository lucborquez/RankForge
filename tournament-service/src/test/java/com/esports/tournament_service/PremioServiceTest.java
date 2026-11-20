package com.esports.tournament_service;
import com.esports.tournament_service.model.*;
import com.esports.tournament_service.dto.PremioRequestDTO;
import com.esports.tournament_service.repository.*;
import com.esports.tournament_service.client.ClasificacionClient;
import com.esports.tournament_service.service.PremioService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class PremioServiceTest {
    TorneoRepository torneos = mock(TorneoRepository.class);
    PremioRepository premios = mock(PremioRepository.class);
    ClasificacionClient ranking = mock(ClasificacionClient.class);
    PremioService service = new PremioService(torneos, premios, ranking);
    PremioRequestDTO dto = new PremioRequestDTO(1, 7L, "Trofeo campeón");
    void torneo(String estado) {
        Torneo t = new Torneo(); t.setEstado(estado);
        when(torneos.findById(1L)).thenReturn(Optional.of(t)); when(torneos.buscarParaActualizar(1L)).thenReturn(Optional.of(t));
    }
    @Test void noPublicaPremiosAntesDelFinal() { torneo("EN_CURSO"); assertTrue(service.listar(1L).isEmpty()); verifyNoInteractions(premios); }
    @Test void noAsignaAntesDelFinal() { torneo("ABIERTO"); assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.asignar(1L, dto)).getStatusCode().value()); verifyNoInteractions(premios, ranking); }
    @Test void exigeGanadorEnPosicionCorrecta() { torneo("FINALIZADO"); assertThrows(ResponseStatusException.class, () -> service.asignar(1L, dto)); verify(premios, never()).save(any()); }
    @Test void asignaPremioDeTorneoFinalizado() {
        torneo("FINALIZADO"); when(ranking.coincide(1L, 1, 7L)).thenReturn(true); when(premios.save(any())).thenAnswer(c -> c.getArgument(0));
        Premio p = service.asignar(1L, dto); assertEquals(7L, p.getParticipanteId()); assertEquals("Trofeo campeón", p.getDescripcion());
    }
    @Test void rechazaPosicionDuplicada() { torneo("FINALIZADO"); when(premios.existsByTorneoIdAndPosicion(1L, 1)).thenReturn(true); assertThrows(ResponseStatusException.class, () -> service.asignar(1L, dto)); verify(premios, never()).save(any()); }
}
