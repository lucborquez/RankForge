package com.esports.registration_service.repository;

import com.esports.registration_service.model.CupoTorneo;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CupoTorneoRepository extends JpaRepository<CupoTorneo, Long> {
    /** La base de datos mantiene el bloqueo de la fila hasta commit/rollback. */
    @Modifying
    @Query("insert into CupoTorneo (torneoId) values (:torneoId) on conflict (torneoId) do update set torneoId = excluded.torneoId")
    void bloquear(@Param("torneoId") Long torneoId);
}
