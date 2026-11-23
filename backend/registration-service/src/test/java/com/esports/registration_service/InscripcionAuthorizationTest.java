package com.esports.registration_service;

import com.esports.registration_service.client.*;
import com.esports.registration_service.controller.InscripcionController;
import com.esports.registration_service.dto.*;
import com.esports.registration_service.service.InscripcionService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InscripcionAuthorizationTest {
    UsuarioClient usuarios = mock(UsuarioClient.class);
    EquipoClient equipos = mock(EquipoClient.class);
    InscripcionService service = mock(InscripcionService.class);
    InscripcionController controller = new InscripcionController(service, usuarios, equipos);
    void jugador() { when(usuarios.autenticar("Bearer validado")).thenReturn(new UsuarioClient.Actor(7L, "JUGADOR", true)); }
    @Test void noInscribeUnaIdentidadSuplantada() {
        jugador();
        var dto = new InscripcionRequestDTO(); dto.setTipoParticipante("JUGADOR"); dto.setJugadorId(999L);
        controller.crear(dto, "Bearer validado");
        verify(service).crear(argThat(i -> i.getJugadorId().equals(7L)));
    }
    @Test void jugadorNoApruebaInscripciones() {
        jugador();
        assertEquals(403, assertThrows(ResponseStatusException.class,
            () -> controller.actualizarEstado(1L, "APROBADA", "Bearer validado")).getStatusCode().value());
        verifyNoInteractions(service);
    }
    @Test void jugadorNoCancelaInscripcionAjena() {
        jugador();
        var dto = new InscripcionResponseDTO(); dto.setJugadorId(8L); dto.setTipoParticipante("JUGADOR");
        when(service.buscarPorId(1L)).thenReturn(dto);
        assertEquals(403, assertThrows(ResponseStatusException.class,
            () -> controller.cancelar(1L, "Bearer validado")).getStatusCode().value());
        verify(service, never()).cancelar(any());
    }
    @Test void capitanPuedeCancelarInscripcionDeSuEquipo() {
        jugador();
        var dto = new InscripcionResponseDTO(); dto.setEquipoId(2L); dto.setTipoParticipante("EQUIPO");
        when(service.buscarPorId(1L)).thenReturn(dto);
        when(equipos.obtener(2L)).thenReturn(new EquipoClient.Equipo(2L, 7L, true));
        controller.cancelar(1L, "Bearer validado");
        verify(service).cancelar(1L);
    }
    @Test void organizadorPuedeAprobar() {
        when(usuarios.autenticar("Bearer validado")).thenReturn(new UsuarioClient.Actor(9L, "ORGANIZADOR", true));
        controller.actualizarEstado(1L, "APROBADA", "Bearer validado");
        verify(service).actualizarEstado(1L, "APROBADA");
    }
}
