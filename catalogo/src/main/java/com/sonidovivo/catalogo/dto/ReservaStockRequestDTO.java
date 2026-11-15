package com.sonidovivo.catalogo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservaStockRequestDTO(
        @NotNull @Positive Integer cantidad) {
}
