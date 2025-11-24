package com.webapp.comparar.controller;

import com.webapp.comparar.service.ChatbotService;
import com.webapp.comparar.dto.ChatbotResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    // ========================================================
    // 🎯 ENDPOINT SSE PARA RAILWAY - VERSIÓN SIMPLIFICADA
    // ========================================================
    @GetMapping(value = "/consulta-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> consultaStream(
            @RequestParam(required = false) String mensaje,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("🎯 SSE ENDPOINT ACCEDIDO con mensaje: " + mensaje);
        System.out.println("📋 Headers - Auth presente: " + (authHeader != null));
        System.out.println("🌐 Railway request from Vercel - CORS configurado");

        try {
            // Crear emitter con timeout más largo para Railway
            SseEmitter emitter = new SseEmitter(120000L); // 2 minutos

            // Callbacks para debugging
            emitter.onCompletion(() -> {
                System.out.println("✅ SSE COMPLETADO - Cliente desconectado");
            });

            emitter.onTimeout(() -> {
                System.out.println("⏰ SSE TIMEOUT - Configuración de Railway");
            });

            emitter.onError((e) -> {
                System.out.println("❌ SSE ERROR en Railway: " + e.getMessage());
                e.printStackTrace();
            });

            // Enviar evento de inicialización inmediato
            try {
                System.out.println("📤 Enviando evento inicial para Railway...");
                emitter.send(SseEmitter.event()
                        .name("conectado")
                        .data(Map.of(
                                "type", "conectado",
                                "data", "🤖 Conexión establecida con backend Railway ✓")));
                System.out.println("✅ Evento inicial enviado exitosamente");
            } catch (IOException e) {
                System.out.println("❌ Error enviando evento inicial: " + e.getMessage());
                return ResponseEntity.ok().build();
            }

            // Procesar consulta de forma asíncrona
            CompletableFuture.runAsync(() -> {
                try {
                    boolean isAuth = authHeader != null && authHeader.startsWith("Bearer ");
                    System.out.println("🚀 Iniciando streamReceta Railway con auth=" + isAuth);

                    chatbotService.streamReceta(mensaje != null ? mensaje : "pizza", isAuth, emitter);

                    System.out.println("✅ streamReceta Railway completado exitosamente");

                } catch (Exception e) {
                    System.out.println("❌ Error en thread Railway: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(Map.of("type", "error", "data", "❌ Error interno Railway: " + e.getMessage())));
                    } catch (IOException ex) {
                        System.out.println("❌ Error enviando mensaje error Railway: " + ex.getMessage());
                    }
                }
            });

            System.out.println("🚀 SSE Emitter Railway configurado y ejecutándose...");
            return ResponseEntity.ok(emitter);

        } catch (Exception e) {
            System.out.println("❌ Error general en consultaStream Railway: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================================
    // 🧪 ENDPOINT DE TEST SIMPLE PARA VERIFICAR RAILWAY
    // ========================================================
    @GetMapping("/test-simple")
    public ResponseEntity<String> testSimple(@RequestParam(required = false, defaultValue = "test") String mensaje) {
        System.out.println("🧪 TEST SIMPLE Railway recibido: " + mensaje);

        try {
            String respuesta = chatbotService.obtenerRespuestaIAPlusProductos(mensaje, false);
            System.out.println("✅ Respuesta Railway generada correctamente");
            return ResponseEntity.ok("🚀 BACKEND RAILWAY FUNCIONANDO CORRECTAMENTE\n\n" + respuesta);
        } catch (Exception e) {
            System.out.println("❌ Error en testSimple Railway: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Error Railway: " + e.getMessage());
        }
    }

    // ========================================================
    // 🧪 ENDPOINT DE TEST SSE PARA RAILWAY
    // ========================================================
    @GetMapping(value = "/test-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> testStream() {
        System.out.println("🧪 TEST STREAM Railway iniciado");

        SseEmitter emitter = new SseEmitter(60000L); // 1 minuto

        emitter.onCompletion(() -> System.out.println("✅ Test stream Railway completado"));
        emitter.onTimeout(() -> System.out.println("⏰ Test stream Railway timeout"));
        emitter.onError((e) -> System.out.println("❌ Test stream Railway error: " + e.getMessage()));

        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("📤 Enviando eventos de prueba Railway...");

                // Evento 1
                emitter.send(SseEmitter.event()
                        .name("prueba")
                        .data(Map.of("type", "prueba", "data", "🚀 Evento 1 - Backend Railway funcionando")));

                Thread.sleep(500);

                // Evento 2
                emitter.send(SseEmitter.event()
                        .name("prueba")
                        .data(Map.of("type", "prueba", "data", "🎯 Evento 2 - SSE configuración correcta")));

                Thread.sleep(500);

                // Evento final
                emitter.send(SseEmitter.event()
                        .name("final")
                        .data(Map.of("type", "final", "data", "✅ Test Railway completado - Chatbot listo")));

                emitter.complete();
                System.out.println("✅ Test stream Railway enviado completamente");

            } catch (Exception e) {
                System.out.println("❌ Error en test stream Railway: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return ResponseEntity.ok(emitter);
    }

    // ========================================================
    // 📝 ENDPOINT REGULAR (NO SSE) - VERSIÓN RAILWAY
    // ========================================================
    @PostMapping("/consulta")
    public ResponseEntity<ChatbotResponse> consulta(
            @RequestBody(required = false) Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("📝 ENDPOINT REGULAR Railway ACCEDIDO");

        try {
            String mensaje = request != null ? request.get("mensaje") : "";
            boolean isAuth = authHeader != null && authHeader.startsWith("Bearer ");

            System.out.println("🎯 Procesando consulta regular Railway: " + mensaje);

            ChatbotResponse response = chatbotService.obtenerRespuestaIAPlusProductos(mensaje, isAuth);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error en consulta regular Railway: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}