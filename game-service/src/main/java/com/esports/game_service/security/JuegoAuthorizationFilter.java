package com.esports.game_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

@Component
public class JuegoAuthorizationFilter extends OncePerRequestFilter {
    private final String secret;
    public JuegoAuthorizationFilter(@Value("${jwt.secret}") String secret) { this.secret = secret; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
            FilterChain chain) throws ServletException, IOException {
        if (!req.getRequestURI().startsWith("/api/juegos") || Set.of("GET", "HEAD", "OPTIONS").contains(req.getMethod())) {
            chain.doFilter(req, res); return;
        }
        String rol;
        try {
            String header = req.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) throw new JwtException("Falta token");
            var jwt = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                    .build().parseClaimsJws(header.substring(7));
            var claims = jwt.getBody();
            rol = claims.get("rol", String.class);
            if (!"HS256".equals(jwt.getHeader().getAlgorithm()) || claims.getExpiration() == null
                    || !claims.getExpiration().after(new Date()) || claims.getSubject() == null
                    || claims.getSubject().isBlank() || rol == null
                    || !Set.of("ADMIN", "ORGANIZADOR", "JUGADOR").contains(rol)) throw new JwtException("Token inválido");
        } catch (RuntimeException ex) { rechazar(res, 401); return; }
        if (!"ADMIN".equals(rol)) { rechazar(res, 403); return; }
        chain.doFilter(req, res);
    }
    private void rechazar(HttpServletResponse res, int status) throws IOException {
        res.setStatus(status); res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + (status == 401 ? "Autenticación requerida" : "Permisos insuficientes") + "\"}");
    }
}

