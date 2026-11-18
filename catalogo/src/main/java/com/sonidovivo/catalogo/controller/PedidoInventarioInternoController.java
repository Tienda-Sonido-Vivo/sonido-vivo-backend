package com.sonidovivo.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sonidovivo.catalogo.dto.DescuentoStockPedidoRequestDTO;
import com.sonidovivo.catalogo.service.CatalogoService;
import com.sonidovivo.security.InternalTokenValidator;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Inventario interno", description = "Descuento idempotente del inventario después de aprobar un pedido.")
@RequestMapping("/api/catalogo/interno/pedidos")
public class PedidoInventarioInternoController {

    private final CatalogoService catalogoService;
    private final InternalTokenValidator tokenValidator;

    public PedidoInventarioInternoController(
            CatalogoService catalogoService,
            InternalTokenValidator tokenValidator) {
        this.catalogoService = catalogoService;
        this.tokenValidator = tokenValidator;
    }

    @Operation(
            summary = "Descontar stock de un pedido pagado",
            description = "Descuenta todas las unidades del pedido dentro de una transacción. El código del pedido se registra una sola vez para evitar descuentos duplicados. Solo puede invocarlo otro microservicio mediante X-Internal-Token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Stock descontado o el pedido ya había sido procesado."),
            @ApiResponse(responseCode = "400", description = "La lista de productos o sus cantidades no son válidas."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido."),
            @ApiResponse(responseCode = "409", description = "No existe stock suficiente para completar el pedido.")
    })
    @PostMapping("/{codigoPedido}/descontar-stock")
    public ResponseEntity<Void> descontar(
            @PathVariable String codigoPedido,
            @Valid @RequestBody DescuentoStockPedidoRequestDTO request,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Internal-Token") String token) {

        if (!tokenValidator.esValido(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno inválido.");
        }

        catalogoService.descontarStockPorPedido(codigoPedido, request);
        return ResponseEntity.noContent().build();
    }
}
