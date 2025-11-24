package com.webapp.comparar.controller;

import com.webapp.comparar.dto.ChatbotRequest;
import com.webapp.comparar.dto.ChatbotResponse;
import com.webapp.comparar.dto.BuscarProductosRequest;
import com.webapp.comparar.dto.IngredienteEncontrado;
import com.webapp.comparar.service.ChatbotService;
import com.webapp.comparar.service.ChatbotProductosService;
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
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private ChatbotProductosService chatbotProductosService;

    @GetMapping(value = "/consulta-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter consultarRecetaConStreaming(
            @RequestParam String mensaje,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("🎯 SOLUCIÓN DEFINITIVA: Solicitud recibida -> " + mensaje);

        // ⬅️ TIMEOUT MÁS LARGO para dar tiempo a la IA
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutos máximo

        // 🔥 CONFIGURACIÓN MEJORADA ANTIDESCONEXIÓN
        emitter.onCompletion(() -> {
            System.out.println("✅ SSE Completado normalmente");
        });

        emitter.onTimeout(() -> {
            System.out.println("⏰ SSE Timeout (3min) - Cerrando conexión");
            emitter.complete();
        });

        emitter.onError((e) -> {
            System.err.println("❌ SSE Error: " + e.getMessage());
            try {
                emitter.complete();
            } catch (Exception ex) {
                // Ignorar errores al completar
            }
        });

        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        // EJECUTAR EN HILO SEPARADO INMEDIATAMENTE
        CompletableFuture.runAsync(() -> {
            try {
                chatbotService.obtenerRespuestaConStreaming(mensaje, isAuthenticated, emitter);
            } catch (Exception e) {
                System.err.println("❌ Error crítico en controller async: " + e.getMessage());
                try {
                    // Intentar enviar error final
                    Map<String, Object> errorEvent = new HashMap<>();
                    errorEvent.put("data", "❌ Error crítico: " + e.getMessage());
                    errorEvent.put("type", "error_fatal");
                    emitter.send(SseEmitter.event().name("error_fatal").data(errorEvent));
                    emitter.complete();
                } catch (Exception ex) {
                    // Si falla, simplemente completar
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