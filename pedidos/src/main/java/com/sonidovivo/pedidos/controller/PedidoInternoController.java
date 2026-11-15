package com.sonidovivo.pedidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
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
@Tag(name = "Pedidos internos", description = "Operaciones de actualización de pedidos invocadas por otros microservicios.")
@RequestMapping("/api/pedidos/interno")
public class PedidoInternoController {

    private final PedidoService pedidoService;
    private final InternalTokenValidator tokenValidator;

    public PedidoInternoController(
            PedidoService pedidoService,
            InternalTokenValidator tokenValidator) {

        this.pedidoService = pedidoService;
        this.tokenValidator = tokenValidator;
    }
    @Operation(summary = "Marcar pedido como pagado",
            description = "Actualiza el pedido después de la confirmación interna del pago. Requiere el token de comunicación entre microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido marcado como pagado correctamente."),
            @ApiResponse(responseCode = "400", description = "No se encontró el pedido indicado."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido.")
    })
    @PatchMapping("/{codigo}/pagado")
    public PedidoResponseDTO marcarPagado(
            @PathVariable String codigo,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para autenticar la comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER) @RequestHeader("X-Internal-Token") String token) {

        if (!tokenValidator.esValido(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno inválido.");
        }

        return pedidoService.marcarPagado(codigo);
    }
}
