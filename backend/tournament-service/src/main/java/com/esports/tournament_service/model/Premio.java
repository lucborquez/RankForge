package com.esports.tournament_service.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "premios", uniqueConstraints = @UniqueConstraint(columnNames = {"torneo_id", "posicion"}))
@Data @NoArgsConstructor
public class Premio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "torneo_id", nullable = false) private Long torneoId;
    @Column(nullable = false) private Integer posicion;
    @Column(nullable = false) private Long participanteId;
    @Column(nullable = false, length = 500) private String descripcion;
}
