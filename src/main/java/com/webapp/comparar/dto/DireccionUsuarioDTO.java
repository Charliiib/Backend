package com.webapp.comparar.dto;

import java.math.BigDecimal;

public class DireccionUsuarioDTO {
    private Long idDireccionUsuario;
    private Long idUsuario;
    private String nombreDireccion;
    private BigDecimal latitud;
    private BigDecimal longitud;

    // Constructores
    public DireccionUsuarioDTO() {
    }

    public DireccionUsuarioDTO(Long idUsuario, String nombreDireccion, BigDecimal latitud, BigDecimal longitud) {
        this.idUsuario = idUsuario;
        this.nombreDireccion = nombreDireccion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Getters y Setters
    public Long getIdDireccionUsuario() {
        return idDireccionUsuario;
    }

    public void setIdDireccionUsuario(Long idDireccionUsuario) {
        this.idDireccionUsuario = idDireccionUsuario;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreDireccion() {
        return nombreDireccion;
    }

    public void setNombreDireccion(String nombreDireccion) {
        this.nombreDireccion = nombreDireccion;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }
}