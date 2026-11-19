package com.esports.resultservice;

import com.esports.resultservice.security.ResultadoAuthorizationFilter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ResultadoSecurityTest {
    byte[] key = "test-only-signing-key-for-results-not-production".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    ResultadoAuthorizationFilter filter = new ResultadoAuthorizationFilter(Base64.getEncoder().encodeToString(key));
    String token(String rol, long vigencia) {
        return Jwts.builder().setSubject("organizador@example.test").claim("rol", rol)
                .setExpiration(new Date(System.currentTimeMillis() + vigencia))
                .signWith(Keys.hmacShaKeyFor(key), SignatureAlgorithm.HS256).compact();
    }
    int ejecutar(String token) throws Exception {
        var req = new MockHttpServletRequest("PATCH", "/api/resultados/1/validar");
        req.addHeader("X-Role", "ADMIN");
        if (token != null) req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse(); filter.doFilter(req, res, new MockFilterChain()); return res.getStatus();
    }
    @Test void accesoDirectoSinTokenEs401() throws Exception { assertEquals(401, ejecutar(null)); }
    @Test void jugadorNoPuedeValidarAunqueFalsifiqueHeader() throws Exception { assertEquals(403, ejecutar(token("JUGADOR", 60000))); }
    @Test void organizadorConFirmaValidaPuedeValidar() throws Exception { assertEquals(200, ejecutar(token("ORGANIZADOR", 60000))); }
    @Test void tokenExpiradoEs401() throws Exception { assertEquals(401, ejecutar(token("ADMIN", -60000))); }
}
