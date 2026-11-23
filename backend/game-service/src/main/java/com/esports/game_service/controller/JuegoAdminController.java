package com.esports.game_service.controller;

import com.esports.game_service.dto.JuegoRequestDTO;
import com.esports.game_service.dto.JuegoResponseDTO;
import com.esports.game_service.service.JuegoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("local-admin")
@RestController
@RequestMapping("/api/juegos")
public class JuegoAdminController {

    private final JuegoService juegoService;

    public JuegoAdminController(JuegoService juegoService) {
        this.juegoService = juegoService;
    }

    @GetMapping("/admin")
    public List<JuegoResponseDTO> listarTodos() {
        return juegoService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<JuegoResponseDTO> crear(@Valid @RequestBody JuegoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(juegoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JuegoResponseDTO> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody JuegoRequestDTO dto) {
        return ResponseEntity.ok(juegoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        juegoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
