package com.esports.user_service.security;

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

/** Verifica la firma también cuando se accede directamente al microservicio. */
@Component
public class UsuarioJwtFilter extends OncePerRequestFilter {
    public static final String IDENTIDAD = "rankforge.identidad.validada";
    public record Identidad(String email, String rol) {}
    private final String secret;
    public UsuarioJwtFilter(@Value("${jwt.secret}") String secret) { this.secret = secret; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // Consulta booleana utilizada por los demás servicios; no expone el perfil.
        if (!path.startsWith("/api/usuarios") || "OPTIONS".equals(request.getMethod())
                || ("GET".equals(request.getMethod()) && path.matches("/api/usuarios/[0-9]+/activo"))) {
            chain.doFilter(request, response); return;
        }
        Identidad identidad;
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) throw new JwtException("Token requerido");
            Jws<Claims> jwt = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                    .build().parseClaimsJws(header.substring(7));
            Claims claims = jwt.getBody();
            String rol = claims.get("rol", String.class);
            if (!"HS256".equals(jwt.getHeader().getAlgorithm()) || claims.getExpiration() == null
                    || !claims.getExpiration().after(new Date()) || claims.getSubject() == null
                    || claims.getSubject().isBlank() || rol == null
                    || !Set.of("ADMIN", "ORGANIZADOR", "JUGADOR").contains(rol)) throw new JwtException("Token inválido");
            identidad = new Identidad(claims.getSubject(), rol);
        } catch (RuntimeException ex) { error(response, 401); return; }
        boolean propio = path.equals("/api/usuarios/me") || path.equals("/api/usuarios/me/");
        if (!propio && !"ADMIN".equals(identidad.rol())) { error(response, 403); return; }
        request.setAttribute(IDENTIDAD, identidad);
        chain.doFilter(request, response);
    }
    private void error(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status); response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + (status == 401 ? "Autenticación requerida" : "Permisos insuficientes") + "\"}");
    }
}
