package com.webapp.comparar.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DisableHttp2Filter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResp = (HttpServletResponse) response;

        // Fuerza compatibilidad con HTTP/1.1 en Railway
        httpResp.setHeader("Connection", "keep-alive");
        httpResp.setHeader("X-Railway-Force-HTTP11", "true");

        chain.doFilter(request, response);
    }
}
