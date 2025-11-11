package com.webapp.comparar.dto;

import java.time.LocalDateTime;

public class ProductoListaDTO {
    private Long idProducto;
    private String descripcion;
    private String marca;
    private String presentacion;
    private Double precioActual;
    private LocalDateTime fechaAgregado;

    // Constructores
    public ProductoListaDTO() {}

    public ProductoListaDTO(Long idProducto, String descripcion, String marca, String presentacion, Double precioActual, LocalDateTime fechaAgregado) {
        this.idProducto = idProducto;
        this.descripcion = descripcion;
        this.marca = marca;
        this.presentacion = presentacion;
        this.precioActual = precioActual;
        this.fechaAgregado = fechaAgregado;
    }

    // Getters y Setters
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }

    public Double getPrecioActual() { return precioActual; }
    public void setPrecioActual(Double precioActual) { this.precioActual = precioActual; }

    public LocalDateTime getFechaAgregado() { return fechaAgregado; }
    public void setFechaAgregado(LocalDateTime fechaAgregado) { this.fechaAgregado = fechaAgregado; }
}