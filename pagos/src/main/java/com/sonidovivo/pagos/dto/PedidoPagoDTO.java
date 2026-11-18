package com.sonidovivo.pagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Contrato local de lectura para el endpoint interno de Pedidos.
 * Se declara en Pagos para mantener independientes los módulos de negocio.
 */
public record PedidoPagoDTO(
        String codigo,
        String fecha,
        LocalDateTime fechaISO,
        String estado,
        boolean pagado,
        List<Map<String, Object>> productos,
        BigDecimal subtotal,
        BigDecimal envio,
        BigDecimal iva,
        BigDecimal total,
        String metodoEntrega,
        String metodoPago,
        Map<String, Object> cliente,
        String correoCliente) {
}
