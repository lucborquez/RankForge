package com.esports.team_service;

import com.esports.team_service.client.UsuarioClient;
import com.esports.team_service.model.Equipo;
import com.esports.team_service.model.MiembroEquipo;
import com.esports.team_service.repository.EquipoRepository;
import com.esports.team_service.repository.MiembroEquipoRepository;
import com.esports.team_service.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamMembersTest {
    EquipoRepository equipos;
    MiembroEquipoRepository miembros;
    TeamService service;
    Equipo equipo;
    MiembroEquipo miembro;

    @BeforeEach void preparar() {
        equipos = mock(EquipoRepository.class);
        miembros = mock(MiembroEquipoRepository.class);
        service = new TeamService(equipos, miembros, mock(UsuarioClient.class));
        equipo = new Equipo(1L, "Arena", 10L, 3L, true);
        miembro = new MiembroEquipo(2L, 1L, 11L, "JUGADOR");
        when(equipos.findById(1L)).thenReturn(Optional.of(equipo));
        when(miembros.findById(2L)).thenReturn(Optional.of(miembro));
    }
    @Test void capitanQuitaIntegrante() {
        service.quitarMiembro(1L, 2L, 10L, false);
        verify(miembros).delete(miembro);
    }
    @Test void rechazaActorAjeno() {
        assertEquals(403, assertThrows(ResponseStatusException.class,
            () -> service.quitarMiembro(1L, 2L, 99L, false)).getStatusCode().value());
        verify(miembros, never()).delete(any());
    }
    @Test void preservaCapitanInclusoParaAdmin() {
        miembro.setUsuarioId(10L);
        assertEquals(409, assertThrows(ResponseStatusException.class,
            () -> service.quitarMiembro(1L, 2L, 99L, true)).getStatusCode().value());
        verify(miembros, never()).delete(any());
    }
    @Test void noPermiteMiembroDeOtroEquipo() {
        miembro.setEquipoId(9L);
        assertEquals(404, assertThrows(ResponseStatusException.class,
            () -> service.quitarMiembro(1L, 2L, 10L, false)).getStatusCode().value());
        verify(miembros, never()).delete(any());
    }
    @Test void rechazaEquipoInactivo() {
        equipo.setEstado(false);
        assertEquals(409, assertThrows(ResponseStatusException.class,
            () -> service.quitarMiembro(1L, 2L, 10L, false)).getStatusCode().value());
        verify(miembros, never()).delete(any());
    }
}
