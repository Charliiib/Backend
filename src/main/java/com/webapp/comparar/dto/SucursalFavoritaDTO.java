package com.webapp.comparar.dto;


import java.time.LocalDateTime;

public class SucursalFavoritaDTO {

    private Long idFavorita;
    private Long idUsuario;
    private Long idComercio;
    private Long idBandera;
    private Long idSucursal;
    private String sucursalNombre;
    private String comercioNombre;
    private String barrioNombre;
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaAgregado;

    // Constructores
    public SucursalFavoritaDTO() {}

    public SucursalFavoritaDTO(Long idUsuario, Long idComercio, Long idBandera, Long idSucursal,
                               String sucursalNombre, String comercioNombre, String barrioNombre,
                               Double latitud, Double longitud) {
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