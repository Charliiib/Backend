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
        String method = request.getMethod();

        // 🔥 BLOQUE NUCLEAR: IGNORAR COMPLETAMENTE TODO EL CHATBOT
        System.out.println("🚨 JWT FILTER CHECK: " + method + " " + path);

        if (path.contains("/api/chatbot") || path.startsWith("/api/chatbot")) {
            System.out.println("🛑 JWT FILTER BYPASSED - Chatbot route detected!");
            return true; // COMPLETAMENTE IGNORAR
        }

        // Otras rutas públicas
        boolean shouldSkip = path.startsWith("/api/auth/")
                || path.startsWith("/api/productos/")
                || path.startsWith("/api/barrios/")
                || path.startsWith("/api/comercios/")
                || path.startsWith("/api/sucursales/")
                || path.startsWith("/api/chat/")
                || path.startsWith("/api/debug/");

        if (shouldSkip) {
            System.out.println("🛑 JWT FILTER BYPASSED - Public route: " + path);
        } else {
            System.out.println("✅ JWT FILTER WILL APPLY - Private route: " + path);
        }

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 🔍 VERIFICACIÓN ADICIONAL
        String path = request.getRequestURI();
        if (path.contains("/api/chatbot") || path.startsWith("/api/chatbot")) {
            System.out.println("🚨 URGENT: Chatbot route leaked through filter!");
            // Force bypass even if previous check failed
            chain.doFilter(request, response);
            return;
        }

        // ✅ Este método SOLO para rutas PRIVADAS
        System.out.println("🔐 JWT FILTER EXECUTING for: " + path);

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
                // No lanzar excepción, simplemente continuar sin auth
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("🔐 Authentication set for user: " + username);
            } catch (Exception e) {
                System.out.println("❌ Failed to load user details: " + e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}