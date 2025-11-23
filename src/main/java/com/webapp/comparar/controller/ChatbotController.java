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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response) {

        System.out.println("🎯 CHATBOT STREAM INICIADO - mensaje: " + mensaje);

        // Necesario para SSE en Railway
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Content-Type", "text/event-stream");

        // Timeout grande
        SseEmitter emitter = new SseEmitter(0L);

        // HEARTBEAT para que Railway NO mate la conexión
        Timer heartbeat = new Timer(true);
        heartbeat.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    emitter.send(SseEmitter.event()
                            .name("ping")
                            .data("💓")
                    );
                } catch (Exception e) {
                    heartbeat.cancel();
                }
            }
        }, 0, 800); // cada 800ms mantener la conexión viva

        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        CompletableFuture.runAsync(() -> {
            try {
                chatbotService.obtenerRespuestaConStreaming(mensaje, isAuthenticated, emitter);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("Error en el servidor"));
                } catch (Exception ignored) {}
            } finally {
                heartbeat.cancel();
                emitter.complete();
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