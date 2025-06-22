package com.webapp.comparar.dto;


import java.util.Date;

public class ProductoPrecioDTO {
    private Integer idComercio;
    private Integer idBandera;
    private Integer idSucursal;
    private Long idProducto;
    private Float productos_precio_lista;
    private Date fecha_carga;

    // Constructor
    public ProductoPrecioDTO(Integer idComercio, Integer idBandera, Integer idSucursal,
                             Long idProducto, Float productos_precio_lista, Date fecha_carga) {
        this.idComercio = idComercio;
        this.idBandera = idBandera;
        this.idSucursal = idSucursal;
        this.idProducto = idProducto;
        this.productos_precio_lista = productos_precio_lista;
        this.fecha_carga = fecha_carga;
    }

    // Getters
    public Integer getIdComercio() {
        return idComercio;
    }

    public Integer getIdBandera() {
        return idBandera;
    }

    public Integer getIdSucursal() {
        return idSucursal;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public Float getProductos_precio_lista() {
        return productos_precio_lista;
    }

    public Date getFecha_carga() {
        return fecha_carga;
    }
}