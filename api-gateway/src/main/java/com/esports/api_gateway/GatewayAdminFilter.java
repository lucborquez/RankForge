package com.esports.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Component
public class GatewayAdminFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_LECTURA =
            Set.of("GET", "HEAD", "OPTIONS");

    private static final PathPattern JUEGOS =
            PathPatternParser.defaultInstance.parse("/api/juegos/**");

    private static final PathPattern TORNEOS =
            PathPatternParser.defaultInstance.parse("/api/torneos/**");

    private static final PathPattern CATALOGO_ADMIN =
            PathPatternParser.defaultInstance.parse("/api/juegos/admin");

    private final GatewayJwtUtil jwtUtil;

    public GatewayAdminFilter(GatewayJwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        var ruta = RequestPath.parse(
                request.getRequestURI(),
                request.getContextPath()
        ).pathWithinApplication();

        boolean escrituraCatalogo =
                !METODOS_LECTURA.contains(request.getMethod()) &&
                (JUEGOS.matches(ruta) || TORNEOS.matches(ruta));

        return !escrituraCatalogo && !CATALOGO_ADMIN.matches(ruta);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        // Verificar que exista un token
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            responderError(response, 401, "Autenticacion requerida");
            return;
        }

        String token = authorization.substring(7);

        Claims claims;

        // Validar token JWT
        try {
            claims = jwtUtil.validarToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            responderError(response, 401, "Token invalido o expirado");
            return;
        }

        String rol = claims.get("rol", String.class);

        // Verificar permisos de administrador
        if (!"ADMIN".equals(rol)) {
            responderError(response, 403, "Permisos insuficientes");
            return;
        }

        chain.doFilter(request, response);
    }

    private void responderError(
            HttpServletResponse response,
            int status,
            String mensaje) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\":\"" + mensaje + "\"}"
        );
    }
}
