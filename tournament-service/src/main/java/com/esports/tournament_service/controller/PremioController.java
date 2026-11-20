package com.esports.tournament_service.controller;
import com.esports.tournament_service.model.Premio;
import com.esports.tournament_service.dto.PremioRequestDTO;
import com.esports.tournament_service.service.PremioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/torneos/{torneoId}/premios")
public class PremioController {
    private final PremioService service;
    public PremioController(PremioService service) { this.service = service; }
    @GetMapping public List<Premio> listar(@PathVariable Long torneoId) { return service.listar(torneoId); }
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Premio asignar(@PathVariable Long torneoId, @Valid @RequestBody PremioRequestDTO dto) { return service.asignar(torneoId, dto); }
}
