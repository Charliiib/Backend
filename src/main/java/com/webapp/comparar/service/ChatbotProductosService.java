package com.webapp.comparar.service;

import com.webapp.comparar.dto.IngredienteEncontrado;
import com.webapp.comparar.model.Producto;
import com.webapp.comparar.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatbotProductosService {

    @Autowired
    private ProductoRepository productoRepository;

    // Sinónimos de ingredientes comunes para mejorar la búsqueda
    private static final Map<String, List<String>> SINONIMOS_INGREDIENTES = Map.ofEntries(
            Map.entry("carne", Arrays.asList("carne", "res", "vacuno", "ternera", "cerdo", "carnes", "carne picada")),
            Map.entry("pollo", Arrays.asList("pollo", "ave", "pechuga", "muslo", "alitas")),
            Map.entry("tomate", Arrays.asList("tomate", "tomates", "jitomate", "pasta de tomate", "salsa de tomate")),
            Map.entry("cebolla", Arrays.asList("cebolla", "cebollas", "cebollita")),
            Map.entry("ajo", Arrays.asList("ajo", "ajos", "diente de ajo", "dientes de ajo")),
            Map.entry("queso", Arrays.asList("queso", "quesos", "parmesano", "mozzarella", "cheddar")),
            Map.entry("leche", Arrays.asList("leche", "crema", "crema de leche", "leche entera")),
            Map.entry("aceite", Arrays.asList("aceite", "aceite de oliva", "aceite vegetal", "oliva")),
            Map.entry("sal", Arrays.asList("sal", "sal marina", "sal gruesa", "sal fina")),
            Map.entry("pimienta", Arrays.asList("pimienta", "pimienta negra", "pimienta blanca")),
            Map.entry("pasta", Arrays.asList("pasta", "fideos", "macarrones", "espagueti", "canelones", "lasaña")),
            Map.entry("harina", Arrays.asList("harina", "harina de trigo", "fécula", "almidón")),
            Map.entry("azúcar", Arrays.asList("azúcar", "azúcar blanco", "azúcar moreno", "stevia")),
            Map.entry("huevo", Arrays.asList("huevo", "huevos", "clara", "yema")),
            Map.entry("mantequilla", Arrays.asList("mantequilla", "margarina", "manteca")),
            Map.entry("perejil", Arrays.asList("perejil", "perejil fresco", "hierbas", "albahaca", "orégano", "romero")),
            Map.entry("zanahoria", Arrays.asList("zanahoria", "zanahorias", "vegetales", "verduras")),
            Map.entry("papa", Arrays.asList("papa", "papas", "patata", "patatas", "tubérculo")),
            Map.entry("champiñón", Arrays.asList("champiñón", "champiñones", "hongos", "setas")),
            Map.entry("espinaca", Arrays.asList("espinaca", "espinacas", "verduras verdes", "acelga"))
    );

    // Lista de palabras que indican cantidades o unidades (para filtrar)
    private static final Set<String> PALABRAS_CANTIDAD = Set.of(
            "ml", "gr", "gramos", "kg", "cucharadas", "cucharaditas", "vasos", "tazas",
            "pizca", "pizcas", "gota", "gotas", "unidad", "unidades", "piezas", "dientes",
            "hojas", "ramas", "cabezas", "ramos", "centímetros", "metros", "litros", "lb", "libra", "libras"
    );

    public List<IngredienteEncontrado> buscarProductosPorReceta(String recetaCompleta) {
        try {
            // 1. Extraer ingredientes de la receta
            List<String> ingredientes = extraerIngredientes(recetaCompleta);

            if (ingredientes.isEmpty()) {
                return new ArrayList<>();
            }

            // 2. Buscar productos para cada ingrediente
            List<IngredienteEncontrado> ingredientesEncontrados = new ArrayList<>();

            for (String ingrediente : ingredientes) {
                List<Producto> productos = buscarProductosPorIngrediente(ingrediente);
                if (!productos.isEmpty()) {
                    ingredientesEncontrados.add(new IngredienteEncontrado(ingrediente, productos));
                }
            }

            return ingredientesEncontrados;

        } catch (Exception e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> extraerIngredientes(String receta) {
        Set<String> ingredientes = new HashSet<>();

        // Buscar secciones de ingredientes en la receta
        Pattern ingredientesPattern = Pattern.compile(
                "(?:\\*{0,2}[📝📋]?\\s*(?:INGREDIENTES?|INSTRUCCIONES?|PASOS?)|" +
                        "INGREDIENTES[🔥:]*|" +
                        "PARA (?:LA|SUS|EL)\\s*(?:Salsa|Preparaci[óo]n|Relleno|Cocción)\\s*:|" +
                        "Secci[óo]n de ingredientes)" +
                        "([\\s\\S]*?)(?=\\n\\s*(?:\\*{0,2}[📋📝]|INSTRUCCIONES?|PASOS?|Preparaci[óo]n|Cocci[óo]n|$))",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = ingredientesPattern.matcher(receta);

        if (matcher.find()) {
            String seccionIngredientes = matcher.group(1);
            ingredientes.addAll(parsearListaIngredientes(seccionIngredientes));
        } else {
            // Si no encuentra sección específica, buscar en toda la receta
            ingredientes.addAll(parsearListaIngredientes(receta));
        }

        return new ArrayList<>(ingredientes);
    }

    private List<String> parsearListaIngredientes(String texto) {
        Set<String> ingredientes = new HashSet<>();

        // Buscar patrones de líneas con ingredientes
        String[] lineas = texto.split("\n");

        for (String linea : lineas) {
            String lineaLimpia = linea.trim();

            // Filtrar líneas que no parecen ingredientes
            if (lineaLimpia.isEmpty() ||
                    lineaLimpia.startsWith("•") ||
                    lineaLimpia.startsWith("**") ||
                    lineaLimpia.contains("tiempo") ||
                    lineaLimpia.contains("cocción") ||
                    lineaLimpia.contains("preparación") ||
                    !lineaLimpia.contains("g") && !lineaLimpia.contains("ml") &&
                            !lineaLimpia.matches(".*\\d.*") &&
                            !lineaLimpia.toLowerCase().contains("cucharada") &&
                            !lineaLimpia.toLowerCase().contains("vaso") &&
                            !lineaLimpia.toLowerCase().contains("taza")) {
                continue;
            }

            // Extraer solo el nombre del ingrediente (sin cantidades)
            String ingrediente = limpiarNombreIngrediente(lineaLimpia);

            if (ingrediente != null && !ingrediente.isEmpty()) {
                ingredientes.add(ingrediente.toLowerCase());
            }
        }

        return new ArrayList<>(ingredientes);
    }

    private String limpiarNombreIngrediente(String linea) {
        // Remover cantidades, unidades y caracteres especiales
        String lineaLimpia = linea.replaceAll("\\*+", "").trim();
        lineaLimpia = lineaLimpia.replaceAll("\\d+[.,]?\\d*\\s*(ml|gr|kg|gramos|cucharad|cucharaditas|vasos|tazas|litros|lb|libra|libras|unidades|unidades|piezas|dientes|hojas|ramas)", "");
        lineaLimpia = lineaLimpia.replaceAll("\\s*[:\\-]\\s*", " ");
        lineaLimpia = lineaLimpia.replaceAll("^.*\\d.*?cup.*?$", "");

        // Extraer el último segmento (el nombre del ingrediente)
        String[] partes = lineaLimpia.split(" ");
        if (partes.length > 0) {
            String posibleIngrediente = partes[partes.length - 1];

            // Validar que no sea solo una cantidad
            if (!posibleIngrediente.matches("\\d+[.,]?\\d*") &&
                    !PALABRAS_CANTIDAD.contains(posibleIngrediente.toLowerCase())) {
                return posibleIngrediente;
            }
        }

        return lineaLimpia;
    }

    public List<Producto> buscarProductosPorIngrediente(String ingrediente) {
        try {
            String ingredienteLimpio = limpiarIngrediente(ingrediente);

            // Buscar por nombre exacto primero
            List<Producto> productos = productoRepository.findByDescripcionContainingIgnoreCase(
                    ingredienteLimpio, PageRequest.of(0, 10)
            );

            // Si no hay resultados, buscar por sinónimos
            if (productos.isEmpty()) {
                productos = buscarPorSinonimos(ingredienteLimpio);
            }

            return productos;

        } catch (Exception e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String limpiarIngrediente(String ingrediente) {
        String limpio = ingrediente.toLowerCase();
        limpio = limpio.replaceAll("grosera|triturad|picad|cortad|sobr|hervid|calent|enfre|sald|piment|aceit", "");
        limpio = limpio.replaceAll("precocid|hierb|fresc|seco|humed", "");
        limpio = limpio.replaceAll("\\s+", " ").trim();
        return limpio;
    }

    private List<Producto> buscarPorSinonimos(String ingrediente) {
        // Buscar en la lista de sinónimos
        for (Map.Entry<String, List<String>> entry : SINONIMOS_INGREDIENTES.entrySet()) {
            if (entry.getValue().contains(ingrediente) || ingrediente.contains(entry.getKey())) {
                List<Producto> productos = productoRepository.findByDescripcionContainingIgnoreCase(
                        entry.getKey(), PageRequest.of(0, 5)
                );
                if (!productos.isEmpty()) {
                    return productos;
                }
            }
        }

        return new ArrayList<>();
    }
}
