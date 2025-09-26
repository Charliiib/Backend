package com.webapp.comparar.repository;


import com.webapp.comparar.model.SucursalFavorita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalFavoritaRepository extends JpaRepository<SucursalFavorita, Long> {

    // Buscar todas las sucursales favoritas de un usuario
    List<SucursalFavorita> findByIdUsuario(Long idUsuario);

    // Verificar si una sucursal ya es favorita de un usuario
    @Query("SELECT sf FROM SucursalFavorita sf WHERE sf.idUsuario = :idUsuario AND sf.idComercio = :idComercio AND sf.idBandera = :idBandera AND sf.idSucursal = :idSucursal")
    Optional<SucursalFavorita> findByUsuarioAndSucursal(
            @Param("idUsuario") Long idUsuario,
            @Param("idComercio") Long idComercio,
            @Param("idBandera") Long idBandera,
            @Param("idSucursal") Long idSucursal);

    // Eliminar una sucursal favorita
    @Modifying
    @Transactional
    @Query("DELETE FROM SucursalFavorita sf WHERE sf.idUsuario = :idUsuario AND sf.idComercio = :idComercio AND sf.idBandera = :idBandera AND sf.idSucursal = :idSucursal")
    int deleteByUsuarioAndSucursal(
            @Param("idUsuario") Long idUsuario,
            @Param("idComercio") Long idComercio,
            @Param("idBandera") Long idBandera,
            @Param("idSucursal") Long idSucursal);

    // Contar sucursales favoritas por usuario
    int countByIdUsuario(Long idUsuario);
}