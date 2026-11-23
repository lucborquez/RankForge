package com.esports.auth_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Roles permitidos en el sistema
    private static final Set<String> ROLES_VALIDOS =
            Set.of("JUGADOR", "ORGANIZADOR", "ADMIN");

    // Obtener clave secreta desde la variable de entorno
    private Key getKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    // Generar token JWT
    public String generarToken(String email, String rol) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email invalido");
        }

        if (rol == null || !ROLES_VALIDOS.contains(rol)) {
            throw new IllegalArgumentException("Rol invalido");
        }

        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validar y obtener la informacion del token
    private Claims obtenerClaims(String token) {

        Jws<Claims> jwt = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);

        // Verificar algoritmo de firma
        if (!"HS256".equals(jwt.getHeader().getAlgorithm())) {
            throw new JwtException("Algoritmo no permitido");
        }

        Claims claims = jwt.getBody();

        // Verificar expiracion
        if (claims.getExpiration() == null ||
                claims.getExpiration().before(new Date())) {
            throw new JwtException("Token expirado");
        }

        // Verificar identidad
        if (claims.getSubject() == null ||
                claims.getSubject().isBlank()) {
            throw new JwtException("Email invalido");
        }

        // Verificar rol
        String rol = claims.get("rol", String.class);

        if (rol == null || !ROLES_VALIDOS.contains(rol)) {
            throw new JwtException("Rol no permitido");
        }

        return claims;
    }

    // Obtener email del usuario
    public String obtenerEmail(String token) {
        return obtenerClaims(token).getSubject();
    }

    // Obtener rol del usuario
    public String obtenerRol(String token) {
        return obtenerClaims(token).get("rol", String.class);
    }

    // Verificar si el token es valido
    public boolean esValido(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
