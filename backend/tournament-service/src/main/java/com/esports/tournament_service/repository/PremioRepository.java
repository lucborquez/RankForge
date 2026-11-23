package com.esports.tournament_service.repository;
import com.esports.tournament_service.model.Premio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PremioRepository extends JpaRepository<Premio, Long> {
    List<Premio> findByTorneoIdOrderByPosicionAsc(Long torneoId);
    boolean existsByTorneoIdAndPosicion(Long torneoId, Integer posicion);
}
