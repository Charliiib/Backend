package com.webapp.comparar.repository;

import com.webapp.comparar.model.Comercio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ComercioRepository extends JpaRepository<Comercio, Comercio.ComercioId> {

    @Query("SELECT c FROM Comercio c WHERE c.idComercio = :idComercio AND c.idBandera = :idBandera")
    Optional<Comercio> findByIdComercioAndIdBandera(@Param("idComercio") Long idComercio,
                                                    @Param("idBandera") Long idBandera);

    @Query("SELECT c FROM Comercio c WHERE c.comercioBanderaNombre LIKE %:banderaNombre%")
    List<Comercio> findByBanderaNombreContaining(@Param("banderaNombre") String banderaNombre);

    List<Comercio> findByIdComercio(Long idComercio);
}