package com.esports.user_service.service;


import com.esports.user_service.dto.UsuarioRequestDTO;
import com.esports.user_service.dto.UsuarioResponseDTO;
import com.esports.user_service.exception.UsuarioNotFoundException;
import com.esports.user_service.model.Usuario;
import com.esports.user_service.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponseDTO asegurarPerfil(String email, String rol) {
        var existente = usuarioRepository.findByEmail(email);
        if (existente.isPresent()) return convertirAResponse(perfilActivo(existente.get()));
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setNombre(email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length()));
        usuario.setApodo("jugador-" + java.util.UUID.randomUUID().toString().substring(0, 24));
        usuario.setFechaRegistro(LocalDate.now());
        try {
            return convertirAResponse(usuarioRepository.saveAndFlush(usuario));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Dos pestañas pueden solicitar simultáneamente la creación del mismo perfil.
            return usuarioRepository.findByEmail(email).map(this::perfilActivo).map(this::convertirAResponse)
                    .orElseThrow(() -> ex);
        }
    }

    public UsuarioResponseDTO perfilPorEmail(String email) {
        return convertirAResponse(obtenerPerfil(email));
    }

    public UsuarioResponseDTO actualizarPerfil(String email, com.esports.user_service.dto.PerfilRequestDTO dto) {
        Usuario usuario = obtenerPerfil(email);
        String apodo = dto.apodo().trim();
        if (apodo.length() < 3 || dto.nombre().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Nombre o apodo inválido");
        }
        usuarioRepository.findByApodo(apodo).filter(otro -> !otro.getId().equals(usuario.getId())).ifPresent(otro -> {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "El apodo ya está en uso");
        });
        usuario.setNombre(dto.nombre().trim());
        usuario.setApodo(apodo);
        return convertirAResponse(usuarioRepository.save(usuario));
    }

    private Usuario obtenerPerfil(String email) {
        return perfilActivo(usuarioRepository.findByEmail(email).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Perfil no creado")));
    }

    private Usuario perfilActivo(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getEstado())) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Perfil desactivado");
        return usuario;
    }

    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        usuarioRepository.findByApodo(dto.getApodo()).ifPresent(u ->{
            throw new RuntimeException("Ya existe un usuario con el Apodo: " + dto.getApodo());
        });
        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(u ->{
            throw new RuntimeException("Ya existe un usuario con ese email: " + dto.getEmail());
        });

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApodo(dto.getApodo());
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());
        usuario.setFechaRegistro(LocalDate.now());

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado: {}  con ID {}", guardado.getApodo(), guardado.getId());
        return convertirAResponse(guardado);
    }

    public List<UsuarioResponseDTO> listarTodo() {
        return usuarioRepository.findAll().stream().map(this::convertirAResponse).collect(Collectors.toList());
    }

    public List<UsuarioResponseDTO> listarPorRol(String rol) {
        return usuarioRepository.findByRol(rol).stream().map(this::convertirAResponse).collect(Collectors.toList());
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        return convertirAResponse(usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id)));
    }

    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNotFoundException(id));

        usuario.setNombre(dto.getNombre());
        usuario.setApodo(dto.getApodo());
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: ID {}", actualizado.getId());
        return convertirAResponse(actualizado);
    }

    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNotFoundException(id));
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
        log.info("Usuario desactivado: ID {}", usuario.getId());
    }

    public Boolean existeYActivo(Long id) {
        return usuarioRepository.findById(id).map(u -> u.getEstado()).orElse(false);
    }

    public boolean existe(Long id) { return usuarioRepository.existsById(id); }

    private UsuarioResponseDTO convertirAResponse(Usuario u) {
        UsuarioResponseDTO r = new UsuarioResponseDTO();
        r.setId(u.getId());
        r.setNombre(u.getNombre());
        r.setApodo(u.getApodo());
        r.setEmail(u.getEmail());
        r.setRol(u.getRol());
        r.setEstado(u.getEstado());
        r.setFechaRegistro(u.getFechaRegistro());
        return r;
    }
}
