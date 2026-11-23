package com.esports.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

/** Autorización de operaciones del organizador en el punto de entrada HTTP. */
@Component
public class OrganizadorAuthorizationFilter extends OncePerRequestFilter {
    private static final Set<String> LECTURA = Set.of("GET", "HEAD", "OPTIONS");
    private final GatewayJwtUtil jwt;

    public OrganizadorAuthorizationFilter(GatewayJwtUtil jwt) { this.jwt = jwt; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (LECTURA.contains(request.getMethod())) return true;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !(recurso(path, "partidas") || recurso(path, "resultados")
            || recurso(path, "sanciones") || recurso(path, "rankings")
            || path.matches("/api/inscripciones/[0-9]+/estado/?"));
    }

    private static boolean recurso(String path, String nombre) {
        String base = "/api/" + nombre;
        return path.equals(base) || path.startsWith(base + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            rechazar(response, 401, "Autenticación requerida");
            return;
        }
        Claims claims;
        try {
            claims = jwt.validarToken(authorization.substring(7));
        } catch (JwtException | IllegalArgumentException ex) {
            rechazar(response, 401, "Token inválido o expirado");
            return;
        }
        String rol = claims.get("rol", String.class);
        if (!"ADMIN".equals(rol) && !"ORGANIZADOR".equals(rol)) {
            rechazar(response, 403, "La operación requiere organizador o administrador");
            return;
        }
        chain.doFilter(request, response);
    }

    private static void rechazar(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
