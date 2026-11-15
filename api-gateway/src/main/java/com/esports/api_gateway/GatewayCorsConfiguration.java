package com.esports.api_gateway;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration(proxyBeanMethods = false)
public class GatewayCorsConfiguration {

    private static final List<String> FRONTEND_LOCAL = List.of("http://localhost:5173", "http://127.0.0.1:5173");

    static boolean esFrontendLocal(String origin) {
        return origin != null && FRONTEND_LOCAL.contains(origin);
    }

    @Bean
    CorsFilter apiCorsFilter(@Value("${rankforge.frontend-origins}") List<String> frontendOrigins,
                             Environment environment) {
        var lectura = rutasCors(frontendOrigins, List.of("GET", "HEAD"));
        if (!environment.matchesProfiles("local-admin")) {
            return new CorsFilter(lectura);
        }

        var administracion = rutasCors(FRONTEND_LOCAL, List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE"));
        return new CorsFilter(request -> {
            String origin = request.getHeader(HttpHeaders.ORIGIN);
            var configuracion = esFrontendLocal(origin) ? administracion : lectura;
            return configuracion.getCorsConfiguration(request);
        });
    }

    private UrlBasedCorsConfigurationSource rutasCors(List<String> origenes, List<String> metodos) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(metodos);
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/juegos", cors);
        source.registerCorsConfiguration("/api/juegos/*", cors);
        source.registerCorsConfiguration("/api/torneos", cors);
        source.registerCorsConfiguration("/api/torneos/*", cors);
        source.registerCorsConfiguration("/api/torneos/*/estado", cors);
        return source;
    }
}
