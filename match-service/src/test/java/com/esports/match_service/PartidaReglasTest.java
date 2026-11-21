package com.esports.match_service;

import com.esports.match_service.client.*;
import com.esports.match_service.dto.PartidaRequestDTO;
import com.esports.match_service.repository.PartidaRepository;
import com.esports.match_service.service.PartidaService;
import com.esports.match_service.model.Partida;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PartidaReglasTest {
    PartidaRepository repo = mock(PartidaRepository.class);
    TorneoClient torneo = mock(TorneoClient.class);
    InscripcionClient inscripciones = mock(InscripcionClient.class);
    PartidaService service = new PartidaService(repo, torneo, inscripciones);
    PartidaRequestDTO datos() {
        var d = new PartidaRequestDTO(); d.setTorneoId(1L); d.setParticipanteAId(2L); d.setParticipanteBId(3L); d.setRonda("Final"); d.setFechaHora("2026-12-01T12:00"); return d;
    }
    @Test void rechazaEnfrentarseConsigoMismo() {
        var d = datos(); d.setParticipanteBId(2L); assertThrows(IllegalArgumentException.class, () -> service.crear(d)); verifyNoInteractions(repo, torneo, inscripciones);
    }
    @Test void rechazaHorarioInvalido() {
        var d = datos(); d.setFechaHora("mañana"); assertThrows(IllegalArgumentException.class, () -> service.crear(d)); verifyNoInteractions(repo);
    }
    @Test void rechazaEnfrentamientoInvertidoEnMismaRonda() {
        when(torneo.torneoExiste(1L)).thenReturn(true); when(inscripciones.participanteInscrito(anyLong(), anyLong())).thenReturn(true);
        when(repo.existsByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(1L, 3L, 2L, "Final")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.crear(datos())); verify(repo, never()).save(any());
    }
    @Test void reprogramarNoCambiaParticipantes() {
        Partida p = new Partida(); p.setId(5L); p.setTorneoId(1L); p.setParticipanteAId(2L); p.setParticipanteBId(4L); p.setEstado("PROGRAMADA");
        when(repo.findById(5L)).thenReturn(Optional.of(p));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(5L, datos())); verify(repo, never()).save(any());
    }
}
