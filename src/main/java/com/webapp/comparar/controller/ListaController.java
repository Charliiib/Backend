package com.webapp.comparar.controller;

import com.webapp.comparar.model.Favorito;
import com.webapp.comparar.model.Lista;
import com.webapp.comparar.model.Producto;
import com.webapp.comparar.repository.FavoritoRepository;
import com.webapp.comparar.repository.ListaRepository;
import com.webapp.comparar.repository.ProductoRepository;
import com.webapp.comparar.dto.ListaConProductosDTO;
import com.webapp.comparar.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/listas")
@CrossOrigin(origins = "http://localhost:3000")
public class ListaController {

    @Autowired
    private ListaRepository listaRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Obtener todas las listas de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ListaConProductosDTO>> obtenerListasPorUsuario(@PathVariable Integer idUsuario) {
        List<Lista> listas = listaRepository.findByIdUsuario(idUsuario);

        List<ListaConProductosDTO> listasDTO = listas.stream().map(lista -> {
            ListaConProductosDTO dto = new ListaConProductosDTO();
            dto.setIdListas(lista.getIdListas());
            dto.setNombreLista(lista.getNombreLista());
            dto.setFechaCreacion(lista.getFechaCreacion());

            // Contar productos en la lista
            Integer cantidad = favoritoRepository.findProductosIdsByLista(lista.getIdListas()).size();
            dto.setCantidadProductos(cantidad);

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(listasDTO);
    }

    // Obtener productos de una lista específica - CORREGIDO
    @GetMapping("/{idLista}/productos")
    public ResponseEntity<List<ProductoDTO>> obtenerProductosDeLista(@PathVariable Integer idLista) {
        List<Long> productosIds = favoritoRepository.findProductosIdsByLista(idLista);

        if (productosIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Producto> productos = productoRepository.findByIdProductoIn(productosIds);

        List<ProductoDTO> productosDTO = productos.stream().map(producto -> {
            ProductoDTO dto = new ProductoDTO();
            dto.setIdProducto(producto.getIdProducto());
            dto.setDescripcion(producto.getDescripcion()); // CORREGIDO

            // Verificar si la marca existe antes de setearla
            if (producto.getMarca() != null) {
                dto.setMarca(producto.getMarca());
            } else {
                dto.setMarca("Sin marca"); // Valor por defecto
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(productosDTO);
    }

    // Crear nueva lista
    @PostMapping
    public ResponseEntity<Lista> crearLista(@RequestBody Lista lista) {
        if (lista.getFechaCreacion() == null) {
            lista.setFechaCreacion(java.time.LocalDateTime.now());
        }
        lista.setFechaActualizacion(java.time.LocalDateTime.now());

        Lista nuevaLista = listaRepository.save(lista);
        return ResponseEntity.ok(nuevaLista);
    }

    // Agregar producto a lista
    @PostMapping("/{idLista}/productos/{idProducto}")
    public ResponseEntity<?> agregarProductoALista(@PathVariable Integer idLista, @PathVariable Long idProducto) {
        if (!listaRepository.existsById(idLista)) {
            return ResponseEntity.badRequest().body("Lista no encontrada");
        }

        if (favoritoRepository.existsByListaIdListasAndIdProducto(idLista, idProducto)) {
            return ResponseEntity.badRequest().body("El producto ya está en la lista");
        }

        Lista lista = listaRepository.findById(idLista)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        Favorito favorito = new Favorito();
        favorito.setLista(lista);
        favorito.setIdProducto(idProducto);

        favoritoRepository.save(favorito);
        return ResponseEntity.ok("Producto agregado a la lista");
    }

    @DeleteMapping("/{idLista}/productos/{idProducto}")
    public ResponseEntity<?> eliminarProductoDeLista(@PathVariable Integer idLista, @PathVariable Long idProducto) {
        try {
            if (!favoritoRepository.existsByListaIdListasAndIdProducto(idLista, idProducto)) {
                return ResponseEntity.status(404).body("Producto no encontrado en la lista");
            }

            favoritoRepository.deleteByListaIdListasAndIdProducto(idLista, idProducto);
            return ResponseEntity.ok("Producto eliminado de la lista");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}