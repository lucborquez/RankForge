package com.esports.tournament_service;
import com.esports.tournament_service.service.*;
import com.esports.tournament_service.repository.*;
import com.esports.tournament_service.model.*;
import com.esports.tournament_service.dto.CambioEstadoDTO;
import com.esports.tournament_service.client.JuegoClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificacionTorneoTest {
    NotificacionTorneoRepository repo = mock(NotificacionTorneoRepository.class);
    TorneoRepository torneos = mock(TorneoRepository.class);
    NotificacionTorneoService service = new NotificacionTorneoService(repo, torneos);
    @Test void torneoInexistenteEs404() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.listar(1L)).getStatusCode().value()); verifyNoInteractions(repo);
    }
    @Test void listaSoloAvisosDelTorneoSolicitado() {
        when(torneos.existsById(1L)).thenReturn(true); when(repo.findTop100ByTorneoIdOrderByIdDesc(1L)).thenReturn(List.of());
        assertTrue(service.listar(1L).isEmpty()); verify(repo).findTop100ByTorneoIdOrderByIdDesc(1L);
    }
    @Test void publicaConFechaYTorneo() {
        service.publicar(1L, "CAMBIO_ESTADO", "Finalizado");
        var captura = ArgumentCaptor.forClass(NotificacionTorneo.class); verify(repo).save(captura.capture());
        assertEquals(1L, captura.getValue().getTorneoId()); assertNotNull(captura.getValue().getFecha()); assertEquals("Finalizado", captura.getValue().getMensaje());
    }
    @Test void cambioDeEstadoPublicaAviso() {
        var avisos = mock(NotificacionTorneoService.class); var juego = mock(JuegoClient.class);
        var torneo = new Torneo(); torneo.setId(1L); torneo.setEstado("EN_CURSO");
        when(torneos.buscarParaActualizar(1L)).thenReturn(Optional.of(torneo)); when(torneos.save(any())).thenAnswer(c -> c.getArgument(0));
        var dto = new CambioEstadoDTO(); dto.setEstado("FINALIZADO");
        var ranking = mock(com.esports.tournament_service.client.ClasificacionClient.class);
        when(ranking.cerrar(1L, "Bearer prueba")).thenReturn(Map.of(1, 7L, 2, 8L));
        new TorneoService(torneos, juego, avisos, ranking).cambiarEstado(1L, dto, "Bearer prueba");
        assertEquals(Map.of(1, 7L, 2, 8L), torneo.getClasificacionFinal());
        verify(avisos).publicar(1L, "CAMBIO_ESTADO", "Estado del torneo: EN_CURSO → FINALIZADO");
    }
    @Test void transicionRechazadaNoPublicaAviso() {
        var avisos = mock(NotificacionTorneoService.class); var juego = mock(JuegoClient.class);
        var torneo = new Torneo(); torneo.setEstado("FINALIZADO"); when(torneos.buscarParaActualizar(1L)).thenReturn(Optional.of(torneo));
        var dto = new CambioEstadoDTO(); dto.setEstado("ABIERTO");
        assertThrows(RuntimeException.class, () -> new TorneoService(torneos, juego, avisos, mock(com.esports.tournament_service.client.ClasificacionClient.class)).cambiarEstado(1L, dto, "Bearer prueba")); verifyNoInteractions(avisos);
    }
    @Test void cierreRechazadoConservaEstadoYAvisos() {
        var avisos = mock(NotificacionTorneoService.class);
        var ranking = mock(com.esports.tournament_service.client.ClasificacionClient.class);
        var torneo = new Torneo(); torneo.setEstado("EN_CURSO");
        when(torneos.buscarParaActualizar(1L)).thenReturn(Optional.of(torneo));
        when(ranking.cerrar(1L, "Bearer prueba")).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT));
        var dto = new CambioEstadoDTO(); dto.setEstado("FINALIZADO");
        assertThrows(ResponseStatusException.class, () -> new TorneoService(torneos, mock(JuegoClient.class), avisos, ranking).cambiarEstado(1L, dto, "Bearer prueba"));
        assertEquals("EN_CURSO", torneo.getEstado());
        verify(torneos, never()).save(any()); verifyNoInteractions(avisos);
    }
}
