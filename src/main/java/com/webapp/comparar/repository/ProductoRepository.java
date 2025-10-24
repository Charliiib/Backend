package com.webapp.comparar.repository;

import com.webapp.comparar.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ProductoRepository extends
        JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    List<Producto> findByDescripcionContainingIgnoreCase(String termino, Pageable pageable);

    // Método para buscar productos por lista de IDs
    @Query("SELECT p FROM Producto p WHERE p.idProducto IN :ids")
    List<Producto> findByIdProductoIn(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Producto> searchByDescription(@Param("term") String term);

    List<Producto> findByDescripcionContainingIgnoreCase(String productName);
}