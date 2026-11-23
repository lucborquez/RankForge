package com.esports.auth_service.config;

import com.esports.auth_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Verificar si existe un token JWT
        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            // Validar el token
            if (!jwtUtil.esValido(token)) {
                SecurityContextHolder.clearContext();

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);

                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Token invalido o expirado\"}");

                return;
            }

            // Obtener la informacion del usuario
            String email = jwtUtil.obtenerEmail(token);
            String rol = jwtUtil.obtenerRol(token);

            // Asignar permisos segun el rol
            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + rol));

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            email, null, authorities);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}