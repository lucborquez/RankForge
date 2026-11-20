package com.esports.registration_service;

import com.esports.registration_service.client.*;
import com.esports.registration_service.service.EquipoReglas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipoReglasTest {
    EquipoClient equipos = mock(EquipoClient.class);
    JuegoClient juegos = mock(JuegoClient.class);
    SancionClient sanciones = mock(SancionClient.class);
    UsuarioClient usuarios = mock(UsuarioClient.class);
    EquipoReglas reglas = new EquipoReglas(equipos, juegos, sanciones, usuarios);
    List<EquipoClient.Miembro> miembros = List.of(new EquipoClient.Miembro(1L, 7L, "CAPITAN"), new EquipoClient.Miembro(2L, 8L, "JUGADOR"));
    @BeforeEach void preparar() {
        when(equipos.obtener(1L)).thenReturn(new EquipoClient.Equipo(1L, 7L, true, 3L, miembros));
        when(juegos.obtener(3L)).thenReturn(new JuegoClient.Juego(3L, 2, true));
        when(usuarios.usuarioExisteYEstaActivo(anyLong())).thenReturn(true);
    }
    @Test void permiteEquipoCompletoCompatibleYHabilitado() {
        assertDoesNotThrow(() -> reglas.verificar(1L, 3L));
        verify(sanciones).tieneSancionActiva(7L); verify(sanciones).tieneSancionActiva(8L);
        verify(sanciones).equipoSancionado(1L);
    }
    @Test void juegoDeEquipoNoAdmiteInscripcionIndividual() {
        assertThrows(IllegalArgumentException.class, () -> reglas.verificarTipo("JUGADOR", 3L));
        assertDoesNotThrow(() -> reglas.verificarTipo("EQUIPO", 3L));
    }
    @Test void juegoIndividualNoAdmiteEquipo() {
        when(juegos.obtener(3L)).thenReturn(new JuegoClient.Juego(3L, 1, true));
        assertThrows(IllegalArgumentException.class, () -> reglas.verificarTipo("EQUIPO", 3L));
        assertDoesNotThrow(() -> reglas.verificarTipo("JUGADOR", 3L));
    }
    @Test void juegoInactivoNoAdmiteInscripciones() {
        when(juegos.obtener(3L)).thenReturn(new JuegoClient.Juego(3L, 1, false));
        assertThrows(IllegalArgumentException.class, () -> reglas.verificarTipo("JUGADOR", 3L));
    }
    @Test void torneoSinJuegoNoConsultaRed() {
        assertThrows(IllegalArgumentException.class, () -> reglas.verificarTipo("JUGADOR", null));
        verify(juegos, never()).obtener(any());
    }
    @Test void rechazaEquipoInactivo() {
        when(equipos.obtener(1L)).thenReturn(new EquipoClient.Equipo(1L, 7L, false, 3L, miembros));
        assertThrows(IllegalArgumentException.class, () -> reglas.verificar(1L, 3L));
    }
    @Test void rechazaJuegoDistinto() { assertThrows(IllegalArgumentException.class, () -> reglas.verificar(1L, 4L)); }
    @Test void rechazaEquipoIncompleto() {
        when(juegos.obtener(3L)).thenReturn(new JuegoClient.Juego(3L, 5, true));
        assertThrows(IllegalArgumentException.class, () -> reglas.verificar(1L, 3L));
    }
    @Test void rechazaMiembroSancionado() {
        when(sanciones.tieneSancionActiva(8L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> reglas.verificar(1L, 3L));
    }
    @Test void rechazaEquipoSancionado() {
        when(sanciones.equipoSancionado(1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> reglas.verificar(1L, 3L));
    }
    @Test void rechazaMiembroInactivo() {
        when(usuarios.usuarioExisteYEstaActivo(8L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> reglas.verificar(1L, 3L));
    }
}
