package com.sonidovivo.pedidos.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

import com.sonidovivo.pedidos.event.PedidoPagadoEvent;
import com.sonidovivo.pedidos.model.Pedido;
import com.sonidovivo.pedidos.repository.PedidoRepository;

import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class VentaNotificacionListener {

    private static final Logger logger = LoggerFactory.getLogger(VentaNotificacionListener.class);

    private final PedidoRepository pedidoRepository;
    private final RestClient ventasClient;
    private final String internalToken;

    public VentaNotificacionListener(
        PedidoRepository pedidoRepository,
        @Qualifier("loadBalancedRestClientBuilder")
        RestClient.Builder restClientBuilder,
        @Value("${services.ventas.url}") String ventasUrl,
        @Value("${app.internal-token}") String internalToken) {

    this.pedidoRepository = pedidoRepository;
    this.ventasClient = restClientBuilder.baseUrl(ventasUrl).build();
    this.internalToken = internalToken;
}

    /* 
    public VentaNotificacionListener(
            PedidoRepository pedidoRepository,
            RestClient.Builder restClientBuilder,
            @Value("${services.ventas.url}") String ventasUrl,
            @Value("${app.internal-token}") String internalToken) {
        this.pedidoRepository = pedidoRepository;
        this.ventasClient = restClientBuilder.baseUrl(ventasUrl).build();
        this.internalToken = internalToken;
    }

    */

    @Async("ventasTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void registrarVentaDespuesDeConfirmarPago(PedidoPagadoEvent evento) {
        notificarVenta(
                evento.codigoPedido(),
                evento.fechaVenta(),
                evento.total(),
                evento.cantidadProductos());
    }

    /**
     * Reintenta únicamente las ventas cuyo pedido ya está pagado y cuya notificación
     * anterior no pudo completarse. No consulta Pedidos ni Ventas continuamente.
     */
    @Scheduled(fixedDelayString = "${services.ventas.retry-ms:300000}")
    public void reintentarVentasPendientes() {
        List<Pedido> pendientes = pedidoRepository.findByPagadoTrueAndVentaRegistradaFalse();

        for (Pedido pedido : pendientes) {
            notificarVenta(
                    pedido.getCodigo(),
                    pedido.getFechaPago(),
                    pedido.getTotal(),
                    pedido.getProductos().stream().mapToInt(detalle -> detalle.getCantidad()).sum());
        }
    }

    private void notificarVenta(String codigoPedido, java.time.LocalDateTime fecha,
            java.math.BigDecimal total, int cantidadProductos) {
        try {
            ventasClient.post()
                    .uri("/api/ventas/interno")
                    .header("X-Internal-Token", internalToken)
                    .body(Map.of(
                            "codigoPedido", codigoPedido,
                            "fecha", fecha,
                            "total", total,
                            "cantidadProductos", cantidadProductos))
                    .retrieve()
                    .toBodilessEntity();

            pedidoRepository.findByCodigo(codigoPedido).ifPresent(pedido -> {
                if (!pedido.isVentaRegistrada()) {
                    pedido.setVentaRegistrada(true);
                    pedidoRepository.save(pedido);
                }
            });
        } catch (RuntimeException exception) {
            logger.warn("El pedido {} está pagado, pero Ventas no está disponible. Se reintentará más adelante.",
                    codigoPedido, exception);
        }
    }
}
