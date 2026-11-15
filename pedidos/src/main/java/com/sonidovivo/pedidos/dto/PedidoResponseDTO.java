package com.sonidovivo.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        String codigo,
        String fecha,
        LocalDateTime fechaISO,
        String estado,
        boolean pagado,
        List<DetallePedidoResponseDTO> productos,
        BigDecimal subtotal,
        BigDecimal envio,
        BigDecimal iva,
        BigDecimal total,
        String metodoEntrega,
        String metodoPago,
        DatosClienteResponseDTO cliente,
        String correoCliente) {
}
