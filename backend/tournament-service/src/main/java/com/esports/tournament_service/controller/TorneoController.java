package com.esports.tournament_service.controller;

import com.esports.tournament_service.dto.*;
import com.esports.tournament_service.service.TorneoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/torneos")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    // GET /api/torneos
    // GET /api/torneos?estado=ABIERTO
    // GET /api/torneos?juegoId=1
    // GET /api/torneos?juegoId=1&estado=ABIERTO
    @GetMapping
    public ResponseEntity<List<TorneoResponseDTO>> listar(
            @RequestParam(required = false) Long juegoId,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(torneoService.listar(juegoId, estado));
    }

    // GET /api/torneos/1
    @GetMapping("/{id}")
    public ResponseEntity<TorneoResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.buscarPorId(id));
    }

    // GET /api/torneos/1/abierto → Para consultas de otros servicios
    @GetMapping("/{id}/abierto")
    public ResponseEntity<Boolean> verificarAbierto(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.torneoEstaAbierto(id));
    }

    // GET /api/torneos/1/inscripcion → Para que registration-service valide
    @GetMapping("/{id}/inscripcion")
    public ResponseEntity<TorneoResponseDTO> obtenerParaInscripcion(
            @PathVariable Long id) {
        return ResponseEntity.ok(torneoService.obtenerParaInscripcion(id));
    }
}
