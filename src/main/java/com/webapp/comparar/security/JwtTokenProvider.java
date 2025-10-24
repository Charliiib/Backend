package com.webapp.comparar.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecret;
    private final int jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${app.jwtSecret}") String secret,
            @Value("${app.jwtExpirationInMs}") int expiration) {

        // Convierte la clave a formato seguro
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationInMs = expiration;
    }
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId()) // Ahora es Integer
                .claim("nombre", userPrincipal.getNombre())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact();
    }


    public String getUsernameFromJWT(String token) {
        try {
            Claims claims = Jwts.parserBuilder() // Usar parserBuilder para consistencia
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            System.err.println("Error extrayendo username del token: " + e.getMessage());
            return null;
        }
    }

    public Long getUserIdFromJWT(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Asegurarnos de que el claim "userId" existe y es Long
            Object userIdClaim = claims.get("userId");
            if (userIdClaim instanceof Integer) {
                return ((Integer) userIdClaim).longValue();
            } else if (userIdClaim instanceof Long) {
                return (Long) userIdClaim;
            } else {
                System.err.println("Tipo de userId inesperado: " + userIdClaim.getClass());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo userId del token: " + e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // log
        } catch (MalformedJwtException ex) {
            // log
        } catch (ExpiredJwtException ex) {
            // log
        } catch (UnsupportedJwtException ex) {
            // log
        } catch (IllegalArgumentException ex) {
            // log
        }
        return false;
    }




}