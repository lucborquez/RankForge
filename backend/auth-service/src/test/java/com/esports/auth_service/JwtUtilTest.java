package com.esports.auth_service;

import com.esports.auth_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void configurar() {
        jwtUtil = new JwtUtil();

        String secret = Base64.getEncoder()
                .encodeToString(new byte[32]);

        ReflectionTestUtils.setField(
                jwtUtil, "secret", secret);

        ReflectionTestUtils.setField(
                jwtUtil, "expiration", 3600000L);
    }

    // Prueba de control: token valido
    @Test
    void tokenValido() {
        String token = jwtUtil.generarToken(
                "prueba@rankforge.cl", "JUGADOR");

        assertTrue(jwtUtil.esValido(token));
    }

    // Prueba 8: token manipulado
    @Test
    void tokenManipulado() {
        String token = jwtUtil.generarToken(
                "prueba@rankforge.cl", "JUGADOR");

        String[] partes = token.split("\\.");

        String firma = partes[2];

        String firmaModificada =
                (firma.charAt(0) == 'A' ? "B" : "A")
                + firma.substring(1);

        String tokenManipulado = partes[0] + "."
                + partes[1] + "." + firmaModificada;

        assertFalse(jwtUtil.esValido(tokenManipulado));
    }

    // Prueba 9: token expirado
    @Test
    void tokenExpirado() {
        ReflectionTestUtils.setField(
                jwtUtil, "expiration", -60000L);

        String token = jwtUtil.generarToken(
                "prueba@rankforge.cl", "JUGADOR");

        assertFalse(jwtUtil.esValido(token));
    }
}