package com.webapp.comparar.controller;

import com.webapp.comparar.dto.ChatbotRequest;
import com.webapp.comparar.dto.ChatbotResponse;
import com.webapp.comparar.dto.BuscarProductosRequest;
import com.webapp.comparar.dto.IngredienteEncontrado;
import com.webapp.comparar.service.ChatbotService;
import com.webapp.comparar.service.ChatbotProductosService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*") // 🔥 CORS GLOBAL para Railway
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private ChatbotProductosService chatbotProductosService;

    @GetMapping(value = "/consulta-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter consultarRecetaConStreaming(
            @RequestParam String mensaje,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response) { // 🔥 AGREGAR HttpServletResponse

        System.out.println("🎯 SOLUCIÓN RAILWAY: Solicitud recibida -> " + mensaje);

        // 🔥 HEADERS CORS MANUALES CRÍTICOS para Railway
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no"); // 🔥 IMPORTANTE para Nginx/Railway

        // ⬅️ TIMEOUT EXTENDIDO para Railway
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutos máximo

        // 🔥 CONFIGURACIÓN MEJORADA RAILWAY
        emitter.onCompletion(() -> {
            System.out.println("✅ SSE Completado normalmente en Railway");
        });

        emitter.onTimeout(() -> {
            System.out.println("⏰ SSE Timeout (3min) en Railway");
            emitter.complete();
        });

        emitter.onError((e) -> {
            System.err.println("❌ SSE Error en Railway: " + e.getMessage());
            try {
                emitter.complete();
            } catch (Exception ex) {
                // Ignorar errores al completar
            }
        });

        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        // 🔥 ENVIAR EVENTO INICIAL INMEDIATO para mantener conexión
        try {
            Map<String, Object> inicioEvent = new HashMap<>();
            inicioEvent.put("data", "🔄 Conectando con chef virtual...");
            inicioEvent.put("type", "inicio");
            inicioEvent.put("timestamp", System.currentTimeMillis());
            emitter.send(SseEmitter.event().name("inicio").data(inicioEvent));
        } catch (Exception e) {
            System.err.println("❌ Error enviando evento inicial: " + e.getMessage());
            emitter.complete();
            return emitter;
        }

        // EJECUTAR EN HILO SEPARADO INMEDIATAMENTE
        CompletableFuture.runAsync(() -> {
            try {
                chatbotService.obtenerRespuestaConStreaming(mensaje, isAuthenticated, emitter);
            } catch (Exception e) {
                System.err.println("❌ Error crítico en controller async: " + e.getMessage());
                try {
                    Map<String, Object> errorEvent = new HashMap<>();
                    errorEvent.put("data", "❌ Error crítico: " + e.getMessage());
                    errorEvent.put("type", "error_fatal");
                    emitter.send(SseEmitter.event().name("error_fatal").data(errorEvent));
                    emitter.complete();
                } catch (Exception ex) {
                    try {
                        emitter.complete();
                    } catch (Exception finalEx) {
                        // Ignorar cualquier error final
                    }
                }
            }
        });

        return emitter;
    }

    // 🔥 AGREGAR ENDPOINT DE HEALTH CHECK para Railway
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("service", "chatbot-streaming");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/consulta")
    public ResponseEntity<ChatbotResponse> consultarRecetaConProductos(
            @RequestBody ChatbotRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

            ChatbotResponse response = chatbotService.obtenerRespuestaIAPlusProductos(
                    request.getMensaje(),
                    isAuthenticated
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ChatbotResponse errorResponse = new ChatbotResponse(
                    "Lo siento, hubo un problema al procesar tu consulta o buscar productos. Por favor, intenta de nuevo."
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/solo-receta")
    public ResponseEntity<ChatbotResponse> consultarSoloReceta(
            @RequestBody ChatbotRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

            String respuesta = chatbotService.obtenerRespuestaIA(request.getMensaje(), isAuthenticated);

            ChatbotResponse response = new ChatbotResponse(respuesta);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ChatbotResponse errorResponse = new ChatbotResponse(
                    "Lo siento, hubo un problema al procesar tu consulta. Por favor, intenta de nuevo."
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/buscar-productos")
    public ResponseEntity<ChatbotResponse> buscarProductosDeReceta(
            @RequestBody BuscarProductosRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            if (request.getReceta() == null || request.getReceta().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<IngredienteEncontrado> productos = chatbotProductosService.buscarProductosPorReceta(request.getReceta());

            return ResponseEntity.ok(new ChatbotResponse("", productos));

        } catch (Exception e) {
            ChatbotResponse errorResponse = new ChatbotResponse(
                    "Lo siento, hubo un problema al buscar productos."
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}