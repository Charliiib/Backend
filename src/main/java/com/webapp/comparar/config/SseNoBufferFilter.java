package com.webapp.comparar.config; // Asegúrate de usar el paquete correcto

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class SseNoBufferFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // :bulb: Aplicar solo a la ruta de streaming (opcional pero recomendado)
        if (request instanceof jakarta.servlet.http.HttpServletRequest &&
                ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI().contains("/consulta-stream")) {

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // :key: Cabecera CRÍTICA para desactivar el buffering en Nginx/Proxies
            httpResponse.setHeader("X-Accel-Buffering", "no");

            // También útil forzar Content-Type para asegurar el SSE
            httpResponse.setContentType("text/event-stream");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Limpieza
    }
}