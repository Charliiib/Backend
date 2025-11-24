package com.webapp.comparar.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.webapp.comparar.service.JwtUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

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

        // ✅ IGNORAR COMPLETAMENTE TODAS LAS RUTAS DE CHATBOT
        if (path.startsWith("/api/chatbot")) {
            System.out.println("🔒 JWT FILTER SKIPPED - Chatbot route: " + path);
            return true; // COMPLETAMENTE IGNORAR
        }

        // Otras rutas públicas
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/productos/")
                || path.startsWith("/api/barrios/")
                || path.startsWith("/api/comercios/")
                || path.startsWith("/api/sucursales/")
                || path.startsWith("/api/chat/")
                || path.startsWith("/api/debug/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 🔍 Este método SOLO se ejecuta para rutas PRIVADAS
        System.out.println("🔍 JWT FILTER APPLIED to: " + request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                if (jwtTokenProvider.validateToken(jwt)) {
                    username = jwtTokenProvider.getUsernameFromJWT(jwt);
                    System.out.println("✅ JWT validated for user: " + username);
                }
            } catch (Exception e) {
                System.out.println("❌ JWT validation failed: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            System.out.println("🔐 Authentication set for user: " + username);
        }

        chain.doFilter(request, response);
    }
}