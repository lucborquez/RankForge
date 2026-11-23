package com.esports.api_gateway;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration(proxyBeanMethods = false)
public class GatewayCorsConfiguration {

    @Bean
    FilterRegistrationBean<CorsFilter> apiCorsFilter(
            @Value("${rankforge.frontend-origins}")
            List<String> frontendOrigins) {

        CorsConfiguration cors = new CorsConfiguration();

        // Origenes permitidos
        cors.setAllowedOrigins(frontendOrigins);

        // Metodos HTTP permitidos
        cors.setAllowedMethods(List.of(
                "GET",
                "HEAD",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Encabezados permitidos
        cors.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", cors);

        FilterRegistrationBean<CorsFilter> registro =
                new FilterRegistrationBean<>(new CorsFilter(source));

        // Ejecutar CORS antes del filtro de seguridad JWT
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registro;
    }
}
