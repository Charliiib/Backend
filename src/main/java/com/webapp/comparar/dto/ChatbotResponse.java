package com.webapp.comparar.dto;

import java.util.List;

public class ChatbotResponse {

    private String respuesta;
    private List<IngredienteEncontrado> productosEncontrados;
    private boolean tieneProductos;

    // Constructor vacío
    public ChatbotResponse() {
    }

    // Constructor simple (para compatibilidad)
    public ChatbotResponse(String respuesta) {
        this.respuesta = respuesta;
        this.tieneProductos = false;
    }

    // Constructor con productos
    public ChatbotResponse(String respuesta, List<IngredienteEncontrado> productos) {
        this.respuesta = respuesta;
        this.productosEncontrados = productos;
        this.tieneProductos = productos != null && !productos.isEmpty();
    }

    // Getters y Setters
    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public List<IngredienteEncontrado> getProductosEncontrados() {
        return productosEncontrados;
    }

    public void setProductosEncontrados(List<IngredienteEncontrado> productosEncontrados) {
        this.productosEncontrados = productosEncontrados;
        this.tieneProductos = productosEncontrados != null && !productosEncontrados.isEmpty();
    }

    public boolean isTieneProductos() {
        return tieneProductos;
    }

    public void setTieneProductos(boolean tieneProductos) {
        this.tieneProductos = tieneProductos;
    }
}
