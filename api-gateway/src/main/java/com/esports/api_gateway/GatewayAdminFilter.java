package com.esports.api_gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Component
public class GatewayAdminFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_LECTURA = Set.of("GET", "HEAD", "OPTIONS");
    private static final PathPattern JUEGOS = PathPatternParser.defaultInstance.parse("/api/juegos/**");
    private static final PathPattern TORNEOS = PathPatternParser.defaultInstance.parse("/api/torneos/**");
    private static final PathPattern CATALOGO_ADMIN = PathPatternParser.defaultInstance.parse("/api/juegos/admin");
    private final boolean habilitado;

    public GatewayAdminFilter(Environment environment) {
        this.habilitado = environment.matchesProfiles("local-admin");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var ruta = RequestPath.parse(request.getRequestURI(), request.getContextPath()).pathWithinApplication();
        boolean escrituraCatalogo = !METODOS_LECTURA.contains(request.getMethod()) &&
                (JUEGOS.matches(ruta) || TORNEOS.matches(ruta));
        return !escrituraCatalogo && !CATALOGO_ADMIN.matches(ruta);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Las peticiones directas también pasan por este control, aunque no tengan Origin.
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (!habilitado || !InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress() ||
                (origin != null && !GatewayCorsConfiguration.esFrontendLocal(origin))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
