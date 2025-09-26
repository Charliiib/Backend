package com.webapp.comparar.repository;

import com.webapp.comparar.model.Favorito;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Integer> {

    List<Favorito> findByListaIdListas(Integer idLista);

    Optional<Favorito> findByListaIdListasAndIdProducto(Integer idLista, Long idProducto);

    @Query("SELECT f.idProducto FROM Favorito f WHERE f.lista.idListas = :idLista")
    List<Long> findProductosIdsByLista(@Param("idLista") Integer idLista);

    @Modifying
    @Query("DELETE FROM Favorito f WHERE f.lista.idListas = :idLista AND f.idProducto = :idProducto")
    void eliminarProductoDeLista(@Param("idLista") Integer idLista, @Param("idProducto") Long idProducto);

    boolean existsByListaIdListasAndIdProducto(Integer idLista, Long idProducto);

    @Transactional
    void deleteByListaIdListasAndIdProducto(Integer idLista, Long idProducto);

    @Transactional
    void deleteByListaIdListas(Integer idLista);
}