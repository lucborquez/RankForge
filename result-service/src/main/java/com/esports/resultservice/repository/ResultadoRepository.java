package com.esports.resultservice.repository;

import com.esports.resultservice.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from Resultado r where r.id = :id")
    java.util.Optional<Resultado> buscarParaModificar(@org.springframework.data.repository.query.Param("id") Long id);
    List<Resultado> findByPartidaId(Long partidaId);
}
