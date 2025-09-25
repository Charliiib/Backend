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
        com.webapp.comparar.security.UserPrincipal userPrincipal = (com.webapp.comparar.security.UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Usar email como subject
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact();
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // Esto debería devolver el email del usuario
        } catch (Exception e) {
            throw new RuntimeException("Error al extraer username del token", e);
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