package com.webapp.comparar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "productos_descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "productos_cantidad_presentacion")
    private Float cantidadPresentacion;

    @Column(name = "productos_unidad_medida_presentacion", length = 10)
    private String unidadMedidaPresentacion;

    @Column(name = "productos_marca", length = 45)
    private String marca;

    // Constructor vacío
    public Producto() {
    }

    // Constructor con parámetros
    public Producto(String descripcion, String marca) {
        this.descripcion = descripcion;
        this.marca = marca;
    }

    // Getters y Setters
    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Float getCantidadPresentacion() {
        return cantidadPresentacion;
    }

    public void setCantidadPresentacion(Float cantidadPresentacion) {
        this.cantidadPresentacion = cantidadPresentacion;
    }

    public String getUnidadMedidaPresentacion() {
        return unidadMedidaPresentacion;
    }

    public void setUnidadMedidaPresentacion(String unidadMedidaPresentacion) {
        this.unidadMedidaPresentacion = unidadMedidaPresentacion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }
}