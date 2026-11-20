package com.esports.resultservice.service;

import com.esports.resultservice.client.MatchClient;
import com.esports.resultservice.model.dto.ResultadoRequestDTO;
import com.esports.resultservice.model.Resultado;
import com.esports.resultservice.repository.ResultadoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultadoService {

    private static final Logger log = LoggerFactory.getLogger(ResultadoService.class);

    private final ResultadoRepository resultadoRepository;
    private final MatchClient matchClient;
    private final com.esports.resultservice.repository.ResultadoPartidaLockRepository bloqueos;

    // Crear resultado validando que la partida exista y no haya duplicado
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Resultado crearResultado(ResultadoRequestDTO dto) {

        // Verificar que la partida existe en match-service
        verificarMarcador(dto);
        bloqueos.bloquear(dto.getPartidaId());

        // No registrar resultado duplicado para la misma partida
        if (!resultadoRepository.findByPartidaId(dto.getPartidaId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Ya existe un resultado para la partida ID: " + dto.getPartidaId());
        }

        Resultado resultado = new Resultado();
        resultado.setPartidaId(dto.getPartidaId());
        resultado.setGanadorId(dto.getGanadorId());
        resultado.setPuntajeA(dto.getPuntajeA());
        resultado.setPuntajeB(dto.getPuntajeB());
        resultado.setEvidenciaUrl(dto.getEvidenciaUrl());

        Resultado guardado = resultadoRepository.save(resultado);
        log.info("Resultado creado ID: {} para partidaId: {}",
                guardado.getId(), guardado.getPartidaId());
        return guardado;
    }

    // Listar resultados de una partida específica
    public List<Resultado> listarPorPartida(Long partidaId) {
        log.info("Listando resultados para partidaId: {}", partidaId);
        return resultadoRepository.findByPartidaId(partidaId);
    }

    // Listar todos los resultados
    public List<Resultado> listarTodos() {
        return resultadoRepository.findAll();
    }

    // Buscar resultado por ID
    public Resultado buscarPorId(Long id) {
        return resultadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resultado no encontrado con ID: " + id));
    }

    // Actualizar resultado (solo si no está validado)
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Resultado actualizarResultado(Long id, ResultadoRequestDTO dto) {
        Resultado existente = buscarParaModificar(id);

        if (!"PENDIENTE".equalsIgnoreCase(existente.getEstadoValidacion())) {
            throw new IllegalStateException(
                    "Un resultado con validación completa no puede ser alterado.");
        }

        if (!java.util.Objects.equals(existente.getPartidaId(), dto.getPartidaId())) {
            throw new IllegalArgumentException("No se puede cambiar la partida del resultado");
        }
        verificarMarcador(dto);

        existente.setGanadorId(dto.getGanadorId());
        existente.setPuntajeA(dto.getPuntajeA());
        existente.setPuntajeB(dto.getPuntajeB());
        existente.setEvidenciaUrl(dto.getEvidenciaUrl());

        Resultado actualizado = resultadoRepository.save(existente);
        log.info("Resultado actualizado ID: {}", actualizado.getId());
        return actualizado;
    }

    // Validar resultado (solo organizador puede hacerlo)
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Resultado validarResultado(Long id) {
        Resultado existente = buscarParaModificar(id);
        if ("VALIDADO".equals(existente.getEstadoValidacion())) return existente;

        if (!"PENDIENTE".equalsIgnoreCase(existente.getEstadoValidacion())) {
            throw new IllegalStateException(
                    "Solo se pueden validar resultados en estado PENDIENTE.");
        }

        verificarMarcador(new ResultadoRequestDTO(existente.getPartidaId(), existente.getGanadorId(),
                existente.getPuntajeA(), existente.getPuntajeB(), existente.getEvidenciaUrl()));

        existente.setEstadoValidacion("VALIDADO");
        Resultado validado = resultadoRepository.save(existente);
        log.info("Resultado validado ID: {}", validado.getId());
        return validado;
    }

    // Anular resultado con justificación
    @org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Resultado anularResultado(Long id, String justificacion) {
        if (justificacion == null || justificacion.isBlank()) {
            throw new IllegalArgumentException("La anulación requiere una justificación");
        }
        Resultado existente = buscarParaModificar(id);

        if ("ANULADO".equalsIgnoreCase(existente.getEstadoValidacion())) {
            return existente;
        }

        existente.setEstadoValidacion("ANULADO");
        existente.setJustificacionAnulacion(justificacion);

        Resultado anulado = resultadoRepository.save(existente);
        log.info("Resultado anulado ID: {} — justificacion: {}", id, justificacion);
        return anulado;
    }

    private void verificarMarcador(ResultadoRequestDTO dto) {
        if (dto.getPartidaId() == null || dto.getPartidaId() <= 0 || dto.getPuntajeA() == null
                || dto.getPuntajeB() == null || dto.getPuntajeA() < 0 || dto.getPuntajeB() < 0) {
            throw new IllegalArgumentException("Partida o puntajes inválidos");
        }
        var partida = matchClient.obtener(dto.getPartidaId());
        if ("CANCELADA".equals(partida.estado()) || partida.participanteAId() == null
                || partida.participanteBId() == null || partida.participanteAId().equals(partida.participanteBId())) {
            throw new IllegalArgumentException("La partida no admite resultados");
        }
        Long ganador = dto.getPuntajeA().equals(dto.getPuntajeB()) ? null
                : dto.getPuntajeA() > dto.getPuntajeB() ? partida.participanteAId() : partida.participanteBId();
        if (!java.util.Objects.equals(ganador, dto.getGanadorId())) {
            throw new IllegalArgumentException("El ganador no corresponde al marcador y participantes");
        }
    }

    private Resultado buscarParaModificar(Long id) {
        return resultadoRepository.buscarParaModificar(id).orElseThrow(() ->
                new IllegalArgumentException("Resultado no encontrado con ID: " + id));
    }
}
