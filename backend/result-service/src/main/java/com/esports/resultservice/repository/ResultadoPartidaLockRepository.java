package com.esports.resultservice.repository;
import com.esports.resultservice.model.ResultadoPartidaLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ResultadoPartidaLockRepository extends JpaRepository<ResultadoPartidaLock, Long> {
    @Modifying
    @Query(value = "INSERT INTO resultado_partida_lock (partida_id) VALUES (:id) ON DUPLICATE KEY UPDATE partida_id = :id", nativeQuery = true)
    void bloquear(@Param("id") Long id);
}
