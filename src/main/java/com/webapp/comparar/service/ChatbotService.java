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

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatbotService {

    @Value("${google.ai.api.key}")
    private String apiKey;

    // OPCIÓN 1: Modelo Gemini 2.5 Flash (más reciente y estable) - Usa v1
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    @Autowired
    private ChatbotProductosService chatbotProductosService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChatbotService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public ChatbotResponse obtenerRespuestaIAPlusProductos(String mensajeUsuario, boolean isAuthenticated) {
        try {
            // 1. Generar la receta con IA
            String receta = generarRecetaConIA(mensajeUsuario, isAuthenticated);

            // 2. Buscar productos relacionados con la receta
            List<IngredienteEncontrado> productos = chatbotProductosService.buscarProductosPorReceta(receta);

            // 3. Crear la respuesta completa
            return new ChatbotResponse(receta, productos);

        } catch (Exception e) {
            System.err.println("Error al procesar consulta: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, devolver solo la receta sin productos
            String recetaError = "Lo siento, hubo un problema al buscar los productos. Aquí tienes la receta:\n\n" +
                    "INGREDIENTES:\n- Revisa en tu despensa\n- Busca los ingredientes básicos\n\nINSTRUCCIONES:\n- 1. Prepara todos los ingredientes\n- 2. Sigue el proceso tradicional\n\nBuena suerte con tu cocina! 👨‍🍳";

            return new ChatbotResponse(recetaError, new ArrayList<>());
        }
    }

    public String obtenerRespuestaIA(String mensajeUsuario, boolean isAuthenticated) {
        try {
            // Construir el prompt con contexto especializado en recetas
            String promptCompleto = construirPromptRecetas(mensajeUsuario, isAuthenticated);

            // Preparar el body de la petición
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

            // Configurar parámetros de generación
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 3072);
            requestBody.put("generationConfig", generationConfig);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Crear la petición
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // URL con la API key
            String urlWithKey = GEMINI_API_URL + "?key=" + apiKey;

            // Hacer la petición a Google AI
            ResponseEntity<String> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parsear la respuesta
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                // Navegar por la estructura de respuesta de Gemini
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
            // Preparar el body de la petición
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

            // Configurar parámetros de generación
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 3072);
            requestBody.put("generationConfig", generationConfig);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Crear la petición
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // URL con la API key
            String urlWithKey = GEMINI_API_URL + "?key=" + apiKey;

            // Hacer la petición a Google AI
            ResponseEntity<String> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parsear la respuesta
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                // Navegar por la estructura de respuesta de Gemini
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

        prompt.append("Eres un asistente culinario experto y amigable especializado en recetas. ");
        prompt.append("Tu objetivo es ayudar a las personas a encontrar y preparar recetas deliciosas.\n\n");

        prompt.append("INSTRUCCIONES:\n");
        prompt.append("- Responde siempre en español de forma clara y estructurada\n");
        prompt.append("- Si te preguntan por una receta, proporciona ingredientes (con cantidades), pasos de preparación y tiempo estimado\n");
        prompt.append("- Estructura la respuesta con: TÍTULO, INGREDIENTES (con cantidades), PASOS DE PREPARACIÓN, TIEMPO ESTIMADO\n");
        prompt.append("- Si no especifican cantidad de personas, asume 4 porciones\n");
        prompt.append("- Incluye consejos útiles cuando sea relevante\n");
        prompt.append("- Si la consulta no está relacionada con cocina o recetas, redirige amablemente al tema culinario\n");
        prompt.append("- Sé claro e informativo, puedes extenderte lo necesario para explicar bien la receta\n");
        prompt.append("- Usa emojis ocasionalmente para hacer la respuesta más amigable\n\n");

        prompt.append("IMPORTANTE: NO menciones dónde comprar productos ni des sugerencias de marcas.\n");
        prompt.append("Enfócate solo en la receta y preparación de los ingredientes.\n\n");

        if (!isAuthenticated) {
            prompt.append("NOTA: El usuario no está autenticado, recuérdale que puede registrarse para guardar sus recetas favoritas.\n\n");
        }

        prompt.append("CONSULTA DEL USUARIO:\n");
        prompt.append(mensajeUsuario);

        return prompt.toString();
    }

    private String generarRecetaDeRespaldo(String mensajeUsuario) {
        return "🍳 **Receta Casera - " + mensajeUsuario + "**\n\n" +
                "**INGREDIENTES:**\n" +
                "- 500g de ingredientes principales\n" +
                "- 2-3 ingredientes de sabor\n" +
                "- Especias al gusto\n" +
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
