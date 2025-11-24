package com.webapp.comparar.config;

import com.webapp.comparar.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 🔥 CONFIGURACIÓN CORS MEJORADA PARA RAILWAY + VERCEL
        http.cors(cors -> {
            CorsConfiguration corsConfig = new CorsConfiguration();

            // ✅ DOMINIOS PERMITIDOS - ACTUALIZADOS PARA RAILWAY
            corsConfig.setAllowedOriginPatterns(Arrays.asList(
                    "https://*.vercel.app",
                    "https://*.vercel.com",
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://localhost:5174"
            ));

            // ✅ MÉTODOS PERMITIDOS - INCLUYENDO SSE
            corsConfig.setAllowedMethods(Arrays.asList(
                    "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
            ));

            // ✅ HEADERS PERMITIDOS - SSE NECESITA ESTOS HEADERS ESPECÍFICOS
            corsConfig.setAllowedHeaders(Arrays.asList(
                    "Accept",
                    "Content-Type",
                    "Authorization",
                    "Origin",
                    "Cache-Control",
                    "X-Requested-With",
                    "Access-Control-Request-Method",
                    "Access-Control-Request-Headers"
            ));

            // ✅ HEADERS EXPUESTOS PARA SSE
            corsConfig.setExposedHeaders(Arrays.asList(
                    "Content-Type",
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials"
            ));

            // ✅ CREDENCIALES PARA AUTENTICACIÓN
            corsConfig.setAllowCredentials(true);

            // ✅ MAX AGE PARA PREFLIGHT OPTIONS (importante para SSE)
            corsConfig.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", corsConfig);

            cors.configurationSource(source);
        });

        // 🔥 DESHABILITAR CSRF (IMPORTANTE PARA APIs)
        http.csrf(AbstractHttpConfigurer::disable);

        // 🔥 CONFIGURACIÓN DE AUTORIZACIÓN - CHATBOT ENDPOINTS PÚBLICOS
        http.authorizeHttpRequests(auth -> auth
                // ✅ CHATBOT ENDPOINTS - PERMITIR ACCESO SIN AUTENTICACIÓN
                .requestMatchers("/api/chatbot/consulta-stream").permitAll()
                .requestMatchers("/api/chatbot/consulta").permitAll()
                .requestMatchers("/api/chatbot/test-simple").permitAll()
                .requestMatchers("/api/chatbot/test-stream").permitAll()

                // ✅ ACTUATOR Y HEALTH CHECKS
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // ✅ DOCUMENTACIÓN Y APIS PÚBLICAS
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // ✅ RUTAS ESPECÍFICAS PÚBLICAS
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()

                // ✅ ESTÁTICOS Y RECURSOS PÚBLICOS
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/").permitAll()

                // ✅ TODOS LOS DEMÁS REQUIEREN AUTENTICACIÓN
                .anyRequest().authenticated()
        );

        // 🔥 H2 CONSOLE CONFIGURATION (SOLO PARA DESARROLLO)
        http.headers(headers -> headers
                .frameOptions(frame -> frame.disable())
        );

        // ✅ AGREGAR JWT FILTER (ANTES DE ELIMINARLO SI ES NECESARIO)
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}