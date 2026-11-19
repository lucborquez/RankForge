package com.esports.user_service;

import com.esports.user_service.dto.PerfilRequestDTO;
import com.esports.user_service.model.Usuario;
import com.esports.user_service.repository.UsuarioRepository;
import com.esports.user_service.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilServiceTest {
    UsuarioRepository repository = mock(UsuarioRepository.class);
    UsuarioService service = new UsuarioService(repository);
    Usuario usuario() {
        Usuario u = new Usuario(); u.setId(5L); u.setEmail("lucas@example.test");
        u.setNombre("Lucas"); u.setApodo("lucas"); u.setRol("JUGADOR"); return u;
    }
    @Test void reutilizaPerfilExistenteSinDuplicarlo() {
        when(repository.findByEmail("lucas@example.test")).thenReturn(Optional.of(usuario()));
        assertEquals(5L, service.asegurarPerfil("lucas@example.test", "JUGADOR").getId());
        verify(repository, never()).saveAndFlush(any());
    }
    @Test void edicionConservaEmailRolYEstado() {
        Usuario u = usuario();
        when(repository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        var resultado = service.actualizarPerfil(u.getEmail(), new PerfilRequestDTO("Lucas B", "lucas-b"));
        assertEquals("lucas-b", resultado.getApodo());
        assertEquals("JUGADOR", resultado.getRol()); assertEquals("lucas@example.test", resultado.getEmail());
        assertTrue(u.getEstado());
    }
    @Test void rechazaPerfilDesactivado() {
        Usuario u = usuario(); u.setEstado(false);
        when(repository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        assertEquals(403, assertThrows(ResponseStatusException.class,
            () -> service.asegurarPerfil(u.getEmail(), "JUGADOR")).getStatusCode().value());
    }
    @Test void apodoDuplicadoNoGuarda() {
        Usuario u = usuario(); Usuario otro = usuario(); otro.setId(9L);
        when(repository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(repository.findByApodo("ocupado")).thenReturn(Optional.of(otro));
        assertEquals(409, assertThrows(ResponseStatusException.class,
            () -> service.actualizarPerfil(u.getEmail(), new PerfilRequestDTO("Lucas", "ocupado"))).getStatusCode().value());
        verify(repository, never()).save(any());
    }
}
