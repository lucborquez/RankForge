package com.esports.tournament_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "notificaciones_torneo", indexes = @Index(name = "idx_notificacion_torneo", columnList = "torneo_id,id"))
@Data @NoArgsConstructor
public class NotificacionTorneo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "torneo_id", nullable = false) private Long torneoId;
    @Column(nullable = false, length = 40) private String tipo;
    @Column(nullable = false, length = 700) private String mensaje;
    @Column(nullable = false) private Instant fecha;
}
