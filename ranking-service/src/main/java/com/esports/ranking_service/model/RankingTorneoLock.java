package com.esports.ranking_service.model;
import jakarta.persistence.*;
@Entity
@Table(name = "ranking_torneo_lock")
public class RankingTorneoLock {
    @Id @Column(name = "torneo_id") private Long torneoId;
    protected RankingTorneoLock() {}
}
