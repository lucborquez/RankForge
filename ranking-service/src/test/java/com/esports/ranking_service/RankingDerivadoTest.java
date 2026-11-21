package com.esports.ranking_service;
import com.esports.ranking_service.dto.RankingRequestDTO;
import com.esports.ranking_service.repository.RankingRepository;
import com.esports.ranking_service.service.RankingService;
import com.esports.ranking_service.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RankingDerivadoTest {
    RankingRepository repo = mock(RankingRepository.class);
    RankingService service = new RankingService(repo);
    void conflicto(org.junit.jupiter.api.function.Executable operacion) {
        var error = assertThrows(ResponseStatusException.class, operacion);
        assertEquals(409, error.getStatusCode().value());
        assertTrue(error.getReason().contains("recalcular"));
        assertEquals(409, new GlobalExceptionHandler().handleStatus(error).getStatusCode().value());
        verifyNoInteractions(repo);
    }
    @Test void noPermiteCrearPuntosManualmente() { conflicto(() -> service.crear(new RankingRequestDTO())); }
    @Test void noPermiteEditarPuntosManualmente() { conflicto(() -> service.actualizar(1L, new RankingRequestDTO())); }
    @Test void noPermiteReiniciarUnaFilaManualmente() { conflicto(() -> service.reiniciar(1L)); }
    @Test void noPermiteEliminarUnaFilaManualmente() { conflicto(() -> service.eliminar(1L)); }
}
