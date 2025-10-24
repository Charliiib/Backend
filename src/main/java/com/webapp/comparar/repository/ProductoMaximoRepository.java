package com.webapp.comparar.repository;

import com.webapp.comparar.model.ProductoMaximo;
import com.webapp.comparar.model.ProductoPrecioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoMaximoRepository extends JpaRepository<ProductoMaximo, ProductoPrecioId> {

    @Query("SELECT pm FROM ProductoMaximo pm WHERE " +
            "pm.idProducto = :idProducto AND " +
            "pm.idComercio = :idComercio AND " +
            "pm.idBandera = :idBandera")
    List<ProductoMaximo> findByProductoAndComercioAndBandera(
            @Param("idProducto") Long idProducto,
            @Param("idComercio") Integer idComercio,
            @Param("idBandera") Integer idBandera);

    @Query("SELECT pm FROM ProductoMaximo pm WHERE " +
            "pm.idProducto = :idProducto")
    List<ProductoMaximo> findByProducto(@Param("idProducto") Long idProducto);
}