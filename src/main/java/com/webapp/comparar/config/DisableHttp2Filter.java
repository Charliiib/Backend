package com.webapp.comparar.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class DisableHttp2Filter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Sólo para SSE
        if (request.getRequestURI().startsWith("/api/chatbot/consulta-stream")) {

            // Fuerza Railway a usar HTTP/1.1 sin cerrar la conexión
            response.setHeader("X-Railway-Force-HTTP11", "true");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("X-Accel-Buffering", "no");

            // Deshabilita buffering (muy importante)
            response.setHeader("Transfer-Encoding", "chunked");
        }

        filterChain.doFilter(request, response);
    }
}
