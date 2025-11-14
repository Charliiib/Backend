package com.webapp.comparar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webapp.comparar.dto.ChatbotResponse;
import com.webapp.comparar.dto.IngredienteEncontrado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatbotService {

    @Value("${google.ai.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    @Autowired
    private ChatbotProductosService chatbotProductosService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChatbotService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void obtenerRespuestaConStreaming(String mensajeUsuario, boolean isAuthenticated, SseEmitter emitter) {
        try {
            System.out.println("🚀 INICIANDO STREAMING para: " + mensajeUsuario);

            // 1. Evento de inicio
            Map<String, Object> inicioEvent = new HashMap<>();
            inicioEvent.put("data", "🤖 Analizando tu consulta...");
            inicioEvent.put("type", "inicio");
            emitter.send(SseEmitter.event().name("inicio").data(inicioEvent));
            Thread.sleep(800);

            // 2. Generar receta completa
            String recetaCompleta = generarRecetaConIA(mensajeUsuario, isAuthenticated);

            System.out.println("📝 Receta generada - Longitud: " + recetaCompleta.length() + " caracteres");

            // 3. Evento de que empezó la generación
            Map<String, Object> generandoEvent = new HashMap<>();
            generandoEvent.put("data", "📝 Generando receta...");
            generandoEvent.put("type", "empezando");
            emitter.send(SseEmitter.event().name("inicio").data(generandoEvent));
            Thread.sleep(600);

            // 4. DIVIDIR POR PALABRAS/FRAGMENTOS y enviar
            // Split que mantiene los delimitadores (espacios y saltos de línea) como fragmentos separados
            String[] fragmentos = recetaCompleta.split("(?<=\\s)|(?<=\\n)");
            System.out.println("📊 Número de fragmentos a enviar: " + fragmentos.length);

            long delayFragmento = 50; // 50ms por fragmento para un efecto de escritura natural

            int fragmentosEnviados = 0;
            for (int i = 0; i < fragmentos.length; i++) {
                String fragmento = fragmentos[i];

                if (!fragmento.isEmpty()) {
                    // Simular el efecto de escritura con un pequeño delay
                    Thread.sleep(delayFragmento);

                    // Crear evento de línea/fragmento
                    Map<String, Object> lineaEvent = new HashMap<>();
                    lineaEvent.put("linea", fragmento); // Enviar solo el fragmento
                    lineaEvent.put("indice", i);
                    lineaEvent.put("total", fragmentos.length);
                    // Progreso usando el total de fragmentos
                    lineaEvent.put("progreso", (i + 1) * 100 / fragmentos.length);
                    lineaEvent.put("esUltimo", i == fragmentos.length - 1);
                    lineaEvent.put("type", "receta");

                    // Enviar el fragmento
                    emitter.send(SseEmitter.event().name("receta").data(lineaEvent));

                    fragmentosEnviados++;
                }
            }

            // 5. Evento de completado
            Thread.sleep(500);
            Map<String, Object> completoEvent = new HashMap<>();
            completoEvent.put("data", "✅ Receta completada!");
            completoEvent.put("type", "completo");
            emitter.send(SseEmitter.event().name("completo").data(completoEvent));

            System.out.println("🎉 STREAMING COMPLETADO");

            emitter.complete();

        } catch (Exception e) {
            System.err.println("❌ Error en streaming: " + e.getMessage());
            e.printStackTrace();

            try {
                Map<String, Object> errorEvent = new HashMap<>();
                errorEvent.put("data", "❌ Error al generar la receta");
                errorEvent.put("type", "error");
                emitter.send(SseEmitter.event().name("error").data(errorEvent));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(e);
            }
        }
    }

    // ELIMINADA la función calcularDelayLinea ya que no es necesaria.
    // ... (El resto de métodos auxiliares como generarMensajeProductos, obtenerRespuestaIAPlusProductos,
    // obtenerRespuestaIA, generarRecetaConIA, construirPromptRecetas, generarRecetaDeRespaldo permanecen sin cambios)

    private String generarMensajeProductos(List<IngredienteEncontrado> productos) {
        if (productos == null || productos.isEmpty()) {
            return "No se encontraron productos para los ingredientes de esta receta.";
        }

        int totalProductos = productos.stream()
                .mapToInt(p -> p.getProductos().size())
                .sum();

        return String.format("📦 Encontré %d productos relacionados con tu receta!", totalProductos);
    }

    public ChatbotResponse obtenerRespuestaIAPlusProductos(String mensajeUsuario, boolean isAuthenticated) {
        try {
            String receta = generarRecetaConIA(mensajeUsuario, isAuthenticated);
            List<IngredienteEncontrado> productos = chatbotProductosService.buscarProductosPorReceta(receta);

            return new ChatbotResponse(receta, productos);

        } catch (Exception e) {
            System.err.println("Error al procesar consulta: " + e.getMessage());
            e.printStackTrace();

            String recetaError = "Lo siento, hubo un problema al buscar los productos. Aquí tienes la receta:\n\n" +
                    "INGREDIENTES:\n- Revisa en tu despensa\n- Busca los ingredientes básicos\n\nINSTRUCCIONES:\n- 1. Prepara todos los ingredientes\n- 2. Sigue el proceso tradicional\n\nBuena suerte con tu cocina! 👨‍🍳";

            return new ChatbotResponse(recetaError, new ArrayList<>());
        }
    }

    public String obtenerRespuestaIA(String mensajeUsuario, boolean isAuthenticated) {
        try {
            String promptCompleto = construirPromptRecetas(mensajeUsuario, isAuthenticated);

            Map<String, Object> requestBody = new HashMap<>();

            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();

            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", promptCompleto);
            parts.add(part);

            content.put("parts", parts);
            contents.add(content);

            requestBody.put("contents", contents);

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 8192);
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String urlWithKey = GEMINI_API_URL + "?key=" + apiKey;

            ResponseEntity<String> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                if (jsonResponse.has("candidates") && jsonResponse.get("candidates").size() > 0) {
                    JsonNode candidate = jsonResponse.get("candidates").get(0);
                    if (candidate.has("content")) {
                        JsonNode content1 = candidate.get("content");
                        if (content1.has("parts") && content1.get("parts").size() > 0) {
                            JsonNode part1 = content1.get("parts").get(0);
                            if (part1.has("text")) {
                                return part1.get("text").asText();
                            }
                        }
                    }
                }
            }

            return "Lo siento, no pude generar una respuesta en este momento. Por favor, intenta de nuevo.";

        } catch (Exception e) {
            System.err.println("Error al llamar a Google AI API: " + e.getMessage());
            e.printStackTrace();
            return "Hubo un error al procesar tu consulta. Por favor, verifica que la API esté configurada correctamente.";
        }
    }

    private String generarRecetaConIA(String mensajeUsuario, boolean isAuthenticated) {
        String promptCompleto = construirPromptRecetas(mensajeUsuario, isAuthenticated);

        try {
            Map<String, Object> requestBody = new HashMap<>();

            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();

            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", promptCompleto);
            parts.add(part);

            content.put("parts", parts);
            contents.add(content);

            requestBody.put("contents", contents);

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 8192);
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String urlWithKey = GEMINI_API_URL + "?key=" + apiKey;

            ResponseEntity<String> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                if (jsonResponse.has("candidates") && jsonResponse.get("candidates").size() > 0) {
                    JsonNode candidate = jsonResponse.get("candidates").get(0);
                    if (candidate.has("content")) {
                        JsonNode content1 = candidate.get("content");
                        if (content1.has("parts") && content1.get("parts").size() > 0) {
                            JsonNode part1 = content1.get("parts").get(0);
                            if (part1.has("text")) {
                                return part1.get("text").asText();
                            }
                        }
                    }
                }
            }

            return generarRecetaDeRespaldo(mensajeUsuario);

        } catch (Exception e) {
            System.err.println("Error al llamar a Google AI API: " + e.getMessage());
            return generarRecetaDeRespaldo(mensajeUsuario);
        }
    }

    private String construirPromptRecetas(String mensajeUsuario, boolean isAuthenticated) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Eres un asistente culinario experto especializado en recetas. Responde en español.\n\n");

        prompt.append("INSTRUCCIONES ESPECÍFICAS:\n");
        prompt.append("- Proporciona recetas completas pero CONCISAS\n");
        prompt.append("- Estructura: TÍTULO, INGREDIENTES (cantidades exactas), INSTRUCCIONES (pasos numerados), TIEMPO, PORCIONES\n");
        prompt.append("- Máximo 10 ingredientes y 8 pasos de preparación\n");
        prompt.append("- Asume 4 porciones si no se especifica\n");
        prompt.append("- Usa formato claro con saltos de línea\n");
        prompt.append("- Incluye tiempo total de preparación y cocción\n");
        prompt.append("- Sé específico con cantidades (gramos, tazas, cucharadas)\n\n");

        prompt.append("FORMATO OBLIGATORIO:\n");
        prompt.append("**TÍTULO DE LA RECETA**\n\n");
        prompt.append("**INGREDIENTES:**\n");
        prompt.append("- Ingrediente 1: cantidad\n");
        prompt.append("- Ingrediente 2: cantidad\n\n");

        prompt.append("**INSTRUCCIONES:**\n");
        prompt.append("1. Paso 1\n");
        prompt.append("2. Paso 2\n\n");

        prompt.append("**TIEMPO ESTIMADO:** X minutos\n");
        prompt.append("**PORCIONES:** X personas\n\n");

        prompt.append("IMPORTANTE: Mantén la receta completa pero evita texto innecesario.\n\n");

        if (!isAuthenticated) {
            prompt.append("NOTA: El usuario no está autenticado.\n\n");
        }

        prompt.append("CONSULTA DEL USUARIO: ");
        prompt.append(mensajeUsuario);

        return prompt.toString();
    }

    private String generarRecetaDeRespaldo(String mensajeUsuario) {
        return "🍳 **Receta Casera - " + mensajeUsuario + "**\n\n" +
                "**INGREDIENTES:**\n" +
                "- 500g de ingredientes principales\n" +
                "- 2-3 ingredientes de sabor\n" +
                "- Especias al gusto\\n" +
                "- Aceite, sal y pimienta\n\n" +
                "**PREPARACIÓN:**\n" +
                "1. Prepara todos los ingredientes\n" +
                "2. Cocina a fuego medio\n" +
                "3. Ajusta sabores a tu gusto\n" +
                "4. Sirve y disfruta!\n\n" +
                "⏱️ **Tiempo estimado:** 30-45 minutos\n" +
                "👥 **Porciones:** 4 personas\n\n" +
                "💡 **Consejo:** ¡Experimenta con diferentes condimentos para personalizar la receta!";
    }
}