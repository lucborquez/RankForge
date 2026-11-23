package com.esports.tournament_service.service;
import com.esports.tournament_service.model.NotificacionTorneo;
import com.esports.tournament_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.List;

@Service
public class NotificacionTorneoService {
    private final NotificacionTorneoRepository notificaciones;
    private final TorneoRepository torneos;
    public NotificacionTorneoService(NotificacionTorneoRepository notificaciones, TorneoRepository torneos) {
        this.notificaciones = notificaciones; this.torneos = torneos;
    }
    public List<NotificacionTorneo> listar(Long torneoId) {
        if (!torneos.existsById(torneoId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneo no encontrado");
        return notificaciones.findTop100ByTorneoIdOrderByIdDesc(torneoId);
    }
    // Se guarda junto con la operación: si falla, ambos cambios se revierten.
    @Transactional(propagation = Propagation.MANDATORY)
    public void publicar(Long torneoId, String tipo, String mensaje) {
        var n = new NotificacionTorneo(); n.setTorneoId(torneoId); n.setTipo(tipo); n.setMensaje(mensaje); n.setFecha(Instant.now());
        notificaciones.save(n);
    }
}
