package com.esports.tournament_service.dto;
import jakarta.validation.constraints.*;
public record PremioRequestDTO(@NotNull @Positive Integer posicion,
        @NotNull @Positive Long participanteId, @NotBlank @Size(max = 500) String descripcion) {}
