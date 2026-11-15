package com.esports.game_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JuegoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255)
    private String nombre;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 255)
    private String genero;

    @NotBlank(message = "La modalidad es obligatoria")
    @Size(max = 255)
    private String modalidad;

    @NotNull(message = "Los jugadores por equipo son obligatorios")
    @Positive(message = "Debe ser un número positivo")
    private Integer jugadoresPorEquipo;

}
