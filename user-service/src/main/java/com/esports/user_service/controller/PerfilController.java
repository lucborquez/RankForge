package com.esports.user_service.controller;

import com.esports.user_service.dto.*;
import com.esports.user_service.security.UsuarioJwtFilter;
import com.esports.user_service.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/me")
public class PerfilController {
    private final UsuarioService usuarios;
    public PerfilController(UsuarioService usuarios) { this.usuarios = usuarios; }
    @PostMapping public UsuarioResponseDTO asegurar(
            @RequestAttribute(UsuarioJwtFilter.IDENTIDAD) UsuarioJwtFilter.Identidad identidad) {
        return conRolActual(usuarios.asegurarPerfil(identidad.email(), identidad.rol()), identidad);
    }
    @GetMapping public UsuarioResponseDTO consultar(
            @RequestAttribute(UsuarioJwtFilter.IDENTIDAD) UsuarioJwtFilter.Identidad identidad) {
        return conRolActual(usuarios.perfilPorEmail(identidad.email()), identidad);
    }
    @PutMapping public UsuarioResponseDTO actualizar(
            @RequestAttribute(UsuarioJwtFilter.IDENTIDAD) UsuarioJwtFilter.Identidad identidad,
            @Valid @RequestBody PerfilRequestDTO dto) {
        return conRolActual(usuarios.actualizarPerfil(identidad.email(), dto), identidad);
    }
    private UsuarioResponseDTO conRolActual(UsuarioResponseDTO perfil, UsuarioJwtFilter.Identidad identidad) {
        perfil.setRol(identidad.rol());
        return perfil;
    }
}
