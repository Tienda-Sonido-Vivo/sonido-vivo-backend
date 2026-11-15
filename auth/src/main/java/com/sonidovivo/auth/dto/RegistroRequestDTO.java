package com.sonidovivo.auth.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record RegistroRequestDTO(
        @NotBlank @Size(max = 15) String run,
        @NotBlank @Size(min = 2, max = 80) String nombre,
        @NotBlank @Size(min = 2, max = 100) String apellidos,
        @NotBlank @Email @Size(max = 150) String email,
        @PastOrPresent LocalDate fechaNacimiento,
        @NotBlank @Size(max = 100) String region,
        @NotBlank @Size(max = 100) String comuna,
        @NotBlank @Size(max = 180) String direccion,
        @NotBlank @Size(min = 8, max = 72) String clave) {
}
