package com.sonidovivo.carrito.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.sonidovivo.carrito.dto.ItemCarritoDTO;
import com.sonidovivo.carrito.dto.ProductoCatalogoDTO;
import com.sonidovivo.carrito.dto.ItemCarritoRequestDTO;
import com.sonidovivo.carrito.model.Carrito;
import com.sonidovivo.carrito.model.DetalleCarrito;
import com.sonidovivo.carrito.repository.CarritoRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final RestClient catalogoClient;

    public CarritoService(
            CarritoRepository carritoRepository,
            RestClient.Builder restClientBuilder,
            @Value("${services.catalogo.url}") String catalogoUrl) {

        this.carritoRepository = carritoRepository;
        this.catalogoClient = restClientBuilder.baseUrl(catalogoUrl).build();
    }

    @Transactional(readOnly = true)
    public List<ItemCarritoDTO> listar(String correo) {
        return carritoRepository.findByCorreoClienteIgnoreCase(correo)
                .map(this::aItems)
                .orElseGet(List::of);
    }

    @Transactional
    public List<ItemCarritoDTO> agregar(String correo, ItemCarritoRequestDTO request) {
        ProductoCatalogoDTO producto = obtenerProducto(request.codigo());
        Carrito carrito = obtenerOCrear(correo);

        DetalleCarrito existente = carrito.getItems()
                .stream()
                .filter(item -> item.getCodigoProducto().equalsIgnoreCase(request.codigo()))
                .findFirst()
                .orElse(null);

        int cantidadActual = existente == null ? 0 : existente.getCantidad();
        int nuevaCantidad = cantidadActual + request.cantidad();

        validarStock(producto, nuevaCantidad);

        if (existente == null) {
            DetalleCarrito item = new DetalleCarrito();
            item.setCodigoProducto(producto.codigo());
            item.setCantidad(nuevaCantidad);
            carrito.agregarItem(item);
        } else {
            existente.setCantidad(nuevaCantidad);
        }

        carrito.setFechaActualizacion(LocalDateTime.now());

        return aItems(carritoRepository.save(carrito));
    }

    @Transactional
    public List<ItemCarritoDTO> actualizar(
            String correo,
            String codigo,
            int cantidad) {

        ProductoCatalogoDTO producto = obtenerProducto(codigo);
        Carrito carrito = carritoRepository.findByCorreoClienteIgnoreCase(correo)
                .orElseThrow(() -> new IllegalArgumentException("El carrito está vacío."));

        DetalleCarrito item = buscarItem(carrito, codigo);
        validarStock(producto, cantidad);

        item.setCantidad(cantidad);
        carrito.setFechaActualizacion(LocalDateTime.now());

        return aItems(carritoRepository.save(carrito));
    }

    @Transactional
    public List<ItemCarritoDTO> eliminarItem(String correo, String codigo) {
        carritoRepository.findByCorreoClienteIgnoreCase(correo).ifPresent(carrito -> {
            carrito.getItems()
                    .stream()
                    .filter(item -> item.getCodigoProducto().equalsIgnoreCase(codigo))
                    .findFirst()
                    .ifPresent(carrito::eliminarItem);

            carrito.setFechaActualizacion(LocalDateTime.now());
            carritoRepository.save(carrito);
        });

        return listar(correo);
    }

    @Transactional
    public void vaciar(String correo) {
        carritoRepository.findByCorreoClienteIgnoreCase(correo)
                .ifPresent(carritoRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<ItemCarritoDTO> listarInterno(String correo) {
        return listar(correo);
    }

    private Carrito obtenerOCrear(String correo) {
        return carritoRepository.findByCorreoClienteIgnoreCase(correo)
                .orElseGet(() -> {
                    Carrito carrito = new Carrito();
                    carrito.setCorreoCliente(correo.toLowerCase(Locale.ROOT));
                    carrito.setFechaActualizacion(LocalDateTime.now());
                    return carritoRepository.save(carrito);
                });
    }

    private ProductoCatalogoDTO obtenerProducto(String codigo) {
        ProductoCatalogoDTO producto = catalogoClient.get()
                .uri("/api/catalogo/productos/{codigo}", codigo)
                .retrieve()
                .body(ProductoCatalogoDTO.class);

        if (producto == null) {
            throw new IllegalArgumentException("No se encontró el producto solicitado.");
        }

        return producto;
    }

    private void validarStock(ProductoCatalogoDTO producto, int cantidad) {
        if (cantidad < 1 || cantidad > producto.stock()) {
            throw new IllegalArgumentException(
                    "La cantidad solicitada supera el stock disponible para " + producto.codigo() + ".");
        }
    }

    private DetalleCarrito buscarItem(Carrito carrito, String codigo) {
        return carrito.getItems()
                .stream()
                .filter(item -> item.getCodigoProducto().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El producto no está en el carrito."));
    }

    private List<ItemCarritoDTO> aItems(Carrito carrito) {
        return carrito.getItems()
                .stream()
                .map(item -> new ItemCarritoDTO(item.getCodigoProducto(), item.getCantidad()))
                .toList();
    }

}
