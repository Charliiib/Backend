package com.webapp.comparar.model;

import java.io.Serializable;
import java.util.Objects;

public class ProductoPrecioId implements Serializable {
    private Integer idComercio;
    private Integer idBandera;
    private Integer idSucursal;
    private Long idProducto;

    // Constructor vacío
    public ProductoPrecioId() {
    }

    // Constructor con parámetros
    public ProductoPrecioId(Integer idComercio, Integer idBandera, Integer idSucursal, Long idProducto) {
        this.idComercio = idComercio;
        this.idBandera = idBandera;
        this.idSucursal = idSucursal;
        this.idProducto = idProducto;
    }

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductoPrecioId that = (ProductoPrecioId) o;
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