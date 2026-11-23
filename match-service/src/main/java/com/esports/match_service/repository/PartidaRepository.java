package com.esports.match_service.repository;

import com.esports.match_service.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    @org.springframework.data.jpa.repository.Query("select p.torneoId from Partida p where p.id = :id")
    java.util.Optional<Long> obtenerTorneoId(@org.springframework.data.repository.query.Param("id") Long id);

    List<Partida> findByTorneoId(Long torneoId);
    List<Partida> findByTorneoIdAndRonda(Long torneoId, String ronda);
    List<Partida> findByEstado(String estado);

    // Verificar si ya existe enfrentamiento entre los mismos participantes en la misma ronda
    boolean existsByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(
            Long torneoId, Long participanteAId, Long participanteBId, String ronda);
}
