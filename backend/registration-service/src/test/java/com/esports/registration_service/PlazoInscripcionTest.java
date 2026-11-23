package com.esports.registration_service;

import com.esports.registration_service.service.PlazoInscripcion;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlazoInscripcionTest {
    Clock reloj(String instant) { return Clock.fixed(Instant.parse(instant), ZoneId.of("America/Santiago")); }
    @Test void permiteUltimoInstanteDelDiaLocal() {
        assertDoesNotThrow(() -> PlazoInscripcion.verificar("2026-11-18", reloj("2026-11-19T02:59:59.999999999Z")));
    }
    @Test void rechazaSiguienteDiaLocal() {
        assertThrows(IllegalArgumentException.class, () -> PlazoInscripcion.verificar("2026-11-18", reloj("2026-11-19T03:00:00Z")));
    }
    @Test void respetaHoraConZonaExplicita() {
        assertDoesNotThrow(() -> PlazoInscripcion.verificar("2026-11-18T20:00:00-03:00", reloj("2026-11-18T23:00:00Z")));
        assertThrows(IllegalArgumentException.class, () -> PlazoInscripcion.verificar("2026-11-18T20:00:00-03:00", reloj("2026-11-18T23:00:01Z")));
    }
    @Test void interpretaHoraSinZonaEnChile() {
        assertDoesNotThrow(() -> PlazoInscripcion.verificar("2026-11-18T20:00:00", reloj("2026-11-18T23:00:00Z")));
    }
    @Test void rechazaAusenteOVacioOInvalido() {
        for (String cierre : new String[]{null, "", "ayer", "2026-99-30"}) {
            assertThrows(IllegalArgumentException.class, () -> PlazoInscripcion.verificar(cierre, reloj("2026-11-18T23:00:00Z")));
        }
    }
}
