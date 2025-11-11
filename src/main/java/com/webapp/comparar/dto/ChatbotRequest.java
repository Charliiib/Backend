package com.webapp.comparar.dto;

public class ChatbotRequest {

    private String mensaje;

    // Constructor vacío
    public ChatbotRequest() {
    }

    // Constructor con parámetros
    public ChatbotRequest(String mensaje) {
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
