package com.sonidovivo.pagos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.sonidovivo.pagos.dto.PagoRegistroInternoDTO;
import com.sonidovivo.pagos.dto.PagoResponseDTO;
import com.sonidovivo.pagos.model.EstadoPago;
import com.sonidovivo.pagos.model.MetodoPago;
import com.sonidovivo.pagos.model.Pago;
import com.sonidovivo.pagos.repository.PagoRepository;
import com.sonidovivo.pagos.dto.PedidoPagoDTO;

@Service
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final RestClient pedidosClient;
    private final String internalToken;

    public PagoService(
            PagoRepository pagoRepository,
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder,
            @Value("${services.pedidos.url}") String pedidosUrl,
            @Value("${app.internal-token}") String internalToken) {

        this.pagoRepository = pagoRepository;
        this.pedidosClient = restClientBuilder.baseUrl(pedidosUrl).build();
        this.internalToken = internalToken;
    }

    @Transactional
    public PagoResponseDTO registrarPendiente(PagoRegistroInternoDTO request) {
        Pago pago = pagoRepository.findByCodigoPedido(request.codigoPedido())
                .orElseGet(Pago::new);

        if (pago.getEstado() == EstadoPago.COMPLETADO) {
            return aResponse(pago);
        }

        pago.setCodigoPedido(request.codigoPedido());
        pago.setCorreoCliente(request.correoCliente().trim().toLowerCase(Locale.ROOT));
        pago.setMonto(request.monto());
        pago.setMetodoPago(request.metodoPago());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado(EstadoPago.PENDIENTE);

        return aResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponseDTO procesarPago(String codigoPedido, String correoAutenticado) {
        PedidoPagoDTO pedido = pedidosClient.get()
                .uri("/api/pedidos/interno/{codigo}", codigoPedido)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .body(PedidoPagoDTO.class);

        if (pedido == null) {
            throw new IllegalStateException("Pedidos no devolvió información para procesar el pago.");
        }

        if (!pedido.correoCliente().equalsIgnoreCase(correoAutenticado)) {
            throw new AccessDeniedException("No tienes permiso para pagar este pedido.");
        }

        Pago pago = pagoRepository.findByCodigoPedido(codigoPedido).orElseGet(Pago::new);
        pago.setCodigoPedido(codigoPedido);
        pago.setCorreoCliente(pedido.correoCliente().trim().toLowerCase(Locale.ROOT));
        pago.setMonto(pedido.total());
        pago.setMetodoPago(MetodoPago.valueOf(pedido.metodoPago().toUpperCase(Locale.ROOT)));

        if (pedido.pagado()) {
            pago.setEstado(EstadoPago.COMPLETADO);
            pago.setFechaPago(LocalDateTime.now());
            return aResponse(pagoRepository.save(pago));
        }

        // En esta versión académica se registra el método elegido; no existe una
        // conexión con una pasarela bancaria ni se reciben datos de tarjetas.
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());
        pago = pagoRepository.save(pago);

        try {
            pedidosClient.patch()
                    .uri("/api/pedidos/interno/{codigo}/pagado", codigoPedido)
                    .header("X-Internal-Token", internalToken)
                    .retrieve()
                    .toBodilessEntity();

            pago.setEstado(EstadoPago.COMPLETADO);
            pago.setFechaPago(LocalDateTime.now());
        } catch (RuntimeException exception) {
            // Un fallo en Catálogo o Pedidos deja el pago sin completar. El cliente
            // puede volver a intentarlo; Pedidos y Catálogo son idempotentes.
            pago.setEstado(EstadoPago.FALLIDO);
            pago.setFechaPago(LocalDateTime.now());
            logger.warn("No se pudo completar el pago del pedido {}.", codigoPedido, exception);
        }

        return aResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponseDTO confirmarPago(String codigoPedido) {
        PedidoPagoDTO pedido = pedidosClient.get()
                .uri("/api/pedidos/interno/{codigo}", codigoPedido)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .body(PedidoPagoDTO.class);

        if (pedido == null) {
            throw new IllegalStateException("Pedidos no devolvió información para confirmar el pago.");
        }

        Pago pago = pagoRepository.findByCodigoPedido(codigoPedido).orElseGet(Pago::new);
        pago.setCodigoPedido(codigoPedido);
        pago.setCorreoCliente(pedido.correoCliente().trim().toLowerCase(Locale.ROOT));
        pago.setMonto(pedido.total());
        pago.setMetodoPago(MetodoPago.valueOf(pedido.metodoPago().toUpperCase(Locale.ROOT)));

        if (pedido.pagado()) {
            pago.setEstado(EstadoPago.COMPLETADO);
            pago.setFechaPago(LocalDateTime.now());
            return aResponse(pagoRepository.save(pago));
        }

        try {
            pedidosClient.patch()
                    .uri("/api/pedidos/interno/{codigo}/pagado", codigoPedido)
                    .header("X-Internal-Token", internalToken)
                    .retrieve()
                    .toBodilessEntity();

            pago.setEstado(EstadoPago.COMPLETADO);
            pago.setFechaPago(LocalDateTime.now());
        } catch (RuntimeException exception) {
            pago.setEstado(EstadoPago.FALLIDO);
            pago.setFechaPago(LocalDateTime.now());
            pagoRepository.save(pago);
            logger.warn("No se pudo confirmar el pago del pedido {}.", codigoPedido, exception);
            throw exception;
        }

        return aResponse(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarTodos() {
        return pagoRepository.findAllByOrderByFechaPagoDesc()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarCliente(String correo) {
        return pagoRepository.findByCorreoClienteIgnoreCaseOrderByFechaPagoDesc(correo)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagoResponseDTO buscar(String codigoPedido, String correo, boolean personalAutorizado) {
        Pago pago = obtenerPago(codigoPedido);

        if (!personalAutorizado && !pago.getCorreoCliente().equalsIgnoreCase(correo)) {
            throw new AccessDeniedException("No tienes permiso para consultar este pago.");
        }

        return aResponse(pago);
    }

    private Pago obtenerPago(String codigoPedido) {
        return pagoRepository.findByCodigoPedido(codigoPedido)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el pago del pedido."));
    }

    private PagoResponseDTO aResponse(Pago pago) {
        return new PagoResponseDTO(
                pago.getId(),
                pago.getCodigoPedido(),
                pago.getCorreoCliente(),
                pago.getMonto(),
                pago.getMetodoPago().name().toLowerCase(Locale.ROOT),
                pago.getFechaPago(),
                pago.getEstado().name());
    }
}
