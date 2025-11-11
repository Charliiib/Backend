package com.webapp.comparar.dto;

import com.webapp.comparar.model.Producto;

import java.util.List;

public class IngredienteEncontrado {

    private String nombreIngrediente; // Nombre del ingrediente
    private List<ProductoDTO> productos; // Productos encontrados
    private int cantidadProductos; // Cantidad de productos encontrados
    private boolean encontrado; // Si se encontraron productos

    // Constructor vacío
    public IngredienteEncontrado() {
    }

    // Constructor con parámetros
    public IngredienteEncontrado(String nombreIngrediente, List<Producto> productos) {
        this.nombreIngrediente = nombreIngrediente;
        this.productos = productos.stream()
                .map(p -> new ProductoDTO(p.getIdProducto(), p.getDescripcion(), p.getMarca()))
                .collect(java.util.stream.Collectors.toList());
        this.cantidadProductos = this.productos.size();
        this.encontrado = !this.productos.isEmpty();
    }

    // Constructor simplificado
    public IngredienteEncontrado(String nombreIngrediente, List<ProductoDTO> productos, boolean encontrado) {
        this.nombreIngrediente = nombreIngrediente;
        this.productos = productos;
        this.cantidadProductos = productos != null ? productos.size() : 0;
        this.encontrado = encontrado;
    }

    // Getters y Setters
    public String getNombreIngrediente() {
        return nombreIngrediente;
    }

    public void setNombreIngrediente(String nombreIngrediente) {
        this.nombreIngrediente = nombreIngrediente;
    }

    public List<ProductoDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoDTO> productos) {
        this.productos = productos;
        this.cantidadProductos = productos != null ? productos.size() : 0;
        this.encontrado = productos != null && !productos.isEmpty();
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    public boolean isEncontrado() {
        return encontrado;
    }

    public void setEncontrado(boolean encontrado) {
        this.encontrado = encontrado;
    }

    // Método de conveniencia
    public String getResumen() {
        if (encontrado) {
            return String.format("📦 %s: %d producto(s) disponible(s)",
                    nombreIngrediente, cantidadProductos);
        } else {
            return String.format("❌ %s: No hay productos en stock", nombreIngrediente);
        }
    }
}
