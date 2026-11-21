package com.esports.ranking_service.service;

import com.esports.ranking_service.dto.RankingRequestDTO;
import com.esports.ranking_service.dto.RankingResponseDTO;
import com.esports.ranking_service.exception.RankingNotFoundException;
import com.esports.ranking_service.model.Ranking;
import com.esports.ranking_service.repository.RankingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {

    private static final Logger log = LoggerFactory.getLogger(RankingService.class);
    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    // Endpoint heredado: las filas se crean sólo al reconstruir resultados.
    public RankingResponseDTO crear(RankingRequestDTO dto) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "La clasificación se deriva de resultados validados. Corrige el resultado y usa /api/rankings/torneo/{torneoId}/recalcular");
    }

    // Listar todos los rankings
    public List<RankingResponseDTO> listarTodos() {
        return rankingRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // Buscar por ID
    public RankingResponseDTO buscarPorId(Long id) {
        Ranking ranking = rankingRepository.findById(id)
                .orElseThrow(() -> new RankingNotFoundException(id));
        return convertirAResponse(ranking);
    }

    // Listar rankings de un torneo ordenados por puntos (tabla de posiciones)
    public List<RankingResponseDTO> listarPorTorneo(Long torneoId) {
        log.info("Listando ranking del torneoId: {}", torneoId);
        return rankingRepository.findByTorneoIdOrderByPuntosDesc(torneoId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // Buscar ranking de un participante en un torneo
    public RankingResponseDTO buscarPorTorneoYParticipante(Long torneoId, Long participanteId) {
        Ranking ranking = rankingRepository.findByTorneoIdAndParticipanteId(torneoId, participanteId)
                .orElseThrow(() -> new RankingNotFoundException(participanteId));
        return convertirAResponse(ranking);
    }

    // Listar historial de rankings de un participante
    public List<RankingResponseDTO> listarPorParticipante(Long participanteId) {
        return rankingRepository.findByParticipanteId(participanteId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // Las correcciones se realizan sobre resultados, no sobre estadísticas derivadas.
    public RankingResponseDTO actualizar(Long id, RankingRequestDTO dto) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "La clasificación se deriva de resultados validados. Corrige el resultado y usa /api/rankings/torneo/{torneoId}/recalcular");
    }

    // No borrar estadísticas sin modificar su resultado de origen.
    public RankingResponseDTO reiniciar(Long id) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "La clasificación se deriva de resultados validados. Corrige el resultado y usa /api/rankings/torneo/{torneoId}/recalcular");
    }

    // Mantener la coherencia del torneo completo.
    public void eliminar(Long id) {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "La clasificación se deriva de resultados validados. Corrige el resultado y usa /api/rankings/torneo/{torneoId}/recalcular");
    }

    private RankingResponseDTO convertirAResponse(Ranking r) {
        RankingResponseDTO dto = new RankingResponseDTO();
        dto.setId(r.getId());
        dto.setTorneoId(r.getTorneoId());
        dto.setParticipanteId(r.getParticipanteId());
        dto.setPuntos(r.getPuntos());
        dto.setVictorias(r.getVictorias());
        dto.setDerrotas(r.getDerrotas());
        dto.setDiferencia(r.getDiferencia());
        dto.setPosicion(r.getPosicion());
        return dto;
    }
}
