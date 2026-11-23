package com.esports.ranking_service.repository;
import com.esports.ranking_service.model.RankingTorneoLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface RankingTorneoLockRepository extends JpaRepository<RankingTorneoLock, Long> {
    @Modifying
    @Query(value = "INSERT INTO ranking_torneo_lock (torneo_id) VALUES (:id) ON DUPLICATE KEY UPDATE torneo_id = :id", nativeQuery = true)
    void bloquear(@Param("id") Long id);
}
