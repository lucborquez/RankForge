package com.esports.api_gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizadorAuthorizationFilterTest {
    GatewayJwtUtil jwt;
    OrganizadorAuthorizationFilter filtro;
    FilterChain chain;
    @BeforeEach void preparar() {
        jwt = mock(GatewayJwtUtil.class);
        filtro = new OrganizadorAuthorizationFilter(jwt);
        chain = mock(FilterChain.class);
    }
    @Test void rechazaEscrituraSinTokenEnCadaRecurso() throws Exception {
        for (String path : new String[]{"/api/partidas", "/api/resultados/1/validar", "/api/sanciones/2/cerrar", "/api/rankings/2", "/api/inscripciones/3/estado"}) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filtro.doFilter(new MockHttpServletRequest("PATCH", path), response, chain);
            assertEquals(401, response.getStatus(), path);
        }
        verifyNoInteractions(chain);
    }
    @Test void rechazaTokenInvalido() throws Exception {
        when(jwt.validarToken("prueba")).thenThrow(new JwtException("firma"));
        MockHttpServletRequest request = peticion();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, chain);
        assertEquals(401, response.getStatus()); verifyNoInteractions(chain);
    }
    @Test void jugadorNoPuedeFalsificarRolPorHeader() throws Exception {
        claims("JUGADOR");
        MockHttpServletRequest request = peticion(); request.addHeader("X-Role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, chain);
        assertEquals(403, response.getStatus()); verifyNoInteractions(chain);
    }
    @Test void permiteOrganizadorYAdminValidados() throws Exception {
        for (String role : new String[]{"ORGANIZADOR", "ADMIN"}) {
            claims(role);
            MockHttpServletRequest request = peticion();
            MockHttpServletResponse response = new MockHttpServletResponse();
            filtro.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);
        }
    }
    @Test void conservaConsultaPublicaYPreflight() throws Exception {
        for (String method : new String[]{"GET", "HEAD", "OPTIONS"}) {
            MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/partidas");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filtro.doFilter(request, response, chain);
            verify(chain).doFilter(request, response);
        }
        verifyNoInteractions(jwt);
    }
    private void claims(String role) {
        Claims claims = mock(Claims.class); when(claims.get("rol", String.class)).thenReturn(role);
        when(jwt.validarToken("prueba")).thenReturn(claims);
    }
    private MockHttpServletRequest peticion() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/partidas");
        request.addHeader("Authorization", "Bearer prueba"); return request;
    }
}
