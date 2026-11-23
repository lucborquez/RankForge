package com.esports.registration_service;

import com.esports.registration_service.client.*;
import com.esports.registration_service.dto.InscripcionRequestDTO;
import com.esports.registration_service.model.Inscripcion;
import com.esports.registration_service.repository.InscripcionRepository;
import com.esports.registration_service.service.InscripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InscripcionReglasTest {
    InscripcionRepository repository = mock(InscripcionRepository.class);
    TorneoClient torneos = mock(TorneoClient.class);
    SancionClient sanciones = mock(SancionClient.class);
    InscripcionService service = new InscripcionService(repository, torneos, sanciones,
        Clock.fixed(Instant.parse("2026-11-18T15:00:00Z"), ZoneId.of("America/Santiago")),
        mock(com.esports.registration_service.service.EquipoReglas.class),
        mock(com.esports.registration_service.repository.CupoTorneoRepository.class));
    InscripcionRequestDTO request;
    TorneoClient.TorneoInfoDTO torneo;

    @BeforeEach void preparar() {
        request = new InscripcionRequestDTO();
        request.setTorneoId(1L); request.setJugadorId(2L); request.setTipoParticipante("JUGADOR");
        torneo = new TorneoClient.TorneoInfoDTO();
        torneo.setEstado("ABIERTO"); torneo.setCupoMaximo(8);
        torneo.setFechaCierreInscripcion("2026-11-18");
        when(torneos.obtenerTorneo(1L)).thenReturn(torneo);
        when(repository.save(any())).thenAnswer(call -> {
            Inscripcion i = call.getArgument(0); i.setId(3L); return i;
        });
    }

    @Test void reinscribeCuandoSoloHayInscripcionesCanceladas() {
        when(repository.existsByTorneoIdAndJugadorId(1L, 2L)).thenReturn(true);
        assertEquals("PENDIENTE", service.crear(request).getEstado());
        verify(repository).existsByTorneoIdAndJugadorIdAndEstadoNot(1L, 2L, "CANCELADA");
        verify(repository, never()).existsByTorneoIdAndJugadorId(anyLong(), anyLong());
    }
    @Test void rechazaDuplicadoVigente() {
        when(repository.existsByTorneoIdAndJugadorIdAndEstadoNot(1L, 2L, "CANCELADA")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }
    @Test void rechazaPlazoVencidoSinGuardar() {
        torneo.setFechaCierreInscripcion("2026-11-17");
        assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }
    @Test void rechazaCupoLleno() {
        when(repository.countByTorneoIdAndEstadoNot(1L, "CANCELADA")).thenReturn(8L);
        assertThrows(RuntimeException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }
    @Test void rechazaIdentidadAmbiguaAntesDeConsultarServicios() {
        request.setEquipoId(9L);
        assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        verifyNoInteractions(torneos, repository, sanciones);
    }
    @Test void rechazaEstadosInventados() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizarEstado(3L, "GANADOR"));
        verifyNoInteractions(repository);
    }
}
