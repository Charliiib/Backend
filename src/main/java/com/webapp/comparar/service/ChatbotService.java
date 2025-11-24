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
import java.util.concurrent.CompletableFuture;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ChatbotService {

    @Value("${google_ai_api_key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";
    private final Random random = new Random();

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
            System.out.println("🚀 INICIANDO STREAMING (con Heartbeat) para: " + mensajeUsuario);

            // 1. Mensaje inicial inmediato (para abrir conexión rápido)
            Map<String, Object> inicioEvent = new HashMap<>();
            inicioEvent.put("data", "🤖 Iniciando...");
            inicioEvent.put("type", "inicio");
            emitter.send(SseEmitter.event().name("inicio").data(inicioEvent));

            // 2. Lanzar la petición a Gemini en un hilo SEPARADO (Asíncrono)
            CompletableFuture<String> futureReceta = CompletableFuture.supplyAsync(() -> {
                try {
                    return generarRecetaConIA(mensajeUsuario, isAuthenticated);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // 3. BUCLE DE ESPERA ACTIVA (Heartbeat)
            // Mientras la IA piensa, enviamos mensajes al front cada 1.5 segundos
            String[] mensajesEspera = {
                    "🔥 Calentando los fogones...",
                    "🥕 Picando ingredientes...",
                    "📖 Consultando el recetario...",
                    "🤖 Generando instrucciones...",
                    "🍳 Preparando tu respuesta..."
            };

            int contador = 0;

            // Loop mientras la tarea NO haya terminado
            while (!futureReceta.isDone()) {
                // Dormimos un poco (1.5 segundos)
                Thread.sleep(1500);

                // Si la IA tarda mucho, enviamos actualización para mantener el socket vivo
                if (!futureReceta.isDone()) {
                    String mensajeActual = mensajesEspera[contador % mensajesEspera.length];

                    Map<String, Object> keepAliveEvent = new HashMap<>();
                    keepAliveEvent.put("data", "⏳ " + mensajeActual);
                    keepAliveEvent.put("type", "inicio"); // Usamos 'inicio' para actualizar el globo de "Cargando"

                    try {
                        emitter.send(SseEmitter.event().name("inicio").data(keepAliveEvent));
                        System.out.println("💓 Keep-alive enviado: " + mensajeActual);
                    } catch (IOException e) {
                        // Si el cliente cerró la conexión, cancelamos la IA y salimos
                        System.err.println("❌ Cliente desconectado durante espera: " + e.getMessage());
                        futureReceta.cancel(true);
                        return;
                    }
                    contador++;
                }
            }

            // 4. Obtener el resultado final de la IA
            String recetaCompleta;
            try {
                recetaCompleta = futureReceta.get(); // Esto ya será inmediato porque isDone es true
            } catch (Exception e) {
                throw new RuntimeException("Error al obtener resultado de IA", e);
            }

            // --- A PARTIR DE AQUÍ ES IGUAL A TU CÓDIGO ORIGINAL ---

            // Verificar si la respuesta es un mensaje de error
            if (recetaCompleta.contains("Lo sentimos, estamos experimentando una alta demanda")) {
                throw new RuntimeException("Service unavailable - returned error message");
            }

            System.out.println("📝 Receta lista - Longitud: " + recetaCompleta.length());

            // Verificar validez
            boolean esRecetaValida = esRecetaValida(recetaCompleta);

            if (!esRecetaValida) {
                // ... Lógica de respuesta no válida (copia tu lógica original aquí) ...
                // Para simplificar el ejemplo, asumo el flujo normal:
                enviarRespuestaFragmentada(recetaCompleta, emitter);
                // Finalizar
                Map<String, Object> completoEvent = new HashMap<>();
                completoEvent.put("data", "✅ Fin");
                completoEvent.put("type", "completo");
                emitter.send(SseEmitter.event().name("completo").data(completoEvent));
                emitter.complete();
                return;
            }

            // Mensaje de que ya tenemos la receta
            Map<String, Object> preparandoEvent = new HashMap<>();
            preparandoEvent.put("data", "📝 ¡Receta lista! Escribiendo...");
            preparandoEvent.put("type", "empezando");
            emitter.send(SseEmitter.event().name("inicio").data(preparandoEvent));

            // 5. Streaming del texto (Tu lógica original de fragmentación)
            String[] fragmentos = recetaCompleta.split("(?<=\\s)|(?<=\\n)");
            long delayFragmento = 30;

            for (int i = 0; i < fragmentos.length; i++) {
                String fragmento = fragmentos[i];
                if (!fragmento.trim().isEmpty()) {
                    Thread.sleep(delayFragmento);
                    Map<String, Object> lineaEvent = new HashMap<>();
                    lineaEvent.put("linea", fragmento);
                    lineaEvent.put("indice", i);
                    lineaEvent.put("total", fragmentos.length);
                    lineaEvent.put("progreso", (i + 1) * 100 / fragmentos.length);
                    lineaEvent.put("esUltimo", i == fragmentos.length - 1);
                    lineaEvent.put("type", "receta");
                    emitter.send(SseEmitter.event().name("receta").data(lineaEvent));
                }
            }

            // 6. Mensaje de cierre IA
            String mensajeCierre = generarMensajeCierreGenerico(recetaCompleta);
            String mensajeCompleto = "\n\n---\n\n" + mensajeCierre;
            String[] fragmentosCierre = mensajeCompleto.split("(?<=\\s)|(?<=\\n)");

            for (String fragmento : fragmentosCierre) {
                if (!fragmento.trim().isEmpty()) {
                    Thread.sleep(45);
                    Map<String, Object> cierreEvent = new HashMap<>();
                    cierreEvent.put("linea", fragmento);
                    cierreEvent.put("progreso", 100);
                    cierreEvent.put("type", "receta");
                    emitter.send(SseEmitter.event().name("receta").data(cierreEvent));
                }
            }

            // Finalizar
            Thread.sleep(500);
            Map<String, Object> completoEvent = new HashMap<>();
            completoEvent.put("data", "✅ Receta completada!");
            completoEvent.put("type", "completo");
            emitter.send(SseEmitter.event().name("completo").data(completoEvent));
            emitter.complete();

        } catch (Exception e) {
            System.err.println("❌ Error en streaming: " + e.getMessage());
            try {
                emitter.completeWithError(e);
            } catch (Exception ex) {
                // Ignorar si ya estaba cerrado
            }
        }
    }

    // NUEVO MÉTODO: Verificar si la respuesta es una receta válida
    private boolean esRecetaValida(String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            return false;
        }

        // Patrones que indican que NO es una receta válida
        String[] patronesNoReceta = {
                "no está dentro de mi área",
                "no puedo ayudarte con eso",
                "fuera de mi conocimiento",
                "especialidad son las recetas",
                "consulta culinaria",
                "pregunta sobre",
                "no es una consulta culinaria",
                "asistente culinario"
        };

        String respuestaLower = respuesta.toLowerCase();

        for (String patron : patronesNoReceta) {
            if (respuestaLower.contains(patron.toLowerCase())) {
                return false;
            }
        }

        // Patrones que indican que SÍ es una receta válida
        String[] patronesReceta = {
                "**ingredientes:**",
                "**instrucciones:**",
                "**tiempo estimado:**",
                "**porciones:**",
                "1. ",
                "2. ",
                "3. ",
                "- ",
                "gramos",
                "tazas",
                "cucharadas"
        };

        for (String patron : patronesReceta) {
            if (respuestaLower.contains(patron.toLowerCase())) {
                return true;
            }
        }

        // Si no coincide con ningún patrón, asumimos que no es receta
        return false;
    }

    // NUEVO MÉTODO: Enviar respuesta fragmentada sin mensaje de cierre
    private void enviarRespuestaFragmentada(String respuesta, SseEmitter emitter) throws InterruptedException, IOException {
        String[] fragmentos = respuesta.split("(?<=\\s)|(?<=\\n)");
        long delayFragmento = 30;

        for (int i = 0; i < fragmentos.length; i++) {
            String fragmento = fragmentos[i];

            if (!fragmento.trim().isEmpty()) {
                Thread.sleep(delayFragmento);

                Map<String, Object> lineaEvent = new HashMap<>();
                lineaEvent.put("linea", fragmento);
                lineaEvent.put("indice", i);
                lineaEvent.put("total", fragmentos.length);
                lineaEvent.put("progreso", (i + 1) * 100 / fragmentos.length);
                lineaEvent.put("esUltimo", i == fragmentos.length - 1);
                lineaEvent.put("type", "receta");

                emitter.send(SseEmitter.event().name("receta").data(lineaEvent));
            }
        }
    }

    // =========================================================================
    // FUNCIÓN MEJORADA: Generar un mensaje de cierre personalizado con IA
    // =========================================================================

    private String generarMensajeCierreGenerico(String recetaCompleta) {

        // Mensajes de respaldo alternativos
        String[] fallbackMessages = {
                "¡Manos a la obra! Espero que te quede deliciosa. 😋",
                "¡A cocinar! Que esta receta te traiga mucha felicidad. 😊",
                "Suena increíble, ¡espero que lo disfrutes! 🧑‍🍳",
                "¡Buen provecho! Avísame si necesitas otra cosa. 😉"
        };

        String fallback = fallbackMessages[random.nextInt(fallbackMessages.length)];

        try {
            // Usamos solo el título de la receta como contexto para ser más rápido
            String titulo = "Receta";
            int startIndex = recetaCompleta.indexOf("**");
            int endIndex = recetaCompleta.indexOf("**", startIndex + 2);

            if (startIndex != -1 && endIndex != -1) {
                titulo = recetaCompleta.substring(startIndex + 2, endIndex).trim();
            }

            String promptCierre = "Eres un asistente culinario divertido y entusiasta. Genera un mensaje de cierre corto (máximo 15 palabras) y entusiasta relacionado con el título de esta receta, motivando al usuario a cocinar. Usa emojis culinarios y evita listas, títulos o negritas. El título es: " + titulo;

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", promptCierre);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 1.0); // Aún más creativo
            generationConfig.put("maxOutputTokens", 50);
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

                // 1. Verificar si hay un error de bloqueo de contenido o seguridad
                if (jsonResponse.has("promptFeedback") && jsonResponse.get("promptFeedback").has("blockReason")) {
                    System.err.println("API bloqueó la respuesta: " + jsonResponse.get("promptFeedback").get("blockReason").asText());
                    return fallback;
                }

                // 2. Extraer el texto de manera segura
                if (jsonResponse.has("candidates") && jsonResponse.get("candidates").size() > 0) {
                    JsonNode candidate = jsonResponse.get("candidates").get(0);
                    if (candidate.has("content") && candidate.get("content").has("parts") && candidate.get("content").get("parts").size() > 0) {
                        JsonNode part1 = candidate.get("content").get("parts").get(0);
                        if (part1.has("text")) {
                            return part1.get("text").asText().trim();
                        }
                    }
                }
            } else {
                System.err.println("Error de API al generar cierre - Estado: " + response.getStatusCode());
                // Podríamos intentar parsear el cuerpo para obtener el error de la API
                if (response.hasBody()) {
                    System.err.println("Cuerpo de error: " + response.getBody());
                }
            }

            return fallback; // Mensaje de respaldo si el JSON está vacío o mal formado

        } catch (Exception e) {
            System.err.println("Error al generar mensaje de cierre: " + e.getMessage());
            e.printStackTrace();
            return fallback; // Mensaje de respaldo si hay una excepción
        }
    }


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

            return "Lo sentimos, el servicio de recetas está temporalmente saturado. Por favor, vuelve a intentarlo en unos minutos. 🕒";

        } catch (Exception e) {
            System.err.println("Error al llamar a Google AI API: " + e.getMessage());
            e.printStackTrace();
            return "Lo sentimos, estamos experimentando una alta demanda en este momento. Por favor, vuelve a probar en unos minutos. 🕒";
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

            // Retornar mensaje de error en lugar de lanzar excepción
            return "❌ Lo sentimos, estamos experimentando una alta demanda en este momento. Por favor, vuelve a probar en unos minutos. 🕒";

        } catch (Exception e) {
            System.err.println("Error al llamar a Google AI API: " + e.getMessage());
            // Retornar mensaje de error en lugar de lanzar excepción
            return "❌ Lo sentimos, estamos experimentando una alta demanda en este momento. Por favor, vuelve a probar en unos minutos. 🕒";
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

        prompt.append("IMPORTANTE: Mantén la receta completa pero evita texto innecesario.\\n\\n");

        if (!isAuthenticated) {
            prompt.append("NOTA: El usuario no está autenticado.\\n\\n");
        }

        prompt.append("CONSULTA DEL USUARIO: ");
        prompt.append(mensajeUsuario);

        return prompt.toString();
    }


}