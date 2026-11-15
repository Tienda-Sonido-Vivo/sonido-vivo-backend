package com.sonidovivo.ventas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sonidovivo.security.InternalTokenValidator;
import com.sonidovivo.ventas.dto.ResumenVentasDTO;
import com.sonidovivo.ventas.dto.VentaRegistroRequestDTO;
import com.sonidovivo.ventas.service.VentaService;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Ventas", description = "Registro interno y consulta de resúmenes de ventas.")
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final InternalTokenValidator tokenValidator;

    public VentaController(
            VentaService ventaService,
            InternalTokenValidator tokenValidator) {

        this.ventaService = ventaService;
        this.tokenValidator = tokenValidator;
    }
    @Operation(summary = "Registrar venta",
            description = "Registra una venta generada por el proceso de pedidos. Requiere el token de comunicación entre microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venta registrada correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos de la venta no son válidos."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido.")
    })
    @PostMapping("/interno")
    public ResponseEntity<Void> registrar(
            @Valid @RequestBody VentaRegistroRequestDTO request,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para autenticar la comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER) @RequestHeader("X-Internal-Token") String token) {

        if (!tokenValidator.esValido(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno inválido.");
        }

        ventaService.registrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @Operation(summary = "Consultar resumen diario de ventas",
            description = "Obtiene el resumen de ventas correspondiente a una fecha determinada. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen diario obtenido correctamente."),
            @ApiResponse(responseCode = "400", description = "La fecha indicada no es válida."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })

    @GetMapping("/resumen/diario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResumenVentasDTO resumenDiario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ventaService.resumenDiario(fecha);
    }
    @Operation(summary = "Consultar resumen mensual de ventas",
            description = "Obtiene el resumen de ventas de un año y mes determinados. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen mensual obtenido correctamente."),
            @ApiResponse(responseCode = "400", description = "El año o mes indicado no es válido."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })

    @GetMapping("/resumen/mensual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResumenVentasDTO resumenMensual(
            @RequestParam int anio,
            @RequestParam int mes) {

        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }

        return ventaService.resumenMensual(anio, mes);
    }
}
