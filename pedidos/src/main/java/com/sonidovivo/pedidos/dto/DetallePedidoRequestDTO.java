package com.sonidovivo.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DetallePedidoRequestDTO(
        @NotBlank @Size(max = 20) String codigo,
        @NotNull @Positive Integer cantidad) {
}
