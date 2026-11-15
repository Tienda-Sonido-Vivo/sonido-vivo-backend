package com.sonidovivo.carrito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.carrito.dto.CantidadCarritoRequestDTO;
import com.sonidovivo.carrito.dto.ItemCarritoDTO;
import com.sonidovivo.carrito.dto.ItemCarritoRequestDTO;
import com.sonidovivo.carrito.service.CarritoService;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Carrito", description = "Operaciones del carrito de compra del cliente autenticado.")
@RequestMapping("/api/carrito")
@PreAuthorize("hasRole('CLIENTE')")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }
    @Operation(summary = "Consultar carrito",
            description = "Obtiene los productos del carrito correspondiente al cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "La operación está reservada a clientes.")
    })

    @GetMapping
    public List<ItemCarritoDTO> listar(Authentication authentication) {
        return carritoService.listar(authentication.getName());
    }
    @Operation(summary = "Agregar producto al carrito",
            description = "Agrega un producto al carrito del cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto agregado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos no son válidos o no se encontró el producto."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "La operación está reservada a clientes.")
    })
    @PostMapping("/items")
    public ResponseEntity<List<ItemCarritoDTO>> agregar(
            @Valid @RequestBody ItemCarritoRequestDTO request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carritoService.agregar(authentication.getName(), request));
    }
    @Operation(summary = "Actualizar cantidad de un producto",
            description = "Modifica la cantidad de un producto dentro del carrito del cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada correctamente."),
            @ApiResponse(responseCode = "400", description = "La cantidad o el producto indicado no son válidos."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "La operación está reservada a clientes.")
    })
    @PutMapping("/items/{codigo}")
    public List<ItemCarritoDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody CantidadCarritoRequestDTO request,
            Authentication authentication) {

        return carritoService.actualizar(authentication.getName(), codigo, request.cantidad());
    }
    @Operation(summary = "Eliminar producto del carrito",
            description = "Elimina un producto del carrito del cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado del carrito correctamente."),
            @ApiResponse(responseCode = "400", description = "El producto no está en el carrito."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "La operación está reservada a clientes.")
    })
    @DeleteMapping("/items/{codigo}")
    public List<ItemCarritoDTO> eliminar(
            @PathVariable String codigo,
            Authentication authentication) {

        return carritoService.eliminarItem(authentication.getName(), codigo);
    }
    @Operation(summary = "Vaciar carrito",
            description = "Elimina todos los productos del carrito del cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrito vaciado correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "La operación está reservada a clientes.")
    })

    @DeleteMapping
    public ResponseEntity<Void> vaciar(Authentication authentication) {
        carritoService.vaciar(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
