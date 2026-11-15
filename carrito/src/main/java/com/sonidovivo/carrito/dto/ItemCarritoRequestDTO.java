package com.sonidovivo.carrito.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ItemCarritoRequestDTO(
        @NotBlank @Size(max = 20) String codigo,
        @NotNull @Positive Integer cantidad) {
}
