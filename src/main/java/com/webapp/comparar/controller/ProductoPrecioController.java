package com.webapp.comparar.controller;


import com.webapp.comparar.dto.ProductoPrecioDTO;
import com.webapp.comparar.model.ProductoPrecio;
import com.webapp.comparar.repository.ProductoPrecioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoPrecioController {

    @Autowired
    private ProductoPrecioRepository productoPrecioRepository;

    @GetMapping("/precios")
    public ResponseEntity<List<ProductoPrecioDTO>> getPrecios(
            @RequestParam Long id_producto,
            @RequestParam Integer id_comercio,
            @RequestParam Integer id_bandera,
            @RequestParam Integer id_sucursal) {

        List<ProductoPrecio> precios = productoPrecioRepository.findPreciosByProductoAndSucursal(
                id_producto, id_comercio, id_bandera, id_sucursal);

        List<ProductoPrecioDTO> dtos = precios.stream()
                .map(p -> new ProductoPrecioDTO(
                        p.getIdComercio(),
                        p.getIdBandera(),
                        p.getIdSucursal(),
                        p.getIdProducto(),
                        p.getPrecioLista(),
                        p.getFechaCarga()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    }