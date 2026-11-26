package com.esports.resultservice.repository;
import com.esports.resultservice.model.ResultadoPartidaLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ResultadoPartidaLockRepository extends JpaRepository<ResultadoPartidaLock, Long> {
    @Modifying
    @Query("insert into ResultadoPartidaLock (partidaId) values (:id) on conflict (partidaId) do update set partidaId = excluded.partidaId")
    void bloquear(@Param("id") Long id);
}
