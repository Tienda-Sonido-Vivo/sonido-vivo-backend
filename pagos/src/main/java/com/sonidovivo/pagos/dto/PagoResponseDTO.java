package com.sonidovivo.pagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponseDTO(
        Long id,
        String codigoPedido,
        String correoCliente,
        BigDecimal monto,
        String metodoPago,
        LocalDateTime fechaPago,
        String estado) {
}
