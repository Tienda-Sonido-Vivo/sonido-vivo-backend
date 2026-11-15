package com.sonidovivo.pedidos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import com.sonidovivo.pedidos.dto.DatosClienteRequestDTO;
import com.sonidovivo.pedidos.dto.DatosClienteResponseDTO;
import com.sonidovivo.pedidos.dto.CarritoItemDTO;
import com.sonidovivo.pedidos.dto.DetallePedidoResponseDTO;
import com.sonidovivo.pedidos.dto.EstadoPedidoRequestDTO;
import com.sonidovivo.pedidos.dto.PedidoCreateRequestDTO;
import com.sonidovivo.pedidos.dto.PedidoResponseDTO;
import com.sonidovivo.pedidos.dto.ProductoCatalogoDTO;
import com.sonidovivo.pedidos.model.DatosClientePedido;
import com.sonidovivo.pedidos.model.DetallePedido;
import com.sonidovivo.pedidos.model.EstadoPedido;
import com.sonidovivo.pedidos.model.MetodoEntrega;
import com.sonidovivo.pedidos.model.Pedido;
import com.sonidovivo.pedidos.repository.PedidoRepository;

@Service
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private static final BigDecimal COSTO_ENVIO = new BigDecimal("8990");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(
            "dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"));

    private final PedidoRepository pedidoRepository;
    private final RestClient catalogoClient;
    private final RestClient carritoClient;
    private final RestClient pagosClient;
    private final RestClient ventasClient;
    private final String internalToken;

    public PedidoService(
            PedidoRepository pedidoRepository,
            RestClient.Builder restClientBuilder,
            @Value("${services.catalogo.url}") String catalogoUrl,
            @Value("${services.carrito.url}") String carritoUrl,
            @Value("${services.pagos.url}") String pagosUrl,
            @Value("${services.ventas.url}") String ventasUrl,
            @Value("${app.internal-token}") String internalToken) {

        this.pedidoRepository = pedidoRepository;
        this.catalogoClient = restClientBuilder.clone().baseUrl(catalogoUrl).build();
        this.carritoClient = restClientBuilder.clone().baseUrl(carritoUrl).build();
        this.pagosClient = restClientBuilder.clone().baseUrl(pagosUrl).build();
        this.ventasClient = restClientBuilder.clone().baseUrl(ventasUrl).build();
        this.internalToken = internalToken;
    }

    @Transactional
    public PedidoResponseDTO crear(PedidoCreateRequestDTO request, String correoAutenticado) {
        List<ProductoReservado> reservados = new ArrayList<>();

        try {
            List<CarritoItemDTO> itemsCarrito = carritoClient.get()
                    .uri("/api/carrito/interno/{correo}", correoAutenticado)
                    .header("X-Internal-Token", internalToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CarritoItemDTO>>() {});

            if (itemsCarrito == null || itemsCarrito.isEmpty()) {
                throw new IllegalStateException("El carrito está vacío.");
            }

            for (CarritoItemDTO item : itemsCarrito) {
                ProductoCatalogoDTO producto = catalogoClient.post()
                        .uri("/api/catalogo/interno/productos/{codigo}/reservar", item.codigo())
                        .header("X-Internal-Token", internalToken)
                        .body(Map.of("cantidad", item.cantidad()))
                        .retrieve()
                        .body(ProductoCatalogoDTO.class);

                if (producto == null) {
                    throw new IllegalStateException("El catálogo no devolvió información del producto.");
                }

                reservados.add(new ProductoReservado(producto, item.cantidad()));
            }

            Pedido pedido = construirPedido(request, correoAutenticado, reservados);
            Pedido guardado = pedidoRepository.save(pedido);

            guardado.setCodigo(String.format("PED-%06d", guardado.getId()));
            guardado = pedidoRepository.save(guardado);

            if (registrarPagoPendiente(guardado)) {
                guardado.setPagoRegistrado(true);
                guardado = pedidoRepository.save(guardado);
            }

            try {
                carritoClient.delete()
                        .uri("/api/carrito/interno/{correo}", correoAutenticado)
                        .header("X-Internal-Token", internalToken)
                        .retrieve()
                        .toBodilessEntity();
            } catch (RuntimeException exception) {
                logger.error("El pedido {} se creó, pero no se pudo vaciar el carrito.", guardado.getCodigo(), exception);
            }

            return aResponse(guardado);
        } catch (RuntimeException exception) {
            liberarReservas(reservados);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaRegistroDesc()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarDelCliente(String correo) {
        return pedidoRepository.findByCorreoClienteOrderByFechaRegistroDesc(correo)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscar(String codigo, String correo, boolean personalAutorizado) {
        Pedido pedido = obtenerPedido(codigo);

        if (!personalAutorizado && !pedido.getCorreoCliente().equalsIgnoreCase(correo)) {
            throw new AccessDeniedException("No tienes permiso para consultar este pedido.");
        }

        return aResponse(pedido);
    }

    @Transactional
    public PedidoResponseDTO actualizarEstado(String codigo, EstadoPedidoRequestDTO request) {
        Pedido pedido = obtenerPedido(codigo);
        EstadoPedido nuevoEstado = EstadoPedido.desdeEtiqueta(request.estado());

        if (nuevoEstado.ordinal() <= pedido.getEstado().ordinal()) {
            if (nuevoEstado == pedido.getEstado()) {
                return aResponse(pedido);
            }

            throw new IllegalArgumentException("El estado del pedido solo puede avanzar.");
        }

        pedido.setEstado(nuevoEstado);

        return aResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO marcarPagado(String codigo) {
        Pedido pedido = obtenerPedido(codigo);

        if (!pedido.isPagado()) {
            pedido.setPagado(true);
            pedido = pedidoRepository.save(pedido);
            if (registrarVenta(pedido)) {
                pedido.setVentaRegistrada(true);
                pedido = pedidoRepository.save(pedido);
            }
        }

        return aResponse(pedido);
    }

    private Pedido construirPedido(
            PedidoCreateRequestDTO request,
            String correoAutenticado,
            List<ProductoReservado> reservados) {

        BigDecimal subtotal = BigDecimal.ZERO;
        Pedido pedido = new Pedido();
        pedido.setCorreoCliente(correoAutenticado);
        pedido.setFechaRegistro(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setPagado(false);
        pedido.setVentaRegistrada(false);
        pedido.setPagoRegistrado(false);
        pedido.setMetodoEntrega(request.metodoEntrega());
        pedido.setMetodoPago(request.metodoPago());
        pedido.setCliente(construirDatosCliente(request.cliente(), request.metodoEntrega(), correoAutenticado));

        for (ProductoReservado reservado : reservados) {
            ProductoCatalogoDTO producto = reservado.producto();
            BigDecimal precio = producto.precio();
            BigDecimal importe = precio.multiply(BigDecimal.valueOf(reservado.cantidad()));

            subtotal = subtotal.add(importe);

            DetallePedido detalle = new DetallePedido();
            detalle.setCodigo(producto.codigo());
            detalle.setNombre(producto.nombre());
            detalle.setPrecio(precio);
            detalle.setCantidad(reservado.cantidad());

            pedido.agregarProducto(detalle);
        }

        BigDecimal envio = request.metodoEntrega() == MetodoEntrega.DESPACHO
                ? COSTO_ENVIO
                : BigDecimal.ZERO;

        BigDecimal iva = subtotal.multiply(new BigDecimal("19"))
                .divide(new BigDecimal("119"), 0, RoundingMode.HALF_UP);

        pedido.setSubtotal(subtotal);
        pedido.setEnvio(envio);
        pedido.setIva(iva);
        pedido.setTotal(subtotal.add(envio));

        return pedido;
    }

    private DatosClientePedido construirDatosCliente(
            DatosClienteRequestDTO request,
            MetodoEntrega metodoEntrega,
            String correoAutenticado) {

        if (metodoEntrega == MetodoEntrega.DESPACHO
                && (request.direccion() == null || request.direccion().isBlank()
                || request.numeroVivienda() == null || request.numeroVivienda().isBlank())) {
            throw new IllegalArgumentException("La dirección y el número de vivienda son obligatorios para el despacho.");
        }

        DatosClientePedido cliente = new DatosClientePedido();
        cliente.setNombre(request.nombre().trim());
        cliente.setApellidos(request.apellidos().trim());
        cliente.setTelefono(request.telefono().trim());
        cliente.setCorreo(correoAutenticado);
        cliente.setRegion(request.region().trim());
        cliente.setComuna(request.comuna().trim());
        cliente.setDireccion(request.direccion() == null ? "" : request.direccion().trim());
        cliente.setNumeroVivienda(request.numeroVivienda() == null ? "" : request.numeroVivienda().trim());
        cliente.setCodigoPostal(request.codigoPostal() == null ? "" : request.codigoPostal().trim());

        return cliente;
    }

    private void liberarReservas(List<ProductoReservado> reservados) {
        for (ProductoReservado reservado : reservados) {
            try {
                catalogoClient.post()
                        .uri("/api/catalogo/interno/productos/{codigo}/liberar", reservado.producto().codigo())
                        .header("X-Internal-Token", internalToken)
                        .body(Map.of("cantidad", reservado.cantidad()))
                        .retrieve()
                        .toBodilessEntity();
            } catch (RuntimeException exception) {
                logger.error("No se pudo liberar la reserva del producto {}", reservado.producto().codigo(), exception);
            }
        }
    }

    @Scheduled(fixedDelayString = "${services.pagos.retry-ms:60000}")
    @Transactional
    public void sincronizarPagosPendientes() {
        List<Pedido> pendientes = pedidoRepository.findByPagoRegistradoFalse();

        for (Pedido pedido : pendientes) {
            if (registrarPagoPendiente(pedido)) {
                pedido.setPagoRegistrado(true);
                pedidoRepository.save(pedido);
            }
        }
    }

    @Scheduled(fixedDelayString = "${services.ventas.retry-ms:60000}")
    @Transactional
    public void sincronizarVentasPendientes() {
        List<Pedido> pendientes = pedidoRepository.findByPagadoTrueAndVentaRegistradaFalse();

        for (Pedido pedido : pendientes) {
            if (registrarVenta(pedido)) {
                pedido.setVentaRegistrada(true);
                pedidoRepository.save(pedido);
            }
        }
    }

    private boolean registrarPagoPendiente(Pedido pedido) {
        try {
            pagosClient.post()
                    .uri("/api/pagos/interno")
                    .header("X-Internal-Token", internalToken)
                    .body(Map.of(
                            "codigoPedido", pedido.getCodigo(),
                            "correoCliente", pedido.getCorreoCliente(),
                            "monto", pedido.getTotal(),
                            "metodoPago", pedido.getMetodoPago().name()))
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (RuntimeException exception) {
            logger.error("No se pudo registrar el intento de pago del pedido {}.", pedido.getCodigo(), exception);
            return false;
        }
    }

    private boolean registrarVenta(Pedido pedido) {
        try {
            ventasClient.post()
                    .uri("/api/ventas/interno")
                    .header("X-Internal-Token", internalToken)
                    .body(Map.of(
                            "codigoPedido", pedido.getCodigo(),
                            "fecha", pedido.getFechaRegistro(),
                            "total", pedido.getTotal(),
                            "cantidadProductos", pedido.getProductos().stream()
                                    .mapToInt(DetallePedido::getCantidad)
                                    .sum()))
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (RuntimeException exception) {
            logger.error("El pedido {} quedó pagado, pero la venta no pudo sincronizarse.", pedido.getCodigo(), exception);
            return false;
        }
    }

    private Pedido obtenerPedido(String codigo) {
        return pedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el pedido solicitado."));
    }

    private PedidoResponseDTO aResponse(Pedido pedido) {
        List<DetallePedidoResponseDTO> productos = pedido.getProductos()
                .stream()
                .map(detalle -> new DetallePedidoResponseDTO(
                        detalle.getCodigo(),
                        detalle.getNombre(),
                        detalle.getPrecio(),
                        detalle.getCantidad()))
                .toList();

        DatosClientePedido cliente = pedido.getCliente();
        DatosClienteResponseDTO datosCliente = new DatosClienteResponseDTO(
                cliente.getNombre(),
                cliente.getApellidos(),
                cliente.getTelefono(),
                cliente.getCorreo(),
                cliente.getRegion(),
                cliente.getComuna(),
                cliente.getDireccion(),
                cliente.getNumeroVivienda(),
                cliente.getCodigoPostal());

        return new PedidoResponseDTO(
                pedido.getCodigo(),
                pedido.getFechaRegistro().format(FORMATO_FECHA),
                pedido.getFechaRegistro(),
                pedido.getEstado().etiqueta(),
                pedido.isPagado(),
                productos,
                pedido.getSubtotal(),
                pedido.getEnvio(),
                pedido.getIva(),
                pedido.getTotal(),
                pedido.getMetodoEntrega().name().toLowerCase(Locale.ROOT),
                pedido.getMetodoPago().name().toLowerCase(Locale.ROOT),
                datosCliente,
                pedido.getCorreoCliente());
    }

    private record ProductoReservado(ProductoCatalogoDTO producto, int cantidad) {
    }

}
