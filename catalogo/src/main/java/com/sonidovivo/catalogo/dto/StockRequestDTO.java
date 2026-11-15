package com.sonidovivo.catalogo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockRequestDTO(
        @NotNull @PositiveOrZero Integer stock) {
}
