package com.sonidovivo.pedidos.controller;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.pedidos.dto.EstadoPedidoRequestDTO;
import com.sonidovivo.pedidos.dto.PedidoCreateRequestDTO;
import com.sonidovivo.pedidos.dto.PedidoResponseDTO;
import com.sonidovivo.pedidos.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Pedidos", description = "Creación, consulta y administración de pedidos.")
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }
    @Operation(summary = "Crear pedido",
            description = "Registra un pedido a partir de los datos de compra del cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos del pedido no son válidos."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "409", description = "No es posible crear el pedido debido al estado del carrito, stock o proceso de compra.")
    })

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> crear(
            @Valid @RequestBody PedidoCreateRequestDTO request,
            Authentication authentication) {

        PedidoResponseDTO pedido = pedidoService.crear(request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }
    @Operation(summary = "Listar pedidos del cliente",
            description = "Obtiene exclusivamente los pedidos asociados al cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pedidos obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "La operación está reservada a clientes.")
    })

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<PedidoResponseDTO> listarMisPedidos(Authentication authentication) {
        return pedidoService.listarDelCliente(authentication.getName());
    }
    @Operation(summary = "Listar todos los pedidos",
            description = "Obtiene todos los pedidos registrados. Disponible para administradores y vendedores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pedidos obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para consultar todos los pedidos.")
    })

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoService.listarTodos();
    }
    @Operation(summary = "Buscar pedido por código",
            description = "Obtiene el detalle de un pedido. Los clientes solo pueden consultar sus propios pedidos; administradores y vendedores pueden consultar todos.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado."),
            @ApiResponse(responseCode = "400", description = "No se encontró el pedido indicado."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permiso para consultar este pedido.")
    })
    @GetMapping("/{codigo}")
    public PedidoResponseDTO buscar(
            @PathVariable String codigo,
            Authentication authentication) {

        boolean personalAutorizado = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_ADMIN") || rol.equals("ROLE_VENDEDOR"));

        return pedidoService.buscar(codigo, authentication.getName(), personalAutorizado);
    }
    @Operation(summary = "Actualizar estado del pedido",
            description = "Actualiza el estado de un pedido. Disponible para administradores y vendedores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pedido actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "El estado indicado no es válido o no se encontró el pedido."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para actualizar pedidos.")
    })
    @PatchMapping("/{codigo}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public PedidoResponseDTO actualizarEstado(
            @PathVariable String codigo,
            @Valid @RequestBody EstadoPedidoRequestDTO request) {

        return pedidoService.actualizarEstado(codigo, request);
    }
}
