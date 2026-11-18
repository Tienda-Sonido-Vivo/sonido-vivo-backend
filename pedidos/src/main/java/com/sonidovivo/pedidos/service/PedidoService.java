package com.sonidovivo.pedidos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.sonidovivo.pedidos.event.PedidoPagadoEvent;
import com.sonidovivo.pedidos.model.DatosClientePedido;
import com.sonidovivo.pedidos.model.DetallePedido;
import com.sonidovivo.pedidos.model.EstadoPedido;
import com.sonidovivo.pedidos.model.MetodoEntrega;
import com.sonidovivo.pedidos.model.Pedido;
import com.sonidovivo.pedidos.repository.PedidoRepository;

import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class PedidoService {

    private static final BigDecimal COSTO_ENVIO = new BigDecimal("8990");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(
            "dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"));

    private final PedidoRepository pedidoRepository;
    private final RestClient catalogoClient;
    private final RestClient carritoClient;
    private final String internalToken;
    private final ApplicationEventPublisher eventPublisher;

    public PedidoService(

        PedidoRepository pedidoRepository,
        @Qualifier("loadBalancedRestClientBuilder")
        RestClient.Builder restClientBuilder,
        ApplicationEventPublisher eventPublisher,
        @Value("${services.catalogo.url}") String catalogoUrl,
        @Value("${services.carrito.url}") String carritoUrl,
        @Value("${app.internal-token}") String internalToken) {

        /* 
            PedidoRepository pedidoRepository,
            RestClient.Builder restClientBuilder,
            ApplicationEventPublisher eventPublisher,
            @Value("${services.catalogo.url}") String catalogoUrl,
            @Value("${services.carrito.url}") String carritoUrl,
            @Value("${app.internal-token}") String internalToken) {
        */

        this.pedidoRepository = pedidoRepository;
        this.catalogoClient = restClientBuilder.clone().baseUrl(catalogoUrl).build();
        this.carritoClient = restClientBuilder.clone().baseUrl(carritoUrl).build();
        this.internalToken = internalToken;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PedidoResponseDTO crear(PedidoCreateRequestDTO request, String correoAutenticado) {
        List<CarritoItemDTO> itemsCarrito = carritoClient.get()
                .uri("/api/carrito/interno/{correo}", correoAutenticado)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CarritoItemDTO>>() {});

        if (itemsCarrito == null || itemsCarrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        List<ProductoPedido> productos = new ArrayList<>();

        for (CarritoItemDTO item : itemsCarrito) {
            ProductoCatalogoDTO producto = catalogoClient.get()
                    .uri("/api/catalogo/productos/{codigo}", item.codigo())
                    .retrieve()
                    .body(ProductoCatalogoDTO.class);

            if (producto == null) {
                throw new IllegalStateException("El catálogo no devolvió información del producto " + item.codigo() + ".");
            }

            if (item.cantidad() == null || item.cantidad() <= 0 || item.cantidad() > producto.stock()) {
                throw new IllegalStateException(
                        "No hay stock suficiente para el producto " + producto.codigo() + ".");
            }

            productos.add(new ProductoPedido(producto, item.cantidad()));
        }

        Pedido pedido = construirPedido(request, correoAutenticado, productos);
        Pedido guardado = pedidoRepository.save(pedido);
        guardado.setCodigo(String.format("PED-%06d", guardado.getId()));
        guardado = pedidoRepository.save(guardado);

        // El carrito se conserva hasta que el pago se concrete correctamente.
        return aResponse(guardado);
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

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarInterno(String codigo) {
        return aResponse(obtenerPedido(codigo));
    }

    @Transactional
    public PedidoResponseDTO actualizarEstado(String codigo, EstadoPedidoRequestDTO request) {
        Pedido pedido = obtenerPedido(codigo);

        if (!pedido.isPagado()) {
            throw new IllegalStateException("No se puede avanzar el estado operacional de un pedido sin pagar.");
        }

        EstadoPedido nuevoEstado = EstadoPedido.desdeEtiqueta(request.estado());

        if (nuevoEstado.ordinal() <= pedido.getEstado().ordinal()) {
            if (nuevoEstado == pedido.getEstado()) {
                return aResponse(pedido);
            }

            throw new IllegalArgumentException("El estado del pedido solo puede avanzar.");
        }

        if (nuevoEstado.ordinal() != pedido.getEstado().ordinal() + 1) {
            throw new IllegalArgumentException("El estado del pedido debe avanzar una etapa a la vez.");
        }

        pedido.setEstado(nuevoEstado);
        return aResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO marcarPagado(String codigo) {
        Pedido pedido = obtenerPedido(codigo);

        if (pedido.isPagado()) {
            return aResponse(pedido);
        }

        List<Map<String, Object>> productos = pedido.getProductos().stream()
                .map(detalle -> Map.<String, Object>of(
                        "codigo", detalle.getCodigo(),
                        "cantidad", detalle.getCantidad()))
                .toList();

        // Catálogo aplica el descuento de forma atómica e idempotente por código de pedido.
        catalogoClient.post()
                .uri("/api/catalogo/interno/pedidos/{codigo}/descontar-stock", codigo)
                .header("X-Internal-Token", internalToken)
                .body(Map.of("productos", productos))
                .retrieve()
                .toBodilessEntity();

        pedido.setPagado(true);
        pedido.setFechaPago(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        try {
            carritoClient.delete()
                    .uri("/api/carrito/interno/{correo}", pedido.getCorreoCliente())
                    .header("X-Internal-Token", internalToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            // La compra ya está pagada; un fallo al vaciar el carrito no debe revertirla.
        }

        int cantidadProductos = pedido.getProductos().stream()
                .mapToInt(DetallePedido::getCantidad)
                .sum();

        eventPublisher.publishEvent(new PedidoPagadoEvent(
                pedido.getCodigo(),
                pedido.getFechaPago(),
                pedido.getTotal(),
                cantidadProductos));

        return aResponse(pedido);
    }

    private Pedido construirPedido(
            PedidoCreateRequestDTO request,
            String correoAutenticado,
            List<ProductoPedido> productos) {

        BigDecimal subtotal = BigDecimal.ZERO;
        Pedido pedido = new Pedido();
        pedido.setCorreoCliente(correoAutenticado);
        pedido.setFechaRegistro(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setPagado(false);
        pedido.setVentaRegistrada(false);
        pedido.setMetodoEntrega(request.metodoEntrega());
        pedido.setMetodoPago(request.metodoPago());
        pedido.setCliente(construirDatosCliente(request.cliente(), request.metodoEntrega(), correoAutenticado));

        for (ProductoPedido productoPedido : productos) {
            ProductoCatalogoDTO producto = productoPedido.producto();
            BigDecimal precio = producto.precio();
            BigDecimal importe = precio.multiply(BigDecimal.valueOf(productoPedido.cantidad()));
            subtotal = subtotal.add(importe);

            DetallePedido detalle = new DetallePedido();
            detalle.setCodigo(producto.codigo());
            detalle.setNombre(producto.nombre());
            detalle.setPrecio(precio);
            detalle.setCantidad(productoPedido.cantidad());
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

    private record ProductoPedido(ProductoCatalogoDTO producto, int cantidad) {
    }
}
