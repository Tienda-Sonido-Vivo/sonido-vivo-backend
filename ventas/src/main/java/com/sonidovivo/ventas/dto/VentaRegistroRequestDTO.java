package com.sonidovivo.ventas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VentaRegistroRequestDTO(
        @NotBlank @Size(max = 20) String codigoPedido,
        @NotNull LocalDateTime fecha,
        @NotNull @Positive BigDecimal total,
        @NotNull @PositiveOrZero Integer cantidadProductos) {
}
