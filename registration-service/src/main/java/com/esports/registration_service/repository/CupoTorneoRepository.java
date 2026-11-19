package com.esports.registration_service.repository;

import com.esports.registration_service.model.CupoTorneo;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CupoTorneoRepository extends JpaRepository<CupoTorneo, Long> {
    /** MySQL/InnoDB mantiene el bloqueo de la fila hasta commit/rollback. */
    @Modifying
    @Query(value = "INSERT INTO inscripcion_cupos (torneo_id) VALUES (:torneoId) ON DUPLICATE KEY UPDATE torneo_id = :torneoId", nativeQuery = true)
    void bloquear(@Param("torneoId") Long torneoId);
}
