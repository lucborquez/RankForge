package com.esports.tournament_service.repository;
import com.esports.tournament_service.model.NotificacionTorneo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificacionTorneoRepository extends JpaRepository<NotificacionTorneo, Long> {
    List<NotificacionTorneo> findTop100ByTorneoIdOrderByIdDesc(Long torneoId);
}
