package com.esports.user_service;

import com.esports.user_service.security.UsuarioJwtFilter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PerfilSecurityTest {
    private final byte[] key = "test-only-key-32-bytes-minimum-never-production".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private final UsuarioJwtFilter filter = new UsuarioJwtFilter(Base64.getEncoder().encodeToString(key));
    private String token(String rol) {
        return Jwts.builder().setSubject("lucas@example.test").claim("rol", rol)
            .setExpiration(new Date(System.currentTimeMillis() + 60000))
            .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256).compact();
    }
    @Test void requiereTokenAunqueFalsifiqueIdentidad() throws Exception {
        var req = new MockHttpServletRequest("POST", "/api/usuarios/me");
        req.addHeader("X-User-Email", "admin@example.test");
        var res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        assertEquals(401, res.getStatus());
    }
    @Test void perfilUsaSubjectFirmado() throws Exception {
        var req = new MockHttpServletRequest("PUT", "/api/usuarios/me");
        req.addHeader("Authorization", "Bearer " + token("JUGADOR"));
        req.addHeader("X-User-Email", "otra@example.test");
        var res = new MockHttpServletResponse(); var chain = new MockFilterChain();
        filter.doFilter(req, res, chain);
        assertNotNull(chain.getRequest());
        var identidad = (UsuarioJwtFilter.Identidad) req.getAttribute(UsuarioJwtFilter.IDENTIDAD);
        assertEquals("lucas@example.test", identidad.email());
    }
    @Test void jugadorNoPuedeEditarOtroPerfilNiEnumerarUsuarios() throws Exception {
        for (String path : List.of("/api/usuarios/7", "/api/usuarios")) {
            var req = new MockHttpServletRequest("PUT", path);
            req.addHeader("Authorization", "Bearer " + token("JUGADOR"));
            var res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertEquals(403, res.getStatus());
        }
    }
    @Test void rechazaFirmaAlterada() throws Exception {
        var req = new MockHttpServletRequest("POST", "/api/usuarios/me");
        String firmado = token("ADMIN"); String[] partes = firmado.split("\\.");
        String firma = (partes[2].charAt(0) == 'a' ? "b" : "a") + partes[2].substring(1);
        req.addHeader("Authorization", "Bearer " + partes[0] + "." + partes[1] + "." + firma);
        var res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        assertEquals(401, res.getStatus());
    }
    @Test void existenciaSoloParaRolesDeOperacion() throws Exception {
        for (String rol : List.of("ADMIN", "ORGANIZADOR", "JUGADOR")) {
            var req = new MockHttpServletRequest("GET", "/api/usuarios/7/existe");
            req.addHeader("Authorization", "Bearer " + token(rol));
            var res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertEquals(rol.equals("JUGADOR") ? 403 : 200, res.getStatus());
        }
    }
    @Test void organizadorNoObtienePerfilNiModificaExistencia() throws Exception {
        for (String path : List.of("/api/usuarios/7", "/api/usuarios/7/existe")) {
            var req = new MockHttpServletRequest("PUT", path);
            req.addHeader("Authorization", "Bearer " + token("ORGANIZADOR"));
            var res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertEquals(403, res.getStatus());
        }
    }
}
