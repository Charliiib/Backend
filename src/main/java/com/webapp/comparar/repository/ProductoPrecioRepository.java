package com.webapp.comparar.repository;

import com.webapp.comparar.model.ProductoPrecio;
import com.webapp.comparar.model.ProductoPrecioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoPrecioRepository extends JpaRepository<ProductoPrecio, ProductoPrecioId> {

    @Query("SELECT p FROM ProductoPrecio p WHERE " +
            "p.idProducto = :idProducto AND " +
            "p.idComercio = :idComercio AND " +
            "p.idBandera = :idBandera AND " +
            "p.idSucursal = :idSucursal")
    List<ProductoPrecio> findPreciosByProductoAndSucursal(
            @Param("idProducto") Long idProducto,
            @Param("idComercio") Integer idComercio,
            @Param("idBandera") Integer idBandera,
            @Param("idSucursal") Integer idSucursal);


    @Query("SELECT pp.precioLista, s.sucursalesNombre, pp.fechaCarga " +
            "FROM ProductoPrecio pp " +
            "JOIN Sucursal s ON pp.idComercio = s.idComercio AND pp.idBandera = s.idBandera AND pp.idSucursal = s.idSucursal " +
            "WHERE pp.idProducto = :idProducto " +
            "ORDER BY pp.fechaCarga DESC")
    List<Object[]> findUltimosPreciosByProducto(@Param("idProducto") Long idProducto);
}