package com.esports.sanction_service;
import com.esports.sanction_service.client.DestinatarioClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DestinatarioTest {
    RestTemplate http = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(http).build();
    DestinatarioClient client = new DestinatarioClient(http);
    DestinatarioTest() {
        ReflectionTestUtils.setField(client, "usuarios", "http://usuarios");
        ReflectionTestUtils.setField(client, "equipos", "http://equipos");
    }
    @Test void usuarioExistenteRecibeTokenDelOperador() {
        server.expect(requestTo("http://usuarios/api/usuarios/7/existe"))
            .andExpect(header("Authorization", "Bearer prueba"))
            .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));
        assertDoesNotThrow(() -> client.exigirExistente(7L, null, "Bearer prueba")); server.verify();
    }
    @Test void usuarioInexistenteEs404() {
        server.expect(anything()).andRespond(withSuccess("false", MediaType.APPLICATION_JSON));
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> client.exigirExistente(7L, null, "Bearer prueba")).getStatusCode().value());
    }
    @Test void equipoNoNecesitaEstarActivoParaExistir() {
        server.expect(requestTo("http://equipos/api/equipos/8"))
            .andRespond(withSuccess("{\"id\":8,\"estado\":false}", MediaType.APPLICATION_JSON));
        assertDoesNotThrow(() -> client.exigirExistente(null, 8L, "Bearer prueba")); server.verify();
    }
    @Test void equipoInexistenteEs404() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> client.exigirExistente(null, 8L, "Bearer prueba")).getStatusCode().value());
    }
    @Test void fuenteNoDisponibleEs503() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.exigirExistente(7L, null, "Bearer prueba")).getStatusCode().value());
    }
    @Test void controladorNoCreaSiNoPuedeComprobarDestinatario() {
        var servicio = mock(com.esports.sanction_service.service.SancionService.class);
        var destinatarios = mock(DestinatarioClient.class);
        var controlador = new com.esports.sanction_service.controller.SancionController(servicio, destinatarios);
        var dto = new com.esports.sanction_service.dto.SancionRequestDTO(); dto.setUsuarioId(7L);
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)).when(destinatarios).exigirExistente(7L,null,"Bearer prueba");
        assertThrows(ResponseStatusException.class, () -> controlador.crear(dto,"Bearer prueba")); verifyNoInteractions(servicio);
    }
}
