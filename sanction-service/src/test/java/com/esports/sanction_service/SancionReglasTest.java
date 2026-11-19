package com.esports.sanction_service;

import com.esports.sanction_service.dto.SancionRequestDTO;
import com.esports.sanction_service.model.Sancion;
import com.esports.sanction_service.repository.SancionRepository;
import com.esports.sanction_service.service.SancionService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SancionReglasTest {
    SancionRepository repository = mock(SancionRepository.class);
    SancionService service = new SancionService(repository);
    SancionRequestDTO datos() {
        var dto = new SancionRequestDTO(); dto.setUsuarioId(7L); dto.setMotivo("Motivo"); dto.setSeveridad("LEVE");
        dto.setFechaInicio(LocalDate.of(2030, 1, 1)); dto.setFechaFin(LocalDate.of(2030, 1, 3)); return dto;
    }
    @Test void rechazaDosDestinatariosSinGuardar() {
        var dto = datos(); dto.setEquipoId(3L);
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto)); verifyNoInteractions(repository);
    }
    @Test void rechazaSeveridadInventada() {
        var dto = datos(); dto.setSeveridad("PERMANENTE");
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto)); verifyNoInteractions(repository);
    }
    @Test void rechazaFechasInvertidas() {
        var dto = datos(); dto.setFechaFin(LocalDate.of(2029, 12, 31));
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto)); verifyNoInteractions(repository);
    }
    @Test void conservaParticipanteAlEditar() {
        var dto = datos(); Sancion s = new Sancion(); s.setUsuarioId(8L);
        when(repository.findById(1L)).thenReturn(Optional.of(s));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, dto)); verify(repository, never()).save(any());
    }
    @Test void normalizaSeveridadYGuardaDatosValidos() {
        var dto = datos(); dto.setSeveridad(" grave ");
        when(repository.save(any())).thenAnswer(c -> c.getArgument(0));
        var resultado = service.crear(dto);
        assertEquals("GRAVE", resultado.getSeveridad()); assertEquals(7L, resultado.getUsuarioId()); assertNull(resultado.getEquipoId());
    }
}
