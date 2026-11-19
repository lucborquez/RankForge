package com.esports.team_service;

import com.esports.team_service.client.UsuarioClient;
import com.esports.team_service.controller.TeamController;
import com.esports.team_service.dto.*;
import com.esports.team_service.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamAuthorizationTest {
    UsuarioClient users = mock(UsuarioClient.class);
    TeamService service = mock(TeamService.class);
    TeamController controller = new TeamController(service, users);
    @Test void creacionIgnoraCapitanSuplantado() {
        when(users.autenticar("Bearer validado")).thenReturn(new UsuarioClient.Actor(4L, "JUGADOR", true));
        EquipoRequestDTO dto = new EquipoRequestDTO(); dto.setCapitanId(99L);
        controller.crear(dto, "Bearer validado");
        verify(service).crear(argThat(datos -> datos.getCapitanId().equals(4L)));
    }
    @Test void ajenoNoPuedeDesactivar() {
        when(users.autenticar("Bearer validado")).thenReturn(new UsuarioClient.Actor(4L, "JUGADOR", true));
        EquipoResponseDTO equipo = new EquipoResponseDTO(); equipo.setCapitanId(7L);
        when(service.buscarPorId(1L)).thenReturn(equipo);
        assertEquals(403, assertThrows(ResponseStatusException.class,
            () -> controller.desactivar(1L, "Bearer validado")).getStatusCode().value());
        verify(service, never()).desactivar(any());
    }
    @Test void quitarUsaActorValidadoYNoParametrosDelCliente() {
        when(users.autenticar("Bearer validado")).thenReturn(new UsuarioClient.Actor(4L, "JUGADOR", true));
        assertEquals(204, controller.quitarMiembro(1L, 2L, "Bearer validado").getStatusCode().value());
        verify(service).quitarMiembro(1L, 2L, 4L, false);
    }
    @Test void tokenAusenteFallaSinRed() {
        assertEquals(401, assertThrows(ResponseStatusException.class,
            () -> new UsuarioClient().autenticar(null)).getStatusCode().value());
    }
}
