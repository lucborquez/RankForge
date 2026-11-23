package com.esports.tournament_service.controller;

import com.esports.tournament_service.dto.CambioEstadoDTO;
import com.esports.tournament_service.dto.TorneoRequestDTO;
import com.esports.tournament_service.dto.TorneoResponseDTO;
import com.esports.tournament_service.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("local-admin")
@RestController
@RequestMapping("/api/torneos")
public class TorneoAdminController {

    private final TorneoService torneoService;

    public TorneoAdminController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @PostMapping
    public ResponseEntity<TorneoResponseDTO> crear(@Valid @RequestBody TorneoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(torneoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TorneoResponseDTO> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody TorneoRequestDTO dto) {
        return ResponseEntity.ok(torneoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TorneoResponseDTO> cambiarEstado(@PathVariable Long id,
                                                          @Valid @RequestBody CambioEstadoDTO dto,
                                                          @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(torneoService.cambiarEstado(id, dto, authorization));
    }
}
