package com.webapp.comparar.service;

import com.webapp.comparar.dto.ProductoPrecioDTO;
import com.webapp.comparar.model.ProductoMaximo;
import com.webapp.comparar.model.ProductoPrecio;
import com.webapp.comparar.repository.ProductoMaximoRepository;
import com.webapp.comparar.repository.ProductoPrecioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoPrecioRepository productoPrecioRepository;

    @Autowired
    private ProductoMaximoRepository productoMaximoRepository;

    public List<ProductoPrecioDTO> buscarPreciosConRespaldo(
            Long idProducto,
            Integer idComercio,
            Integer idBandera,
            Integer idSucursal) {

        List<ProductoPrecioDTO> resultados = new ArrayList<>();

        // 1. Buscar primero en la sucursal específica
        List<ProductoPrecio> preciosSucursal = productoPrecioRepository.findPreciosByProductoAndSucursal(
                idProducto, idComercio, idBandera, idSucursal);

        if (!preciosSucursal.isEmpty()) {
            // Si hay resultados en la sucursal específica, los retornamos
            for (ProductoPrecio precio : preciosSucursal) {
                resultados.add(convertToDTO(precio));
            }
            return resultados;
        }

        // 2. Si no hay resultados, buscar en productosmaximos para el mismo comercio y bandera
        List<ProductoMaximo> preciosMaximos = productoMaximoRepository.findByProductoAndComercioAndBandera(
                idProducto, idComercio, idBandera);

        if (!preciosMaximos.isEmpty()) {
            // Convertir los resultados de productosmaximos a DTO
            for (ProductoMaximo precioMaximo : preciosMaximos) {
                resultados.add(convertToDTO(precioMaximo));
            }
            return resultados;
        }

        // 3. Si no hay resultados en el mismo comercio y bandera, devolver lista vacía
        // Esto evita que productos de un comercio aparezcan en otros comercios
        return resultados;
    }

    private ProductoPrecioDTO convertToDTO(ProductoPrecio productoPrecio) {
        return new ProductoPrecioDTO(
                productoPrecio.getIdComercio(),
                productoPrecio.getIdBandera(),
                productoPrecio.getIdSucursal(),
                productoPrecio.getIdProducto(),
                productoPrecio.getPrecioLista(),
                productoPrecio.getFechaCarga()
        );
    }

    private ProductoPrecioDTO convertToDTO(ProductoMaximo productoMaximo) {
        return new ProductoPrecioDTO(
                productoMaximo.getIdComercio(),
                productoMaximo.getIdBandera(),
                productoMaximo.getIdSucursal(),
                productoMaximo.getIdProducto(),
                productoMaximo.getPrecioLista(),
                productoMaximo.getFechaCarga()
        );
    }
}