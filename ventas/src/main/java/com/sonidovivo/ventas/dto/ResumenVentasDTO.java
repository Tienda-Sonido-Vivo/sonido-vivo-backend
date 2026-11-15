package com.sonidovivo.ventas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumenVentasDTO(
        LocalDateTime desde,
        LocalDateTime hasta,
        long cantidadPedidos,
        long unidadesVendidas,
        BigDecimal totalVentas) {
}
