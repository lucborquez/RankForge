package com.esports.game_service.controller;


import com.esports.game_service.dto.JuegoResponseDTO;
import com.esports.game_service.service.JuegoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Aqui se reciben las peticiones  HTTP
@RestController
@RequestMapping("/api/juegos")
public class JuegoController {

    private final JuegoService juegoService;

    public JuegoController(JuegoService juegoService) {
        this.juegoService = juegoService;
    }

    @GetMapping
    public ResponseEntity<List<JuegoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(juegoService.listarActivos());
    }

    //GET -> buscar por ID
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<JuegoResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(juegoService.buscarPorId(id));
    }

}
