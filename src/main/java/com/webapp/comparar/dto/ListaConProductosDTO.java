package com.webapp.comparar.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ListaConProductosDTO {
    private Integer idListas;
    private String nombreLista;
    private Integer cantidadProductos;
    private LocalDateTime fechaCreacion;
    private List<ProductoDTO> productos;

    // Constructor vacío (OBLIGATORIO)
    public ListaConProductosDTO() {
    }

    // Constructor con parámetros
    public ListaConProductosDTO(Integer idListas, String nombreLista, Integer cantidadProductos,
                                LocalDateTime fechaCreacion, List<ProductoDTO> productos) {
        this.idListas = idListas;
        this.nombreLista = nombreLista;
        this.cantidadProductos = cantidadProductos;
        this.fechaCreacion = fechaCreacion;
        this.productos = productos;
    }

    // Getters y Setters
    public Integer getIdListas() { return idListas; }
    public void setIdListas(Integer idListas) { this.idListas = idListas; }

    public String getNombreLista() { return nombreLista; }
    public void setNombreLista(String nombreLista) { this.nombreLista = nombreLista; }

    public Integer getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(Integer cantidadProductos) { this.cantidadProductos = cantidadProductos; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<ProductoDTO> getProductos() { return productos; }
    public void setProductos(List<ProductoDTO> productos) { this.productos = productos; }
}