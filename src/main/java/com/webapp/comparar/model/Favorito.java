package com.webapp.comparar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
public class Favorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProductoFavorito;

    @ManyToOne
    @JoinColumn(name = "id_listas", nullable = false)
    private Lista lista;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;

    // Relación con Producto (si existe la entidad)
    @Transient
    private Producto producto;

    // Constructores
    public Favorito() {
        this.fechaAgregado = LocalDateTime.now();
    }

    public Integer getIdProductoFavorito() {
        return idProductoFavorito;
    }

    public void setIdProductoFavorito(Integer idProductoFavorito) {
        this.idProductoFavorito = idProductoFavorito;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Lista getLista() {
        return lista;
    }

    public void setLista(Lista lista) {
        this.lista = lista;
    }
}