package com.webapp.comparar.repository;

import com.webapp.comparar.model.DireccionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionUsuarioRepository extends JpaRepository<DireccionUsuario, Long> {

    // Encontrar todas las direcciones de un usuario
    List<DireccionUsuario> findByIdUsuarioOrderByIdDireccionUsuarioDesc(Long idUsuario);

    // Encontrar la última dirección usada por un usuario
    @Query("SELECT d FROM DireccionUsuario d WHERE d.idUsuario = :idUsuario ORDER BY d.idDireccionUsuario DESC")
    List<DireccionUsuario> findUltimaDireccionByUsuario(@Param("idUsuario") Long idUsuario);

    // Verificar si ya existe una dirección con el mismo nombre para el usuario
    Optional<DireccionUsuario> findByIdUsuarioAndNombreDireccion(Long idUsuario, String nombreDireccion);
}