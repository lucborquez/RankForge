package com.esports.game_service;

import com.esports.game_service.security.JuegoAuthorizationFilter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class JuegoSecurityTest {
    byte[] key = "test-only-signing-key-for-results-not-production".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    JuegoAuthorizationFilter filter = new JuegoAuthorizationFilter(Base64.getEncoder().encodeToString(key));
    String token(String rol, long vigencia) {
        return Jwts.builder().setSubject("organizador@example.test").claim("rol", rol)
                .setExpiration(new Date(System.currentTimeMillis() + vigencia))
                .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256).compact();
    }
    int ejecutar(String token) throws Exception {
        var req = new MockHttpServletRequest("PATCH", "/api/juegos/1");
        req.addHeader("X-Role", "ADMIN");
        if (token != null) req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse(); filter.doFilter(req, res, new MockFilterChain()); return res.getStatus();
    }
    @Test void accesoDirectoSinTokenEs401() throws Exception { assertEquals(401, ejecutar(null)); }
    @Test void organizadorNoPuedeModificarCatalogo() throws Exception { assertEquals(403, ejecutar(token("ORGANIZADOR", 60000))); }
    @Test void jugadorNoPuedeValidarAunqueFalsifiqueHeader() throws Exception { assertEquals(403, ejecutar(token("JUGADOR", 60000))); }
    @Test void administradorConFirmaValidaPuedeModificar() throws Exception { assertEquals(200, ejecutar(token("ADMIN", 60000))); }
    @Test void tokenExpiradoEs401() throws Exception { assertEquals(401, ejecutar(token("ADMIN", -60000))); }
}

