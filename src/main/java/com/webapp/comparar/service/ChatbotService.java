package com.webapp.comparar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webapp.comparar.dto.ChatbotResponse;
import com.webapp.comparar.dto.IngredienteEncontrado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.io.IOException;

@Service
public class ChatbotService {

    // ========================================================
    @Value("${google.ai.api.key}")
    private String apiKey;

    private final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // ========================================================
    // 🔥 STREAM PRINCIPAL SSE — versión estable
    // ========================================================
    public void streamReceta(String consulta, boolean isAuth, SseEmitter emitter) {

        try {
            // Evento inicial
            emitter.send(evento("inicio", "🤖 Procesando tu consulta..."));

            String receta = generarReceta(consulta, isAuth);

            // Evita 401 de Railway: enviar rápido el primer chunk
            emitter.send(evento("receta", " "));

            // Enviar fragmentado
            String[] partes = receta.split("(?<=\\s)");

            for (int i = 0; i < partes.length; i++) {
                emitter.send(evento("receta", partes[i]));
                Thread.sleep(10); // microdelay seguro
            }

            emitter.send(evento("completo", "✔️ Receta lista"));
            emitter.complete();

        } catch (Exception e) {
            try {
                emitter.send(evento("error", "❌ Error procesando tu solicitud"));
            } catch (Exception ignored) {}
            emitter.complete();
        }
    }

    // ========================================================
    private SseEmitter.SseEventBuilder evento(String tipo, String data) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", tipo);
        map.put("data", data);
        return SseEmitter.event().data(map);
    }

    // ========================================================
    public ChatbotResponse obtenerRespuestaIAPlusProductos(String consulta, boolean isAuth) {
        try {
            String receta = generarReceta(consulta, isAuth);
            return new ChatbotResponse(receta, new ArrayList<>());
        } catch (Exception e) {
            return new ChatbotResponse("Error procesando tu consulta.", new ArrayList<>());
        }
    }

    // ========================================================
    public String generarReceta(String consulta, boolean isAuth) {

        String prompt = construirPromptRecetas(consulta, isAuth);

        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();

        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);

        body.put("contents", contents);

        Map<String, Object> config = new HashMap<>();
        config.put("maxOutputTokens", 8000);
        body.put("generationConfig", config);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = GEMINI_API_URL + "?key=" + apiKey;

        ResponseEntity<String> res = rest.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class
        );

        if (!res.getStatusCode().is2xxSuccessful()) {
            return "❌ El servicio está saturado. Intenta nuevamente.";
        }

        try {
            JsonNode json = mapper.readTree(res.getBody());

            return json.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("❌ No se pudo generar receta.");
        } catch (Exception e) {
            return "❌ Error procesando la respuesta del modelo.";
        }
    }

    // ========================================================
    private String construirPromptRecetas(String msg, boolean isAuth) {

        return """
                Eres un asistente culinario experto. Responde SIEMPRE en español.

                FORMATO OBLIGATORIO:
                **TÍTULO**
                **INGREDIENTES:**
                - ingrediente: cantidad
                **INSTRUCCIONES:**
                1. Paso
                **TIEMPO ESTIMADO:** X minutos
                **PORCIONES:** X personas

                Mantén la receta clara, organizada y concisa.
                Consulta del usuario: 
                """ + msg;
    }
}
