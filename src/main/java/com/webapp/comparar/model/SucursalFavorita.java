package com.webapp.comparar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sucursales_favoritas")
public class SucursalFavorita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorita")
    private Long idFavorita;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_comercio", nullable = false)
    private Long idComercio;

    @Column(name = "id_bandera", nullable = false)
    private Long idBandera;

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(name = "sucursal_nombre", length = 255)
    private String sucursalNombre;

    @Column(name = "comercio_nombre", length = 255)
    private String comercioNombre;

    @Column(name = "barrio_nombre", length = 100)
    private String barrioNombre;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;

    // Constructor vacío
    public SucursalFavorita() {
        this.fechaAgregado = LocalDateTime.now();
    }

    // Constructor con parámetros
    public SucursalFavorita(Long idUsuario, Long idComercio, Long idBandera, Long idSucursal,
                            String sucursalNombre, String comercioNombre, String barrioNombre,
                            Double latitud, Double longitud) {
        this();
        this.idUsuario = idUsuario;
        this.idComercio = idComercio;
        this.idBandera = idBandera;
        this.idSucursal = idSucursal;
        this.sucursalNombre = sucursalNombre;
        this.comercioNombre = comercioNombre;
        this.barrioNombre = barrioNombre;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Getters y Setters
    public Long getIdFavorita() {
        return idFavorita;
    }

    public void setIdFavorita(Long idFavorita) {
        this.idFavorita = idFavorita;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdComercio() {
        return idComercio;
    }

    public void setIdComercio(Long idComercio) {
        this.idComercio = idComercio;
    }

    public Long getIdBandera() {
        return idBandera;
    }

    public void setIdBandera(Long idBandera) {
        this.idBandera = idBandera;
    }

    public Long getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(Long idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
    }

    public String getComercioNombre() {
        return comercioNombre;
    }

    public void setComercioNombre(String comercioNombre) {
        this.comercioNombre = comercioNombre;
    }

    public String getBarrioNombre() {
        return barrioNombre;
    }

    public void setBarrioNombre(String barrioNombre) {
        this.barrioNombre = barrioNombre;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
}