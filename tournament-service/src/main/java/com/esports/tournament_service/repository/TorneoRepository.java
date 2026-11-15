package com.esports.tournament_service.repository;

import com.esports.tournament_service.model.Torneo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {

    // Buscar por juego
    List<Torneo> findByJuegoId(Long juegoId);

    // Buscar por estado (ej: todos los ABIERTOS)
    List<Torneo> findByEstado(String estado);

    // Buscar por juego Y estado
    List<Torneo> findByJuegoIdAndEstado(Long juegoId, String estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Torneo t where t.id = :id")
    Optional<Torneo> buscarParaActualizar(@Param("id") Long id);
}
