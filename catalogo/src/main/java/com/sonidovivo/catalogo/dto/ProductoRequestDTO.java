package com.sonidovivo.catalogo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequestDTO(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 100) String categoria,
        @NotBlank @Size(max = 80) String marca,
        @NotBlank @Size(max = 80) String modelo,
        @NotNull @DecimalMin("1.00") BigDecimal precio,
        @NotNull @PositiveOrZero Integer stock,
        @NotBlank @Size(max = 255) String imagen,
        @NotBlank @Size(max = 500) String descripcion) {
}
