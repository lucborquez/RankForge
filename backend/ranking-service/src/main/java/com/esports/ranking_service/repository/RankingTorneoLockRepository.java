package com.esports.ranking_service.repository;
import com.esports.ranking_service.model.RankingTorneoLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface RankingTorneoLockRepository extends JpaRepository<RankingTorneoLock, Long> {
    @Modifying
    @Query("insert into RankingTorneoLock (torneoId) values (:id) on conflict (torneoId) do update set torneoId = excluded.torneoId")
    void bloquear(@Param("id") Long id);
}
