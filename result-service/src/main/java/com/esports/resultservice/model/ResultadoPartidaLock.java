package com.esports.resultservice.model;
import jakarta.persistence.*;
@Entity
@Table(name = "resultado_partida_lock")
public class ResultadoPartidaLock {
    @Id @Column(name = "partida_id") private Long partidaId;
    protected ResultadoPartidaLock() {}
}
