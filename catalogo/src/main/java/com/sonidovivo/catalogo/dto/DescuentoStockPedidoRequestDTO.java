package com.sonidovivo.catalogo.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record DescuentoStockPedidoRequestDTO(
        @NotEmpty List<@Valid DetalleStockPedidoDTO> productos) {
}
