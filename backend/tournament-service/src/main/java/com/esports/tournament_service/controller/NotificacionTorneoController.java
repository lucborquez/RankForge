package com.esports.tournament_service.controller;
import com.esports.tournament_service.model.NotificacionTorneo;
import com.esports.tournament_service.service.NotificacionTorneoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/torneos/{torneoId}/notificaciones")
public class NotificacionTorneoController {
    private final NotificacionTorneoService service;
    public NotificacionTorneoController(NotificacionTorneoService service) { this.service = service; }
    @GetMapping public List<NotificacionTorneo> listar(@PathVariable Long torneoId) { return service.listar(torneoId); }
}
