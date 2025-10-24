package com.webapp.comparar.repository;

import com.webapp.comparar.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Sucursal.SucursalId> {

    // Ejemplo: Buscar sucursales por barrio
     List<Sucursal> findByBarrioIdBarrios(Long idBarrio);
     List<Sucursal> findBySucursalesNombreContainingIgnoreCase(String nombre);
}