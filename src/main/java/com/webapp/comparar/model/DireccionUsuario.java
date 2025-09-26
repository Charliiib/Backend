
package com.webapp.comparar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "direcciones")
public class DireccionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion_usuario")
    private Long idDireccionUsuario;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "nombre_direcciones", length = 45)
    private String nombreDireccion;

    @Column(name = "direccion_latitud", precision = 10, scale = 6)
    private BigDecimal latitud;

    @Column(name = "direccion_longitud", precision = 10, scale = 6)
    private BigDecimal longitud;

    // Constructores
    public DireccionUsuario() {
    }

    public DireccionUsuario(Long idUsuario, String nombreDireccion, BigDecimal latitud, BigDecimal longitud) {
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