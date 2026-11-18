package com.sonidovivo.pedidos.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoPagadoEvent(
        String codigoPedido,
        LocalDateTime fechaVenta,
        BigDecimal total,
        int cantidadProductos) {
}
