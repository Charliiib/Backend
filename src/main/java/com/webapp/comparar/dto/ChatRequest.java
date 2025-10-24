package com.webapp.comparar.dto;

public class ChatRequest {
    private String message;

    // Constructor vacío
    public ChatRequest() {}

    // Constructor con parámetros
    public ChatRequest(String message) {
        this.message = message;
    }

    // Getters y Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}