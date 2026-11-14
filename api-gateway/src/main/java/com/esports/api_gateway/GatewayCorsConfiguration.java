package com.esports.api_gateway;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration(proxyBeanMethods = false)
public class GatewayCorsConfiguration {

    @Bean
    CorsFilter apiCorsFilter(@Value("${rankforge.frontend-origins}") List<String> frontendOrigins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(frontendOrigins);
        cors.setAllowedMethods(List.of("GET", "HEAD"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/juegos", cors);
        source.registerCorsConfiguration("/api/juegos/*", cors);
        source.registerCorsConfiguration("/api/torneos", cors);
        source.registerCorsConfiguration("/api/torneos/*", cors);
        return new CorsFilter(source);
    }
}
