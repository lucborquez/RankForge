package com.esports.match_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

/** Una fila por torneo para serializar altas entre todas las instancias del servicio. */
@Entity
@Table(name = "partida_torneo_locks")
public class PartidaTorneoLock {
    @Id
    @Column(name = "torneo_id")
    private Long torneoId;
    protected PartidaTorneoLock() {}
}
