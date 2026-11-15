package com.sonidovivo.pagos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.sonidovivo.pagos.dto.PagoRegistroInternoDTO;
import com.sonidovivo.pagos.dto.PagoResponseDTO;
import com.sonidovivo.pagos.model.EstadoPago;
import com.sonidovivo.pagos.model.Pago;
import com.sonidovivo.pagos.repository.PagoRepository;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final RestClient pedidosClient;
    private final String internalToken;

    public PagoService(
            PagoRepository pagoRepository,
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
        pago.setCorreoCliente(request.correoCliente().trim().toLowerCase());
        pago.setMonto(request.monto());
        pago.setMetodoPago(request.metodoPago());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado(EstadoPago.PENDIENTE);

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

    @Transactional
    public PagoResponseDTO confirmar(String codigoPedido) {
        Pago pago = obtenerPago(codigoPedido);

        if (pago.getEstado() == EstadoPago.COMPLETADO) {
            return aResponse(pago);
        }

        pedidosClient.patch()
                .uri("/api/pedidos/interno/{codigo}/pagado", codigoPedido)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .toBodilessEntity();

        pago.setEstado(EstadoPago.COMPLETADO);
        pago.setFechaPago(LocalDateTime.now());

        return aResponse(pagoRepository.save(pago));
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
                pago.getMetodoPago().name().toLowerCase(),
                pago.getFechaPago(),
                pago.getEstado().name());
    }
}
