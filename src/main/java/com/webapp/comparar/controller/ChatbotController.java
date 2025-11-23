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

        System.out.println("🎯 CHATBOT CONSULTA-STREAM ACCEDIDO - Mensaje: " + mensaje);

        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(0L);

        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        // 🔥 PING INICIAL para que Railway NO corte la conexión
        try {
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("inicio"));
        } catch (Exception e) {
            System.out.println("Error enviando ping inicial: " + e.getMessage());
        }

        CompletableFuture.runAsync(() -> {
            try {
                chatbotService.obtenerRespuestaConStreaming(mensaje, isAuthenticated, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
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