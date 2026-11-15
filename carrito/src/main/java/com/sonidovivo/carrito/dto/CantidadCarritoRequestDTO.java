package com.sonidovivo.carrito.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CantidadCarritoRequestDTO(
        @NotNull @Positive Integer cantidad) {
}
