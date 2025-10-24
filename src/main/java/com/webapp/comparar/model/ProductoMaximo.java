package com.webapp.comparar.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "productosmaximos")
@IdClass(ProductoPrecioId.class) // Reutilizamos la misma clase ID
public class ProductoMaximo {

    @Id
    @Column(name = "id_comercio")
    private Integer idComercio;

    @Id
    @Column(name = "id_bandera")
    private Integer idBandera;

    @Id
    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Id
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "productos_precio_lista")
    private Float precioLista;

    @Column(name = "fecha_carga")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;

    // Getters y Setters (igual que ProductoPrecio)
    public Integer getIdComercio() {
        return idComercio;
    }

    public void setIdComercio(Integer idComercio) {
        this.idComercio = idComercio;
    }

    public Integer getIdBandera() {
        return idBandera;
    }

    public void setIdBandera(Integer idBandera) {
        this.idBandera = idBandera;
    }

    public Integer getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(Integer idSucursal) {
        this.idSucursal = idSucursal;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Float getPrecioLista() {
        return precioLista;
    }

    public void setPrecioLista(Float precioLista) {
        this.precioLista = precioLista;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductoMaximo that = (ProductoMaximo) o;
        return Objects.equals(idComercio, that.idComercio) &&
                Objects.equals(idBandera, that.idBandera) &&
                Objects.equals(idSucursal, that.idSucursal) &&
                Objects.equals(idProducto, that.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idComercio, idBandera, idSucursal, idProducto);
    }
}