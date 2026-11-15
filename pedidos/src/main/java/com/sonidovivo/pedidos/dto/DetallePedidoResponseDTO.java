package com.sonidovivo.pedidos.dto;

import java.math.BigDecimal;

public record DetallePedidoResponseDTO(
        String codigo,
        String nombre,
        BigDecimal precio,
        Integer cantidad) {
}
