package com.sonidovivo.catalogo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sonidovivo.catalogo.dto.CategoriaResponseDTO;
import com.sonidovivo.catalogo.dto.ProductoRequestDTO;
import com.sonidovivo.catalogo.dto.ProductoResponseDTO;
import com.sonidovivo.catalogo.model.Categoria;
import com.sonidovivo.catalogo.model.Producto;
import com.sonidovivo.catalogo.repository.CategoriaRepository;
import com.sonidovivo.catalogo.repository.ProductoRepository;

@Service
public class CatalogoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public CatalogoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorCodigo(String codigo) {
        return aResponse(obtenerProducto(codigo));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaNombreIgnoreCase(categoria)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        if (productoRepository.existsByCodigo(request.codigo())) {
            throw new IllegalArgumentException("Ya existe un producto con ese código.");
        }

        Producto producto = new Producto();
        aplicarDatos(producto, request);

        return aResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO actualizar(String codigo, ProductoRequestDTO request) {
        Producto producto = obtenerProducto(codigo);

        if (!producto.getCodigo().equals(request.codigo())) {
            throw new IllegalArgumentException("El código del producto no puede modificarse.");
        }

        aplicarDatos(producto, request);

        return aResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(String codigo) {
        productoRepository.delete(obtenerProducto(codigo));
    }

    @Transactional
    public ProductoResponseDTO actualizarStock(String codigo, int stock) {
        Producto producto = obtenerProducto(codigo);
        producto.setStock(stock);

        return aResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO reservarStock(String codigo, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        int filasActualizadas = productoRepository.reservarStock(codigo, cantidad);

        if (filasActualizadas == 0) {
            throw new IllegalStateException("El producto no existe o no tiene stock suficiente: " + codigo);
        }

        return aResponse(obtenerProducto(codigo));
    }

    @Transactional
    public void liberarStock(String codigo, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        if (productoRepository.liberarStock(codigo, cantidad) == 0) {
            throw new IllegalArgumentException("No se encontró el producto: " + codigo);
        }
    }

    private void aplicarDatos(Producto producto, ProductoRequestDTO request) {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(request.categoria().trim())
                .orElseThrow(() -> new IllegalArgumentException("La categoría indicada no existe."));

        producto.setCodigo(request.codigo().trim().toUpperCase());
        producto.setNombre(request.nombre().trim());
        producto.setCategoria(categoria);
        producto.setMarca(request.marca().trim());
        producto.setModelo(request.modelo().trim());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setImagen(request.imagen().trim());
        producto.setDescripcion(request.descripcion().trim());
    }

    private Producto obtenerProducto(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto solicitado."));
    }

    private ProductoResponseDTO aResponse(Producto producto) {
        Categoria categoria = producto.getCategoria();

        return new ProductoResponseDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                new CategoriaResponseDTO(
                        categoria.getId(),
                        categoria.getNombre(),
                        categoria.getDescripcion()),
                producto.getMarca(),
                producto.getModelo(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getImagen(),
                producto.getDescripcion());
    }
}
