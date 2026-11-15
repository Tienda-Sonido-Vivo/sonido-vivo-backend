package com.sonidovivo.pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.pagos.dto.PagoResponseDTO;
import com.sonidovivo.pagos.service.PagoService;

@RestController
@Tag(name = "Pagos", description = "Consulta y confirmación de pagos.")
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }
    @Operation(summary = "Listar pagos",
            description = "Obtiene todos los pagos registrados. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pagos obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PagoResponseDTO> listarTodos() {
        return pagoService.listarTodos();
    }
    @Operation(summary = "Listar pagos del cliente",
            description = "Obtiene exclusivamente los pagos asociados al cliente autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de pagos obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "La operación está reservada a clientes.")
    })

    @GetMapping("/mis-pagos")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<PagoResponseDTO> listarMisPagos(Authentication authentication) {
        return pagoService.listarCliente(authentication.getName());
    }
    @Operation(summary = "Buscar pago por código de pedido",
            description = "Obtiene la información del pago asociado a un pedido. Los clientes solo pueden consultar sus propios pagos; el personal autorizado puede consultar todos.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado."),
            @ApiResponse(responseCode = "400", description = "No se encontró el pago asociado al pedido indicado."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permiso para consultar este pago.")
    })
    @GetMapping("/{codigoPedido}")
    public PagoResponseDTO buscar(
            @PathVariable String codigoPedido,
            Authentication authentication) {

        boolean personalAutorizado = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_ADMIN") || rol.equals("ROLE_VENDEDOR"));

        return pagoService.buscar(codigoPedido, authentication.getName(), personalAutorizado);
    }
    @Operation(summary = "Confirmar pago",
            description = "Confirma un pago pendiente. Disponible para administradores y vendedores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago confirmado correctamente."),
            @ApiResponse(responseCode = "400", description = "No se encontró el pago asociado al pedido indicado."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para confirmar pagos.")
    })
    @PatchMapping("/{codigoPedido}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public PagoResponseDTO confirmar(@PathVariable String codigoPedido) {
        return pagoService.confirmar(codigoPedido);
    }
}
