package com.webapp.comparar.dto;

import com.webapp.comparar.model.Producto;
import java.util.List;

public class ChatResponse {
    private String response;
    private List<Producto> productos;
    private String correctedQuery;

    // Constructores
    public ChatResponse() {}

    public ChatResponse(String response, List<Producto> productos, String correctedQuery) {
        this.response = response;
        this.productos = productos;
        this.correctedQuery = correctedQuery;
    }

    // Getters y Setters
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }

    public String getCorrectedQuery() { return correctedQuery; }
    public void setCorrectedQuery(String correctedQuery) { this.correctedQuery = correctedQuery; }
}