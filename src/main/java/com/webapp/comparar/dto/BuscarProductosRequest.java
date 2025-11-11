package com.webapp.comparar.dto;

public class BuscarProductosRequest {
    private String receta;

    public BuscarProductosRequest() {
    }

    public BuscarProductosRequest(String receta) {
        this.receta = receta;
    }

    public String getReceta() {
        return receta;
    }

    public void setReceta(String receta) {
        this.receta = receta;
    }
}