package com.esports.match_service.repository;

import com.esports.match_service.model.PartidaTorneoLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PartidaTorneoLockRepository extends JpaRepository<PartidaTorneoLock, Long> {
    /** La base de datos mantiene el bloqueo de la fila hasta commit/rollback. */
    @Modifying
    @Query("insert into PartidaTorneoLock (torneoId) values (:torneoId) on conflict (torneoId) do update set torneoId = excluded.torneoId")
    void bloquear(@Param("torneoId") Long torneoId);
}
