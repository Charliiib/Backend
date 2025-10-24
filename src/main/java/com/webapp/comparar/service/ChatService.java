package com.webapp.comparar.service;

import com.webapp.comparar.model.Producto;
import com.webapp.comparar.model.ProductoPrecio;
import com.webapp.comparar.model.Usuario;
import com.webapp.comparar.repository.ProductoRepository;
import com.webapp.comparar.repository.ProductoPrecioRepository;
import com.webapp.comparar.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ChatService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoPrecioRepository productoPrecioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Map<String, Object> processMessage(String message, Integer userId) {
        Map<String, Object> response = new HashMap<>();

        // Obtener usuario si está logueado
        Usuario usuario = null; // Cambiado de User a Usuario
        if (userId != null) {
            usuario = usuarioRepository.findById(userId).orElse(null); // Cambiado
        }

        // 1. Detectar intención del usuario
        String intention = detectIntention(message.toLowerCase());

        // 2. Buscar productos y precios si es relevante
        List<Producto> productos = new ArrayList<>();
        List<Map<String, Object>> precios = new ArrayList<>();

        if (intention.equals("BUSCAR_PRODUCTO") || intention.equals("CONSULTAR_PRECIO")) {
            productos = buscarProductosInteligente(message);
            if (!productos.isEmpty()) {
                precios = obtenerPreciosProductos(productos);
            }
        }

        // 3. Generar respuesta apropiada
        String aiResponse = generateResponseByIntention(intention, message, productos, precios, usuario); // Cambiado

        response.put("response", aiResponse);
        response.put("productos", productos);
        response.put("precios", precios);
        response.put("intention", intention);

        return response;
    }

    // Método para mensaje de bienvenida inicial
    public Map<String, Object> getWelcomeMessage(Integer userId) { // Cambiado de Long a Integer
        Map<String, Object> response = new HashMap<>();

        Usuario usuario = null; // Cambiado
        if (userId != null) {
            usuario = usuarioRepository.findById(userId).orElse(null); // Cambiado
        }

        String welcomeMessage;
        if (usuario != null) {
            // Usar nombreUsuario en lugar de nombre
            welcomeMessage = "¡Hola " + usuario.getNombreUsuario() + "! 👋\n\n" + // Cambiado
                    "Me alegra verte de nuevo en **Comparar**. ¿En qué puedo ayudarte hoy?\n\n" +
                    "🔍 **Buscar productos** - Ej: 'buscar Coca Cola'\n" +
                    "💰 **Consultar precios** - Ej: 'precio de leche'\n" +
                    "🛒 **Ver ofertas** - Ej: 'ofertas de hoy'\n" +
                    "📋 **Lista de compras** - Ej: 'para hacer pizza'\n\n" +
                    "¿Qué te gustaría hacer?";
        } else {
            welcomeMessage = "¡Hola! 👋 Soy tu asistente de **Comparar**.\n\n" +
                    "Puedo ayudarte a encontrar los mejores precios del mercado. ¿Qué necesitas?\n\n" +
                    "🔍 Buscar productos\n" +
                    "💰 Comparar precios\n" +
                    "🛒 Ver ofertas disponibles\n\n" +
                    "¿Por dónde empezamos?";
        }

        response.put("response", welcomeMessage);
        response.put("productos", new ArrayList<>());
        response.put("precios", new ArrayList<>());
        response.put("isWelcome", true);

        return response;
    }

    private String detectIntention(String message) {
        if (message.matches("(?i).*hola.*|.*buenos.*|.*buenas.*|.*inicio.*")) {
            return "SALUDO";
        } else if (message.matches("(?i).*precio.*|.*cuesta.*|.*valor.*|.*cuanto.*|.*cost.*")) {
            return "CONSULTAR_PRECIO";
        } else if (message.matches("(?i).*buscar.*|.*encontrar.*|.*tienen.*|.*hay.*|.*producto.*|.*donde.*")) {
            return "BUSCAR_PRODUCTO";
        } else if (message.matches("(?i).*receta.*|.*cocinar.*|.*hacer.*|.*preparar.*|.*ingrediente.*")) {
            return "RECETA";
        } else if (message.matches("(?i).*gracias.*|.*bye.*|.*chau.*|.*adiós.*|.*hasta.*")) {
            return "DESPEDIDA";
        } else if (message.trim().length() < 3) {
            return "SALUDO"; // Mensajes cortos tratados como saludo
        } else {
            return "BUSCAR_PRODUCTO";
        }
    }

    private List<Producto> buscarProductosInteligente(String message) {
        // Limpiar y extraer palabras clave
        String cleaned = message.replaceAll("(?i)precio de|buscar|quiero|dame|muestra|cuanto cuesta", "").trim();

        if (cleaned.isEmpty()) {
            return new ArrayList<>();
        }

        // Buscar productos similares
        List<Producto> productos = productoRepository.findByDescripcionContainingIgnoreCase(cleaned);

        // Si no encuentra, intentar con palabras individuales
        if (productos.isEmpty()) {
            String[] palabras = cleaned.split("\\s+");
            for (String palabra : palabras) {
                if (palabra.length() > 2) {
                    List<Producto> parcial = productoRepository.findByDescripcionContainingIgnoreCase(palabra);
                    productos.addAll(parcial);
                }
            }
        }

        return productos.stream().distinct().limit(10).toList();
    }

    private List<Map<String, Object>> obtenerPreciosProductos(List<Producto> productos) {
        List<Map<String, Object>> precios = new ArrayList<>();

        for (Producto producto : productos) {
            try {
                // Obtener últimos precios del producto
                List<Object[]> resultados = productoPrecioRepository.findUltimosPreciosByProducto(producto.getIdProducto());

                if (!resultados.isEmpty()) {
                    for (Object[] resultado : resultados) {
                        Map<String, Object> precioInfo = new HashMap<>();
                        precioInfo.put("productoId", producto.getIdProducto());
                        precioInfo.put("productoNombre", producto.getDescripcion());
                        precioInfo.put("precio", resultado[0]);
                        precioInfo.put("sucursal", resultado[1]);
                        precioInfo.put("fecha", resultado[2]);
                        precios.add(precioInfo);
                    }
                }
            } catch (Exception e) {
                // Si hay error, continuar con el siguiente producto
                System.out.println("Error obteniendo precios para producto: " + producto.getIdProducto());
            }
        }

        return precios;
    }

    private String generateResponseByIntention(String intention, String message,
                                               List<Producto> productos,
                                               List<Map<String, Object>> precios,
                                               Usuario usuario) { // Cambiado
        switch (intention) {
            case "SALUDO":
                return getSaludoPersonalizado(usuario); // Cambiado

            case "CONSULTAR_PRECIO":
                if (productos.isEmpty()) {
                    return "No encontré productos para '" + message + "' 😔\n\n¿Podrías intentar con otro nombre? Por ejemplo: 'precio de Coca Cola'";
                } else {
                    return buildPriceResponse(productos, precios, message);
                }

            case "BUSCAR_PRODUCTO":
                if (productos.isEmpty()) {
                    return "No encontré productos relacionados con '" + message + "' 🤔\n\nSugerencias:\n• Revisa la ortografía\n• Usa términos más generales\n• Ejemplo: 'leche' en lugar de 'leche entera'";
                } else {
                    return buildProductResponse(productos, precios, "¡Encontré estos productos para ti! 🎉");
                }

            case "RECETA":
                return "¡Me encanta cocinar! 🍳\n\nPor ahora puedo ayudarte a encontrar los ingredientes. ¿Qué receta tienes en mente?\n\nEjemplo: 'Necesito ingredientes para pizza'";

            case "DESPEDIDA":
                String nombre = usuario != null ? usuario.getNombreUsuario() : "amigo"; // Cambiado
                return "¡Gracias por visitarnos, " + nombre + "! 👋\n\nVuelve pronto si necesitas ayuda con tus compras. ¡Que tengas un excelente día!";

            default:
                return "¿En qué más puedo ayudarte? Puedo:\n• Buscar productos 🔍\n• Consultar precios 💰\n• Ayudar con ingredientes 🛒";
        }
    }

    private String getSaludoPersonalizado(Usuario usuario) { // Cambiado
        if (usuario != null) {
            // Usar nombreUsuario en lugar de nombre
            return "¡Hola " + usuario.getNombreUsuario() + "! 👋\n\n" + // Cambiado
                    "¿En qué puedo ayudarte hoy? Puedo:\n\n" +
                    "🔍 **Buscar productos específicos**\n" +
                    "💰 **Comparar precios entre sucursales**\n" +
                    "🛒 **Armar lista de compras**\n\n" +
                    "¿Qué te gustaría hacer?";
        } else {
            return "¡Hola! 👋 Soy tu asistente de **Comparar**.\n\n" +
                    "Puedo ayudarte a encontrar los mejores precios del mercado. ¿Qué necesitas?\n\n" +
                    "🔍 Buscar productos\n" +
                    "💰 Comparar precios\n" +
                    "🛒 Ver ofertas disponibles\n\n" +
                    "¿Por dónde empezamos?";
        }
    }

    private String buildPriceResponse(List<Producto> productos, List<Map<String, Object>> precios, String query) {
        StringBuilder response = new StringBuilder();
        response.append("💵 **Precios para '").append(query).append("'**\n\n");

        if (precios.isEmpty()) {
            response.append("Encontré estos productos pero no tengo precios actualizados:\n\n");
            for (Producto p : productos) {
                response.append("• ").append(p.getDescripcion());
                if (p.getMarca() != null) response.append(" - ").append(p.getMarca());
                response.append("\n");
            }
            response.append("\n⚠️ Los precios se actualizan diariamente");
        } else {
            // Agrupar precios por producto
            Map<Long, List<Map<String, Object>>> preciosPorProducto = new HashMap<>();
            for (Map<String, Object> precio : precios) {
                Long productId = (Long) precio.get("productoId");
                preciosPorProducto.computeIfAbsent(productId, k -> new ArrayList<>()).add(precio);
            }

            for (Producto producto : productos) {
                if (preciosPorProducto.containsKey(producto.getIdProducto())) {
                    response.append("🛍️ **").append(producto.getDescripcion()).append("**");
                    if (producto.getMarca() != null) response.append(" - ").append(producto.getMarca());
                    response.append("\n");

                    List<Map<String, Object>> preciosProducto = preciosPorProducto.get(producto.getIdProducto());
                    for (Map<String, Object> precio : preciosProducto) {
                        response.append("   💰 $").append(precio.get("precio"))
                                .append(" - ").append(precio.get("sucursal"))
                                .append("\n");
                    }
                    response.append("\n");
                }
            }
        }

        response.append("¿Te interesa algún producto en particular?");
        return response.toString();
    }

    private String buildProductResponse(List<Producto> productos, List<Map<String, Object>> precios, String header) {
        StringBuilder response = new StringBuilder();
        response.append(header).append("\n\n");

        for (int i = 0; i < Math.min(productos.size(), 5); i++) {
            Producto p = productos.get(i);
            response.append("• **").append(p.getDescripcion()).append("**");
            if (p.getMarca() != null && !p.getMarca().isEmpty()) {
                response.append(" - ").append(p.getMarca());
            }

            // Agregar precio si está disponible
            Optional<Map<String, Object>> precio = precios.stream()
                    .filter(pr -> pr.get("productoId").equals(p.getIdProducto()))
                    .findFirst();

            if (precio.isPresent()) {
                response.append(" → 💰 $").append(precio.get().get("precio"));
            }

            response.append("\n");
        }

        if (productos.size() > 5) {
            response.append("\n📋 Y ").append(productos.size() - 5).append(" productos más...");
        }

        response.append("\n\n¿Quieres que busque precios específicos de alguno?");
        return response.toString();
    }
}