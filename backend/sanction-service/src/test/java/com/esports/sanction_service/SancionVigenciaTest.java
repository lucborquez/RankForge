package com.esports.sanction_service;

import com.esports.sanction_service.model.Sancion;
import com.esports.sanction_service.repository.SancionRepository;
import com.esports.sanction_service.service.SancionService;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SancionVigenciaTest {
    SancionRepository repository = mock(SancionRepository.class);
    SancionService service = new SancionService(repository,
        Clock.fixed(Instant.parse("2026-11-20T02:59:59Z"), ZoneId.of("America/Santiago")));
    void sancion(String inicio, String fin, boolean estado) {
        Sancion s = new Sancion(); s.setEstado(estado);
        s.setFechaInicio(inicio == null ? null : LocalDate.parse(inicio));
        s.setFechaFin(fin == null ? null : LocalDate.parse(fin));
        when(repository.findByUsuarioIdAndEstadoTrue(7L)).thenReturn(List.of(s));
        when(repository.findByEquipoId(3L)).thenReturn(List.of(s));
    }
    @Test void incluyeUltimoDiaEnZonaChile() {
        sancion("2026-11-18", "2026-11-19", true);
        assertTrue(service.tieneSancionActiva(7L)); assertTrue(service.equipoTieneSancionActiva(3L));
    }
    @Test void excluyeSancionesVencidas() {
        sancion("2026-11-10", "2026-11-18", true);
        assertFalse(service.tieneSancionActiva(7L)); assertFalse(service.equipoTieneSancionActiva(3L));
    }
    @Test void excluyeSancionesFuturas() {
        sancion("2026-11-20", "2026-11-21", true);
        assertFalse(service.tieneSancionActiva(7L)); assertFalse(service.equipoTieneSancionActiva(3L));
    }
    @Test void excluyeSancionesCerradas() {
        sancion("2026-11-18", "2026-11-21", false);
        assertFalse(service.tieneSancionActiva(7L)); assertFalse(service.equipoTieneSancionActiva(3L));
    }
    @Test void incluyePrimerDia() {
        sancion("2026-11-19", "2026-11-21", true);
        assertTrue(service.tieneSancionActiva(7L)); assertTrue(service.equipoTieneSancionActiva(3L));
    }
    @Test void registroIncompletoNoHabilitaParticipante() {
        sancion(null, null, true);
        assertTrue(service.tieneSancionActiva(7L)); assertTrue(service.equipoTieneSancionActiva(3L));
    }
    @Test void fechasInvertidasRequierenRevision() {
        sancion("2026-11-20", "2026-11-18", true);
        assertTrue(service.tieneSancionActiva(7L)); assertTrue(service.equipoTieneSancionActiva(3L));
    }
}
