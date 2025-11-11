package com.webapp.comparar.dto;

import java.util.List;

public class ChatbotRecetaResponse {

    private String respuesta; // La receta original
    private List<IngredienteEncontrado> productosEncontrados; // Productos de la base de datos
    private boolean productosEncontradosBool; // Si se encontraron productos
    private String mensajeAdicional; // Mensaje opcional para el usuario

    // Constructor vacío
    public ChatbotRecetaResponse() {
    }

    // Constructor con parámetros
    public ChatbotRecetaResponse(String respuesta) {
        this.respuesta = respuesta;
        this.productosEncontradosBool = false;
    }

    public ChatbotRecetaResponse(String respuesta, List<IngredienteEncontrado> productosEncontrados) {
        this.respuesta = respuesta;
        this.productosEncontrados = productosEncontrados;
        this.productosEncontradosBool = productosEncontrados != null && !productosEncontrados.isEmpty();

        if (this.productosEncontradosBool) {
            this.mensajeAdicional = generarMensajeProductos(productosEncontrados);
        }
    }

    private String generarMensajeProductos(List<IngredienteEncontrado> productos) {
        int totalProductos = productos.stream()
                .mapToInt(p -> p.getProductos().size())
                .sum();

        return String.format("📦 También encontré %d productos relacionados con los ingredientes en nuestra base de datos.", totalProductos);
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
        this.productosEncontradosBool = productosEncontrados != null && !productosEncontrados.isEmpty();
    }

    public boolean isProductosEncontradosBool() {
        return productosEncontradosBool;
    }

    public void setProductosEncontradosBool(boolean productosEncontradosBool) {
        this.productosEncontradosBool = productosEncontradosBool;
    }

    public String getMensajeAdicional() {
        return mensajeAdicional;
    }

    public void setMensajeAdicional(String mensajeAdicional) {
        this.mensajeAdicional = mensajeAdicional;
    }
}
