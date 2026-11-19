package com.esports.registration_service.service;

import java.time.*;
import java.time.format.DateTimeParseException;

public final class PlazoInscripcion {
    private PlazoInscripcion() {}
    public static void verificar(String cierre, Clock reloj) {
        if (cierre == null || cierre.isBlank()) throw new IllegalArgumentException("El torneo no tiene un cierre válido");
        Instant limite;
        try {
            if (cierre.length() == 10) {
                limite = LocalDate.parse(cierre).plusDays(1).atStartOfDay(reloj.getZone()).toInstant();
                if (!reloj.instant().isBefore(limite)) throw new IllegalArgumentException("El plazo de inscripción terminó");
                return;
            }
            try { limite = OffsetDateTime.parse(cierre).toInstant(); }
            catch (DateTimeParseException ex) { limite = LocalDateTime.parse(cierre).atZone(reloj.getZone()).toInstant(); }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("El torneo tiene una fecha de cierre inválida");
        }
        if (reloj.instant().isAfter(limite)) throw new IllegalArgumentException("El plazo de inscripción terminó");
    }
}
