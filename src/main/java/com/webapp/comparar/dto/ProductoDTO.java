package com.webapp.comparar.dto;

public class ProductoDTO {
    private Long idProducto;
    private String descripcion;
    private String marca;

    // Constructor vacío
    public ProductoDTO() {
    }

    // Constructor con parámetros
    public ProductoDTO(Long idProducto, String descripcion, String marca) {
        this.idProducto = idProducto;
        this.descripcion = descripcion;
        this.marca = marca;
    }

    // Getters y Setters
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
}