package com.esports.registration_service.service;

import com.esports.registration_service.client.*;
import org.springframework.stereotype.Service;
import java.util.HashSet;

@Service
public class EquipoReglas {
    private final EquipoClient equipos;
    private final JuegoClient juegos;
    private final SancionClient sanciones;
    private final UsuarioClient usuarios;
    public EquipoReglas(EquipoClient equipos, JuegoClient juegos, SancionClient sanciones, UsuarioClient usuarios) {
        this.equipos = equipos; this.juegos = juegos; this.sanciones = sanciones; this.usuarios = usuarios;
    }
    public void verificar(Long equipoId, Long juegoId) {
        var equipo = equipos.obtener(equipoId);
        if (!Boolean.TRUE.equals(equipo.estado())) throw new IllegalArgumentException("El equipo está inactivo");
        if (juegoId == null || !juegoId.equals(equipo.juegoPrincipalId())) throw new IllegalArgumentException("El equipo no corresponde al juego del torneo");
        var juego = juegos.obtener(juegoId);
        if (!Boolean.TRUE.equals(juego.estado()) || juego.jugadoresPorEquipo() == null || juego.jugadoresPorEquipo() <= 0) {
            throw new IllegalArgumentException("El juego no tiene una configuración válida");
        }
        if (equipo.miembros() == null || equipo.miembros().size() != juego.jugadoresPorEquipo()) {
            throw new IllegalArgumentException("El equipo debe completar exactamente " + juego.jugadoresPorEquipo() + " integrantes");
        }
        var ids = new HashSet<Long>();
        for (var miembro : equipo.miembros()) {
            if (miembro == null || miembro.usuarioId() == null || !ids.add(miembro.usuarioId())) throw new IllegalArgumentException("Integrantes inválidos o repetidos");
            if (!usuarios.usuarioExisteYEstaActivo(miembro.usuarioId())) throw new IllegalArgumentException("Un integrante está inactivo o no pudo verificarse");
            if (sanciones.tieneSancionActiva(miembro.usuarioId())) throw new IllegalArgumentException("Un integrante tiene una sanción activa");
        }
        if (!ids.contains(equipo.capitanId())) throw new IllegalArgumentException("El capitán debe integrar el equipo");
        if (sanciones.equipoSancionado(equipoId)) throw new IllegalArgumentException("El equipo tiene una sanción activa");
    }
}
