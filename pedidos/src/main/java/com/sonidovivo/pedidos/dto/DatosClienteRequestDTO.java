package com.sonidovivo.pedidos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record DatosClienteRequestDTO(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Pattern(regexp = "^(?:\\+?56\\s?)?9\\s?\\d{4}\\s?\\d{4}$") @Size(max = 25) String telefono,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(max = 100) String region,
        @NotBlank @Size(max = 100) String comuna,
        @Size(max = 180) String direccion,
        @Size(max = 80) String numeroVivienda,
        @Size(max = 20) String codigoPostal) {
}
