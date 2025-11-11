package com.webapp.comparar.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ListaDetallesDTO {
    private Integer idLista;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private Integer cantidadProductos;
    private Double totalActual;
    private List<ProductoListaDTO> productos;

    // Constructores
    public ListaDetallesDTO() {}

    public ListaDetallesDTO(Integer idLista, String nombre, LocalDateTime fechaCreacion, Integer cantidadProductos, Double totalActual, List<ProductoListaDTO> productos) {
        this.idLista = idLista;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.cantidadProductos = cantidadProductos;
        this.totalActual = totalActual;
        this.productos = productos;
    }

    // Getters y Setters
    public Integer getIdLista() { return idLista; }
    public void setIdLista(Integer idLista) { this.idLista = idLista; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(Integer cantidadProductos) { this.cantidadProductos = cantidadProductos; }

    public Double getTotalActual() { return totalActual; }
    public void setTotalActual(Double totalActual) { this.totalActual = totalActual; }

    public List<ProductoListaDTO> getProductos() { return productos; }
    public void setProductos(List<ProductoListaDTO> productos) { this.productos = productos; }
}