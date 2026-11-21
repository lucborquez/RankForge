package com.esports.match_service.service;

import com.esports.match_service.client.InscripcionClient;
import com.esports.match_service.client.TorneoClient;
import com.esports.match_service.dto.PartidaRequestDTO;
import com.esports.match_service.dto.PartidaResponseDTO;
import com.esports.match_service.exception.PartidaNotFoundException;
import com.esports.match_service.model.Partida;
import com.esports.match_service.repository.PartidaRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Transactional
@Service
public class PartidaService {

    private static final Logger log = LoggerFactory.getLogger(PartidaService.class);

    private final PartidaRepository partidaRepository;
    private final TorneoClient torneoClient;
    private final InscripcionClient inscripcionClient;

    public PartidaService(PartidaRepository partidaRepository,
                          TorneoClient torneoClient,
                          InscripcionClient inscripcionClient) {
        this.partidaRepository = partidaRepository;
        this.torneoClient = torneoClient;
        this.inscripcionClient = inscripcionClient;
    }

    public PartidaResponseDTO crear(PartidaRequestDTO dto) {

        validarDatos(dto);

        // Verificar que el torneo existe
        if (!torneoClient.torneoExiste(dto.getTorneoId())) {
            throw new RuntimeException("El torneo no existe");
        }

        // Verificar que los participantes estén inscritos
        if (!inscripcionClient.participanteInscrito(
                dto.getTorneoId(), dto.getParticipanteAId())) {
            throw new RuntimeException(
                    "El participante A no está inscrito en el torneo");
        }
        if (!inscripcionClient.participanteInscrito(
                dto.getTorneoId(), dto.getParticipanteBId())) {
            throw new RuntimeException(
                    "El participante B no está inscrito en el torneo");
        }

        // No duplicar enfrentamiento en la misma ronda
        if (partidaRepository.existsByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(
                dto.getTorneoId(), dto.getParticipanteAId(),
                dto.getParticipanteBId(), dto.getRonda()) ||
                partidaRepository.existsByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(
                dto.getTorneoId(), dto.getParticipanteBId(), dto.getParticipanteAId(), dto.getRonda())) {
            throw new RuntimeException(
                    "Ya existe este enfrentamiento en la misma ronda");
        }

        Partida partida = new Partida();
        partida.setTorneoId(dto.getTorneoId());
        partida.setParticipanteAId(dto.getParticipanteAId());
        partida.setParticipanteBId(dto.getParticipanteBId());
        partida.setRonda(dto.getRonda());
        partida.setFechaHora(dto.getFechaHora());
        partida.setEstado("PROGRAMADA");

        Partida guardada = partidaRepository.save(partida);
        log.info("Partida creada ID: {} en torneoId: {}",
                guardada.getId(), guardada.getTorneoId());
        return convertirAResponse(guardada);
    }

    public List<PartidaResponseDTO> listarTodas() {
        return partidaRepository.findAll()
                .stream().map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public List<PartidaResponseDTO> listarPorTorneo(Long torneoId) {
        log.info("Listando partidas del torneoId: {}", torneoId);
        return partidaRepository.findByTorneoId(torneoId)
                .stream().map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public PartidaResponseDTO buscarPorId(Long id) {
        Partida p = partidaRepository.findById(id)
                .orElseThrow(() -> new PartidaNotFoundException(id));
        return convertirAResponse(p);
    }

    public PartidaResponseDTO actualizar(Long id, PartidaRequestDTO dto) {
        validarDatos(dto);
        Partida p = partidaRepository.findById(id)
                .orElseThrow(() -> new PartidaNotFoundException(id));

        if (!"PROGRAMADA".equals(p.getEstado())) {
            throw new RuntimeException(
                    "No se puede modificar una partida cancelada o finalizada");
        }

        if (!java.util.Objects.equals(p.getTorneoId(), dto.getTorneoId()) ||
                !java.util.Objects.equals(p.getParticipanteAId(), dto.getParticipanteAId()) ||
                !java.util.Objects.equals(p.getParticipanteBId(), dto.getParticipanteBId())) {
            throw new IllegalArgumentException("Reprogramar no permite cambiar torneo ni participantes");
        }
        boolean duplicada = partidaRepository.findByTorneoIdAndRonda(p.getTorneoId(), dto.getRonda()).stream()
                .anyMatch(otra -> !java.util.Objects.equals(otra.getId(), id) &&
                        ((java.util.Objects.equals(otra.getParticipanteAId(), p.getParticipanteAId()) && java.util.Objects.equals(otra.getParticipanteBId(), p.getParticipanteBId())) ||
                        (java.util.Objects.equals(otra.getParticipanteAId(), p.getParticipanteBId()) && java.util.Objects.equals(otra.getParticipanteBId(), p.getParticipanteAId()))));
        if (duplicada) throw new IllegalArgumentException("Ya existe este enfrentamiento en la misma ronda");

        p.setFechaHora(dto.getFechaHora());
        p.setRonda(dto.getRonda());

        Partida actualizada = partidaRepository.save(p);
        log.info("Partida actualizada: ID {}", actualizada.getId());
        return convertirAResponse(actualizada);
    }

    public PartidaResponseDTO cancelar(Long id) {
        Partida p = partidaRepository.findById(id)
                .orElseThrow(() -> new PartidaNotFoundException(id));

        if ("CANCELADA".equals(p.getEstado())) {
            throw new RuntimeException("La partida ya está cancelada");
        }

        p.setEstado("CANCELADA");
        Partida cancelada = partidaRepository.save(p);
        log.info("Partida cancelada: ID {}", id);
        return convertirAResponse(cancelada);
    }

    private void validarDatos(PartidaRequestDTO dto) {
        if (dto.getTorneoId() == null || dto.getTorneoId() <= 0 || dto.getParticipanteAId() == null || dto.getParticipanteAId() <= 0 ||
                dto.getParticipanteBId() == null || dto.getParticipanteBId() <= 0 || dto.getParticipanteAId().equals(dto.getParticipanteBId())) {
            throw new IllegalArgumentException("Selecciona torneo y dos participantes distintos con IDs positivos");
        }
        if (dto.getRonda() == null || dto.getRonda().isBlank() || dto.getFechaHora() == null) throw new IllegalArgumentException("Ronda y horario son obligatorios");
        try { java.time.LocalDateTime.parse(dto.getFechaHora()); }
        catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException("Horario inválido: usa fecha y hora ISO", ex); }
    }

    private PartidaResponseDTO convertirAResponse(Partida p) {
        PartidaResponseDTO dto = new PartidaResponseDTO();
        dto.setId(p.getId());
        dto.setTorneoId(p.getTorneoId());
        dto.setParticipanteAId(p.getParticipanteAId());
        dto.setParticipanteBId(p.getParticipanteBId());
        dto.setRonda(p.getRonda());
        dto.setFechaHora(p.getFechaHora());
        dto.setEstado(p.getEstado());
        return dto;
    }
}
