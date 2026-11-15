package com.sonidovivo.pagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sonidovivo.pagos.dto.PagoRegistroInternoDTO;
import com.sonidovivo.pagos.dto.PagoResponseDTO;
import com.sonidovivo.pagos.service.PagoService;
import com.sonidovivo.security.InternalTokenValidator;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Pagos internos", description = "Registro interno de pagos durante el proceso de compra.")
@RequestMapping("/api/pagos/interno")
public class PagoInternoController {

    private final PagoService pagoService;
    private final InternalTokenValidator tokenValidator;

    public PagoInternoController(
            PagoService pagoService,
            InternalTokenValidator tokenValidator) {

        this.pagoService = pagoService;
        this.tokenValidator = tokenValidator;
    }
    @Operation(summary = "Registrar pago pendiente",
            description = "Registra un pago pendiente durante el proceso interno de creación de pedidos. Requiere el token de comunicación entre microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago pendiente registrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos del pago no son válidos."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido.")
    })
    @PostMapping
    public ResponseEntity<PagoResponseDTO> registrar(
            @Valid @RequestBody PagoRegistroInternoDTO request,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para autenticar la comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER) @RequestHeader("X-Internal-Token") String token) {

        if (!tokenValidator.esValido(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno inválido.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagoService.registrarPendiente(request));
    }
}
