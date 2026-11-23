package com.esports.tournament_service.service;
import com.esports.tournament_service.model.*;
import com.esports.tournament_service.dto.PremioRequestDTO;
import com.esports.tournament_service.repository.*;
import com.esports.tournament_service.client.ClasificacionClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
public class PremioService {
    private final TorneoRepository torneos;
    private final PremioRepository premios;
    private final ClasificacionClient clasificacion;
    private final NotificacionTorneoService notificaciones;
    public PremioService(TorneoRepository torneos, PremioRepository premios, ClasificacionClient clasificacion, NotificacionTorneoService notificaciones) {
        this.torneos = torneos; this.premios = premios; this.clasificacion = clasificacion;
        this.notificaciones = notificaciones;
    }
    public List<Premio> listar(Long torneoId) {
        Torneo torneo = torneos.findById(torneoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneo no encontrado"));
        return "FINALIZADO".equals(torneo.getEstado()) ? premios.findByTorneoIdOrderByPosicionAsc(torneoId) : List.of();
    }
    @Transactional
    public Premio asignar(Long torneoId, PremioRequestDTO dto) {
        Torneo torneo = torneos.buscarParaActualizar(torneoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneo no encontrado"));
        if (!"FINALIZADO".equals(torneo.getEstado())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Finaliza el torneo antes de asignar premios");
        if (dto.posicion() == null || dto.posicion() <= 0 || dto.participanteId() == null || dto.participanteId() <= 0 || dto.descripcion() == null || dto.descripcion().isBlank() || dto.descripcion().length() > 500) throw new IllegalArgumentException("Datos del premio inválidos");
        if (premios.existsByTorneoIdAndPosicion(torneoId, dto.posicion())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta posición ya tiene un premio");
        boolean puestoCorrecto = torneo.getClasificacionFinal().isEmpty()
                ? clasificacion.coincide(torneoId, dto.posicion(), dto.participanteId())
                : dto.participanteId().equals(torneo.getClasificacionFinal().get(dto.posicion()));
        if (!puestoCorrecto) throw new ResponseStatusException(HttpStatus.CONFLICT, "El participante no ocupa esa posición en la clasificación");
        Premio premio = new Premio(); premio.setTorneoId(torneoId); premio.setPosicion(dto.posicion()); premio.setParticipanteId(dto.participanteId()); premio.setDescripcion(dto.descripcion().trim());
        Premio guardado = premios.save(premio);
        notificaciones.publicar(torneoId, "PREMIO_ASIGNADO", "Premio asignado al participante #" + dto.participanteId() + " por el puesto " + dto.posicion() + ": " + dto.descripcion().trim());
        return guardado;
    }
}
