package com.webapp.comparar.controller;

import com.webapp.comparar.model.Producto;
import com.webapp.comparar.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.List;


@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarProductos(
            @RequestParam String termino,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Lógica para verificar autenticación
        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        // Limitar a 10 resultados si no está autenticado
        int maxLimit = isAuthenticated ? 50 : 10; // 10 para anónimos, 50 para autenticados
        int finalLimit = Math.min(limit, maxLimit);


        // Dividir el término de búsqueda en palabras individuales
        String[] terminos = termino.toLowerCase().split("\\s+");
        // Construir la consulta dinámica
        Specification<Producto> spec = Specification.where(null);

        for (String term : terminos) {
            if (!term.isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("descripcion")), "%" + term + "%"));
            }
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<Producto> productos = productoRepository.findAll(spec, pageable).getContent();

        return ResponseEntity.ok(productos);
    }
}