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
        System.out.println("🔍 shouldNotFilter path = " + path);

        // ✅ RUTAS PÚBLICAS QUE NO REQUIEREN JWT
        boolean isPublicRoute = path.startsWith("/api/chatbot")
                || path.startsWith("/api/auth/")
                || path.startsWith("/api/productos/")
                || path.startsWith("/api/barrios/")
                || path.startsWith("/api/comercios/")
                || path.startsWith("/api/sucursales/")
                || path.startsWith("/api/chat/")
                || path.startsWith("/api/debug/");

        if (isPublicRoute) {
            System.out.println("🟢 IGNORANDO JWT para ruta pública: " + path);
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // ✅ Este método solo se ejecuta si shouldNotFilter() devuelve false
        System.out.println("🔍 JwtRequestFilter PROCESANDO AUTH para: " + request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            if (jwtTokenProvider.validateToken(jwt)) {
                username = jwtTokenProvider.getUsernameFromJWT(jwt);
            }
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