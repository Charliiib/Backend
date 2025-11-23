package com.webapp.comparar.controller;

import com.webapp.comparar.dto.ChatbotRequest;
import com.webapp.comparar.dto.ChatbotResponse;
import com.webapp.comparar.dto.BuscarProductosRequest;
import com.webapp.comparar.dto.IngredienteEncontrado;
import com.webapp.comparar.service.ChatbotService;
import com.webapp.comparar.service.ChatbotProductosService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private ChatbotProductosService chatbotProductosService;

    // ========================================================
    // 🔥 SSE ENDPOINT — funcionando en Railway sin 401
    // ========================================================
    @GetMapping(value = "/consulta-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter consultarRecetaConStreaming(
            @RequestParam String mensaje,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response) {

        System.out.println("🎯 SSE ACCEDIDO → " + mensaje);

        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        // Importante: timeout alto para evitar desconexión Railway
        SseEmitter emitter = new SseEmitter(1000L * 90); // 90 segundos

        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        CompletableFuture.runAsync(() -> {
            try {
                chatbotService.streamReceta(mensaje, isAuthenticated, emitter);
            } catch (Exception e) {
                System.out.println("❌ Error SSE: " + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // ========================================================
    @PostMapping("/consulta")
    public ResponseEntity<ChatbotResponse> consultarRecetaConProductos(
            @RequestBody ChatbotRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        ChatbotResponse response = chatbotService.obtenerRespuestaIAPlusProductos(
                request.getMensaje(), isAuthenticated
        );

        return ResponseEntity.ok(response);
    }

    // ========================================================
    @PostMapping("/solo-receta")
    public ResponseEntity<ChatbotResponse> consultarSoloReceta(
            @RequestBody ChatbotRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        String receta = chatbotService.generarReceta(request.getMensaje(), isAuthenticated);

        return ResponseEntity.ok(new ChatbotResponse(receta));
    }

    // ========================================================
    @PostMapping("/buscar-productos")
    public ResponseEntity<ChatbotResponse> buscarProductosDeReceta(
            @RequestBody BuscarProductosRequest request) {

        if (request.getReceta() == null || request.getReceta().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<IngredienteEncontrado> productos = chatbotProductosService.buscarProductosPorReceta(request.getReceta());

        return ResponseEntity.ok(new ChatbotResponse("", productos));
    }
}