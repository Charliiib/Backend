package com.webapp.comparar.repository;

import com.webapp.comparar.model.Lista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ListaRepository extends JpaRepository<Lista, Integer> {

    List<Lista> findByIdUsuario(Integer idUsuario);

    @Query("SELECT l FROM Lista l WHERE l.idUsuario = :idUsuario ORDER BY l.fechaCreacion DESC")
    List<Lista> findByUsuarioOrderByFecha(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT COUNT(f) FROM Favorito f WHERE f.lista.idListas = :idLista")
    Integer countProductosEnLista(@Param("idLista") Integer idLista);
}