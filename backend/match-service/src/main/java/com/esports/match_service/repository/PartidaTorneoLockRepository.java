package com.esports.match_service.repository;

import com.esports.match_service.model.PartidaTorneoLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PartidaTorneoLockRepository extends JpaRepository<PartidaTorneoLock, Long> {
    /** MySQL/InnoDB mantiene el bloqueo de la fila hasta commit/rollback. */
    @Modifying
    @Query(value = "INSERT INTO partida_torneo_locks (torneo_id) VALUES (:torneoId) ON DUPLICATE KEY UPDATE torneo_id = :torneoId", nativeQuery = true)
    void bloquear(@Param("torneoId") Long torneoId);
}
