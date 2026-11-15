package com.esports.game_service.repository;

import com.esports.game_service.model.Juego;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

//JpaRepository nos da save(), findById(), findAll(), deleteById()
@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long>{

    //Busca juegos activos
    List<Juego> findByEstadoTrue();

    //Verifica si ya existe un juego con ese nombre
    Optional<Juego> findByNombre(String nombre);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from Juego j where j.id = :id")
    Optional<Juego> buscarParaActualizar(@Param("id") Long id);

}
