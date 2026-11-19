package com.esports.registration_service.controller;

import com.esports.registration_service.dto.InscripcionRequestDTO;
import com.esports.registration_service.dto.InscripcionResponseDTO;
import com.esports.registration_service.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final com.esports.registration_service.client.UsuarioClient usuarios;
    private final com.esports.registration_service.client.EquipoClient equipos;

    public InscripcionController(InscripcionService inscripcionService,
            com.esports.registration_service.client.UsuarioClient usuarios,
            com.esports.registration_service.client.EquipoClient equipos) {
        this.inscripcionService = inscripcionService;
        this.usuarios = usuarios;
        this.equipos = equipos;
    }

    // POST → crear inscripcion
    @PostMapping
    public ResponseEntity<InscripcionResponseDTO> crear(
            @Valid @RequestBody InscripcionRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        var actor = usuarios.autenticar(authorization);
        if ("JUGADOR".equals(dto.getTipoParticipante())) {
            dto.setJugadorId(actor.id());
        } else if ("EQUIPO".equals(dto.getTipoParticipante())) {
            exigirCapitan(dto.getEquipoId(), actor);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscripcionService.crear(dto));
    }

    // GET → listar todas
    @GetMapping
    public ResponseEntity<List<InscripcionResponseDTO>> listar(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        exigirOrganizador(usuarios.autenticar(authorization));
        return ResponseEntity.ok(inscripcionService.listarTodas());
    }

    // GET → buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<InscripcionResponseDTO> buscar(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        var actor = usuarios.autenticar(authorization);
        var inscripcion = inscripcionService.buscarPorId(id);
        exigirPropietario(inscripcion, actor);
        return ResponseEntity.ok(inscripcion);
    }

    // GET → listar por torneo
    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<InscripcionResponseDTO>> porTorneo(
            @PathVariable Long torneoId) {
        return ResponseEntity.ok(inscripcionService.listarPorTorneo(torneoId));
    }

    // GET → listar por jugador
    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<List<InscripcionResponseDTO>> porJugador(
            @PathVariable Long jugadorId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        var actor = usuarios.autenticar(authorization);
        if (!actor.id().equals(jugadorId) && !organizador(actor)) prohibido();
        return ResponseEntity.ok(inscripcionService.listarPorJugador(jugadorId));
    }

    // PUT → actualizar estado
    @PutMapping("/{id}/estado")
    public ResponseEntity<InscripcionResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        exigirOrganizador(usuarios.autenticar(authorization));
        return ResponseEntity.ok(inscripcionService.actualizarEstado(id, estado));
    }

    // DELETE → cancelar
    @DeleteMapping("/{id}")
    public ResponseEntity<InscripcionResponseDTO> cancelar(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        var actor = usuarios.autenticar(authorization);
        exigirPropietario(inscripcionService.buscarPorId(id), actor);
        return ResponseEntity.ok(inscripcionService.cancelar(id));
    }

    private boolean organizador(com.esports.registration_service.client.UsuarioClient.Actor actor) {
        return actor.administrador() || "ORGANIZADOR".equals(actor.rol());
    }
    private void exigirOrganizador(com.esports.registration_service.client.UsuarioClient.Actor actor) {
        if (!organizador(actor)) prohibido();
    }
    private void exigirCapitan(Long equipoId, com.esports.registration_service.client.UsuarioClient.Actor actor) {
        if (equipoId == null || equipoId <= 0) throw new IllegalArgumentException("Indica un equipo válido");
        var equipo = equipos.obtener(equipoId);
        if (!actor.administrador() && !actor.id().equals(equipo.capitanId())) prohibido();
    }
    private void exigirPropietario(InscripcionResponseDTO inscripcion,
            com.esports.registration_service.client.UsuarioClient.Actor actor) {
        if (organizador(actor)) return;
        if ("EQUIPO".equals(inscripcion.getTipoParticipante())) exigirCapitan(inscripcion.getEquipoId(), actor);
        else if (!actor.id().equals(inscripcion.getJugadorId())) prohibido();
    }
    private void prohibido() {
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta inscripción");
    }
}
