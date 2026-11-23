package com.esports.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El email, rol y estado nunca se toman del formulario de perfil. */
public record PerfilRequestDTO(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(min = 3, max = 40) String apodo) {}
