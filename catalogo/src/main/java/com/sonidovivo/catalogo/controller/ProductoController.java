package com.sonidovivo.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.catalogo.dto.ProductoRequestDTO;
import com.sonidovivo.catalogo.dto.ProductoResponseDTO;
import com.sonidovivo.catalogo.dto.StockRequestDTO;
import com.sonidovivo.catalogo.service.CatalogoService;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Productos", description = "Consulta y administración del catálogo de productos de Sonido Vivo.")
@RequestMapping("/api/catalogo/productos")
public class ProductoController {

    private final CatalogoService catalogoService;

    public ProductoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }
    @Operation(summary = "Listar productos",
            description = "Obtiene los productos disponibles. Permite filtrar opcionalmente por categoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de productos obtenido correctamente."),
            @ApiResponse(responseCode = "400", description = "El filtro de categoría no es válido.")
    })

    @GetMapping
    public List<ProductoResponseDTO> listar(
            @RequestParam(required = false) String categoria) {

        if (categoria == null || categoria.isBlank()) {
            return catalogoService.listarProductos();
        }

        return catalogoService.listarPorCategoria(categoria);
    }
    @Operation(summary = "Buscar producto por código",
            description = "Obtiene el detalle de un producto mediante su código.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado."),
            @ApiResponse(responseCode = "400", description = "No existe un producto con el código indicado.")
    })
    @GetMapping("/{codigo}")
    public ProductoResponseDTO buscar(@PathVariable String codigo) {
        return catalogoService.buscarPorCodigo(codigo);
    }
    @Operation(summary = "Crear producto",
            description = "Registra un nuevo producto en el catálogo. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos no son válidos o ya existe un producto con ese código."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.crear(request));
    }
    @Operation(summary = "Actualizar producto",
            description = "Modifica los datos de un producto existente. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos o no se encontró el producto."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })
    @PutMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponseDTO actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody ProductoRequestDTO request) {

        return catalogoService.actualizar(codigo, request);
    }
    @Operation(summary = "Actualizar stock",
            description = "Modifica la cantidad disponible de un producto. Disponible para administradores y vendedores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "La cantidad o el código del producto no son válidos."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para modificar el stock."),
            @ApiResponse(responseCode = "409", description = "El producto no existe o no hay stock suficiente.")
    })
    @PatchMapping("/{codigo}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ProductoResponseDTO actualizarStock(
            @PathVariable String codigo,
            @Valid @RequestBody StockRequestDTO request) {

        return catalogoService.actualizarStock(codigo, request.stock());
    }
    @Operation(summary = "Eliminar producto",
            description = "Elimina un producto del catálogo. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente."),
            @ApiResponse(responseCode = "400", description = "No se encontró el producto indicado."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })
    @DeleteMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        catalogoService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }
}
