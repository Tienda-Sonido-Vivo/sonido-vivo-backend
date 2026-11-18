package com.sonidovivo.pedidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sonidovivo.pedidos.dto.PedidoResponseDTO;
import com.sonidovivo.pedidos.service.PedidoService;
import com.sonidovivo.security.InternalTokenValidator;

@RestController
@Tag(name = "Pedidos internos", description = "Operaciones protegidas para la comunicación entre microservicios.")
@RequestMapping("/api/pedidos/interno")
public class PedidoInternoController {

    private final PedidoService pedidoService;
    private final InternalTokenValidator tokenValidator;

    public PedidoInternoController(PedidoService pedidoService, InternalTokenValidator tokenValidator) {
        this.pedidoService = pedidoService;
        this.tokenValidator = tokenValidator;
    }

    @Operation(
            summary = "Consultar pedido para procesar el pago",
            description = "Entrega a Pagos los datos del pedido para validar su titular, importe y método de pago. Requiere X-Internal-Token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido obtenido."),
            @ApiResponse(responseCode = "401", description = "Token interno inválido."),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado.")
    })
    @GetMapping("/{codigo}")
    public PedidoResponseDTO obtener(
            @PathVariable String codigo,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para comunicación interna.", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Internal-Token") String token) {
        validarToken(token);
        return pedidoService.buscarInterno(codigo);
    }

    @Operation(
            summary = "Marcar pedido como pagado",
            description = "Confirma el pago iniciado por CLIENTE. Antes de actualizar pagado=true, solicita a Catálogo el descuento atómico e idempotente del stock. Requiere X-Internal-Token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido marcado como pagado y stock actualizado."),
            @ApiResponse(responseCode = "401", description = "Token interno inválido."),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado."),
            @ApiResponse(responseCode = "409", description = "No hay stock suficiente o no se pudo concretar la compra.")
    })
    @PatchMapping("/{codigo}/pagado")
    public PedidoResponseDTO marcarPagado(
            @PathVariable String codigo,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para comunicación interna.", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Internal-Token") String token) {
        validarToken(token);
        return pedidoService.marcarPagado(codigo);
    }

    private void validarToken(String token) {
        if (!tokenValidator.esValido(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno inválido.");
        }
    }
}
