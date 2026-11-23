package com.esports.tournament_service.service;

import com.esports.tournament_service.client.JuegoClient;
import com.esports.tournament_service.dto.*;
import com.esports.tournament_service.model.Torneo;
import com.esports.tournament_service.repository.TorneoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TorneoService {

    private static final Logger log = LoggerFactory.getLogger(TorneoService.class);

    private final TorneoRepository torneoRepository;
    private final JuegoClient juegoClient;
    private final NotificacionTorneoService notificaciones;

    private final com.esports.tournament_service.client.ClasificacionClient clasificacion;

    public TorneoService(TorneoRepository torneoRepository,
                         JuegoClient juegoClient, NotificacionTorneoService notificaciones,
                         com.esports.tournament_service.client.ClasificacionClient clasificacion) {
        this.clasificacion = clasificacion;
        this.torneoRepository = torneoRepository;
        this.juegoClient = juegoClient;
        this.notificaciones = notificaciones;
    }

    // Crear torneo
    public TorneoResponseDTO crear(TorneoRequestDTO dto) {
        validarFechas(dto);
        validarJuegoActivo(dto.getJuegoId());

        Torneo torneo = new Torneo();
        torneo.setNombre(dto.getNombre());
        torneo.setJuegoId(dto.getJuegoId());
        torneo.setFechaInicio(dto.getFechaInicio());
        torneo.setFechaFin(dto.getFechaFin());
        torneo.setFechaCierreInscripcion(dto.getFechaCierreInscripcion());
        torneo.setCupoMaximo(dto.getCupoMaximo());
        torneo.setModalidad(dto.getModalidad());
        // Siempre empieza en BORRADOR
        torneo.setEstado("BORRADOR");

        Torneo guardado = torneoRepository.save(torneo);
        log.info("Torneo creado: {} con ID: {}", guardado.getNombre(), guardado.getId());
        return convertirAResponse(guardado);
    }

    // Listar todos o filtrar por juego y/o estado
    public List<TorneoResponseDTO> listar(Long juegoId, String estado) {
        List<Torneo> torneos;

        if (juegoId != null && estado != null) {
            torneos = torneoRepository.findByJuegoIdAndEstado(juegoId, estado);
        } else if (juegoId != null) {
            torneos = torneoRepository.findByJuegoId(juegoId);
        } else if (estado != null) {
            torneos = torneoRepository.findByEstado(estado);
        } else {
            torneos = torneoRepository.findAll();
        }

        return torneos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // Buscar por ID
    public TorneoResponseDTO buscarPorId(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Torneo no encontrado con ID: " + id));
        return convertirAResponse(torneo);
    }

    // Actualizar fechas, cupos o modalidad
    @Transactional
    public TorneoResponseDTO actualizar(Long id, TorneoRequestDTO dto) {
        Torneo torneo = torneoRepository.buscarParaActualizar(id)
                .orElseThrow(() -> new RuntimeException(
                        "Torneo no encontrado con ID: " + id));

        // La configuración queda cerrada desde el inicio de la competición.
        if (!List.of("BORRADOR", "ABIERTO", "CERRADO").contains(torneo.getEstado())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "No se puede modificar la configuración de un torneo en curso, finalizado o cancelado"
            );
        }

        validarFechas(dto);
        if (!Objects.equals(torneo.getJuegoId(), dto.getJuegoId())) {
            validarJuegoActivo(dto.getJuegoId());
        }

        torneo.setNombre(dto.getNombre());
        torneo.setJuegoId(dto.getJuegoId());
        torneo.setFechaInicio(dto.getFechaInicio());
        torneo.setFechaFin(dto.getFechaFin());
        torneo.setFechaCierreInscripcion(dto.getFechaCierreInscripcion());
        torneo.setCupoMaximo(dto.getCupoMaximo());
        torneo.setModalidad(dto.getModalidad());

        Torneo actualizado = torneoRepository.save(torneo);
        log.info("Torneo actualizado: ID {}", actualizado.getId());
        return convertirAResponse(actualizado);
    }

    // Cambiar estado del torneo
    @Transactional
    public TorneoResponseDTO cambiarEstado(Long id, CambioEstadoDTO dto, String authorization) {
        Torneo torneo = torneoRepository.buscarParaActualizar(id)
                .orElseThrow(() -> new RuntimeException(
                        "Torneo no encontrado con ID: " + id));

        // Validar transiciones de estado permitidas
        String estadoAnterior = torneo.getEstado();
        validarTransicionEstado(estadoAnterior, dto.getEstado());
        if ("ABIERTO".equals(dto.getEstado())) validarJuegoActivo(torneo.getJuegoId());

        if ("FINALIZADO".equals(dto.getEstado())) {
            torneo.getClasificacionFinal().putAll(clasificacion.cerrar(id, authorization));
        }
        torneo.setEstado(dto.getEstado());
        Torneo actualizado = torneoRepository.save(torneo);
        notificaciones.publicar(id, "CAMBIO_ESTADO", "Estado del torneo: " + estadoAnterior + " → " + dto.getEstado());
        log.info("Torneo ID {} cambió estado: {} → {}",
                id, estadoAnterior, dto.getEstado());
        return convertirAResponse(actualizado);
    }

    private void validarJuegoActivo(Long juegoId) {
        if (!juegoClient.juegoExiste(juegoId)) {
            throw new IllegalArgumentException("El juego no existe o está inactivo");
        }
    }

    private void validarFechas(TorneoRequestDTO dto) {
        LocalDateTime cierre = leerFecha(dto.getFechaCierreInscripcion());
        LocalDateTime inicio = leerFecha(dto.getFechaInicio());
        LocalDateTime fin = leerFecha(dto.getFechaFin());
        if (!inicio.isAfter(cierre)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser posterior al cierre de inscripción");
        }
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private LocalDateTime leerFecha(String valor) {
        try {
            // Los registros anteriores pueden contener una fecha sola o una fecha con hora.
            return valor.length() == 10
                    ? LocalDate.parse(valor).atStartOfDay()
                    : LocalDateTime.parse(valor.replace(' ', 'T'));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Usa fechas y horas en formato ISO", ex);
        }
    }

    // Verifica que la transición de estado sea válida
    // Ej: no puedes pasar de FINALIZADO a ABIERTO
    private void validarTransicionEstado(String estadoActual, String estadoNuevo) {
        boolean valido = switch (estadoActual) {
            case "BORRADOR"   -> List.of("ABIERTO", "CANCELADO").contains(estadoNuevo);
            case "ABIERTO"    -> List.of("CERRADO", "CANCELADO").contains(estadoNuevo);
            case "CERRADO"    -> List.of("EN_CURSO", "CANCELADO").contains(estadoNuevo);
            case "EN_CURSO"   -> List.of("FINALIZADO", "CANCELADO").contains(estadoNuevo);
            case "FINALIZADO" -> false;
            case "CANCELADO"  -> false;
            default -> false;
        };

        if (!valido) {
            throw new RuntimeException(
                    "Transición de estado inválida: " + estadoActual + " → " + estadoNuevo
            );
        }
    }

    // Para que otros servicios consulten si el torneo está abierto
    public boolean torneoEstaAbierto(Long id) {
        return torneoRepository.findById(id)
                .map(t -> t.getEstado().equals("ABIERTO"))
                .orElse(false);
    }

    // Para que registration-service consulte el cupo máximo
    public TorneoResponseDTO obtenerParaInscripcion(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Torneo no encontrado con ID: " + id));

        if (!torneo.getEstado().equals("ABIERTO")) {
            throw new RuntimeException("El torneo no está abierto para inscripciones");
        }
        return convertirAResponse(torneo);
    }

    private TorneoResponseDTO convertirAResponse(Torneo t) {
        TorneoResponseDTO r = new TorneoResponseDTO();
        r.setId(t.getId());
        r.setNombre(t.getNombre());
        r.setJuegoId(t.getJuegoId());
        r.setFechaInicio(t.getFechaInicio());
        r.setFechaFin(t.getFechaFin());
        r.setFechaCierreInscripcion(t.getFechaCierreInscripcion());
        r.setCupoMaximo(t.getCupoMaximo());
        r.setEstado(t.getEstado());
        r.setModalidad(t.getModalidad());
        return r;
    }
}
