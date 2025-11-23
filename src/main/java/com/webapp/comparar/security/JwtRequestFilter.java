package com.webapp.comparar.security;

import com.webapp.comparar.service.JwtUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtRequestFilter(JwtUserDetailsService jwtUserDetailsService,
                            JwtTokenProvider jwtTokenProvider) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean shouldNotFilter = path.startsWith("/api/auth/") ||
                path.startsWith("/api/productos/") ||
                path.startsWith("/api/barrios/") ||
                path.startsWith("/api/comercios/") ||
                path.startsWith("/api/sucursales/") ||
                path.startsWith("/api/chat/") ||
                path.startsWith("/api/debug/") ||
                path.startsWith("/api/chatbot/");

        // ❌ COMENTA temporalmente este log
        // System.out.println("🔍 JwtRequestFilter - shouldNotFilter: " + shouldNotFilter + " for path: " + path);
        return shouldNotFilter;
    }



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // ✅ PARA SSE, BUSCAR TOKEN EN QUERY PARAMETER
        String jwt = null;
        String username = null;

        // Buscar en query parameter primero (para SSE)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            jwt = tokenParam;
            System.out.println("🔑 Token encontrado en query parameter");
        }
        // Buscar en header (para requests normales)
        else {
            final String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                System.out.println("🔑 Token encontrado en Authorization header");
            }
        }

        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            username = jwtTokenProvider.getUsernameFromJWT(jwt);
            System.out.println("✅ Token válido para: " + username);
        } else if (jwt != null) {
            System.out.println("❌ Token inválido");
        } else {
            System.out.println("ℹ️ Sin token presente");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);
    }
}