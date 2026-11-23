package com.esports.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;

@Component
public class GatewayJwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private static final Set<String> ROLES_VALIDOS =
            Set.of("ADMIN", "ORGANIZADOR", "JUGADOR");

    private Key getKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public Claims validarToken(String token) {

        Jws<Claims> jwt = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);

        if (!"HS256".equals(jwt.getHeader().getAlgorithm())) {
            throw new JwtException("Algoritmo no permitido");
        }

        Claims claims = jwt.getBody();

        if (claims.getExpiration() == null ||
                claims.getExpiration().before(new Date())) {
            throw new JwtException("Token expirado");
        }

        if (claims.getSubject() == null ||
                claims.getSubject().isBlank()) {
            throw new JwtException("Email invalido");
        }

        String rol = claims.get("rol", String.class);

        if (rol == null || !ROLES_VALIDOS.contains(rol)) {
            throw new JwtException("Rol invalido");
        }

        return claims;
    }
}
