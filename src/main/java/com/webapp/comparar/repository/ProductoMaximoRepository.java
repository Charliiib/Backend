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


    @Query("SELECT pm FROM ProductoMaximo pm WHERE pm.idProducto = :idProducto ORDER BY pm.precioLista ASC")
    List<ProductoMaximo> findByProductoOrderByPrecioAsc(@Param("idProducto") Long idProducto);

    @Query("SELECT pm FROM ProductoMaximo pm WHERE pm.idProducto = :idProducto AND pm.precioLista = " +
            "(SELECT MIN(pm2.precioLista) FROM ProductoMaximo pm2 WHERE pm2.idProducto = :idProducto)")
    List<ProductoMaximo> findPrecioMinimoByProducto(@Param("idProducto") Long idProducto);

    @Query("SELECT pm FROM ProductoMaximo pm WHERE pm.idProducto = :idProducto AND pm.precioLista = " +
            "(SELECT MAX(pm2.precioLista) FROM ProductoMaximo pm2 WHERE pm2.idProducto = :idProducto)")
    List<ProductoMaximo> findPrecioMaximoByProducto(@Param("idProducto") Long idProducto);

    // Para análisis por comercio/bandera
    @Query("SELECT pm.idComercio, pm.idBandera, AVG(pm.precioLista), COUNT(pm) " +
            "FROM ProductoMaximo pm WHERE pm.idProducto = :idProducto " +
            "GROUP BY pm.idComercio, pm.idBandera")
    List<Object[]> findPrecioPromedioPorComercio(@Param("idProducto") Long idProducto);
}