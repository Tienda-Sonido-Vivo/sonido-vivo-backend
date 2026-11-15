package com.sonidovivo.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

public record EstadoPedidoRequestDTO(
        @NotBlank String estado) {
}
