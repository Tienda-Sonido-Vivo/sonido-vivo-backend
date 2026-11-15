package com.sonidovivo.carrito.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sonidovivo.carrito.dto.ItemCarritoDTO;
import com.sonidovivo.carrito.service.CarritoService;
import com.sonidovivo.security.InternalTokenValidator;

@RestController
@Tag(name = "Carrito interno", description = "Operaciones de carrito utilizadas por otros microservicios.")
@RequestMapping("/api/carrito/interno")
public class CarritoInternoController {

    private final CarritoService carritoService;
    private final InternalTokenValidator tokenValidator;

    public CarritoInternoController(
            CarritoService carritoService,
            InternalTokenValidator tokenValidator) {

        this.carritoService = carritoService;
        this.tokenValidator = tokenValidator;
    }
    @Operation(summary = "Consultar carrito de un cliente",
            description = "Obtiene el carrito de un cliente para uso interno entre microservicios. Requiere el token interno.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente."),
            @ApiResponse(responseCode = "400", description = "No se encontró el carrito solicitado."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido.")
    })
    @GetMapping("/{correo}")
    public List<ItemCarritoDTO> listar(
            @PathVariable String correo,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para autenticar la comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER) @RequestHeader("X-Internal-Token") String token) {

        validarToken(token);
        return carritoService.listarInterno(correo);
    }
    @Operation(summary = "Vaciar carrito de un cliente",
            description = "Vacía el carrito de un cliente después de completar un proceso interno. Requiere el token interno.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrito vaciado correctamente."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido.")
    })

    @DeleteMapping("/{correo}")
    public ResponseEntity<Void> vaciar(
            @PathVariable String correo,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para autenticar la comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER) @RequestHeader("X-Internal-Token") String token) {

        validarToken(token);
        carritoService.vaciar(correo);

        return ResponseEntity.noContent().build();
    }

    private void validarToken(String token) {
        if (!tokenValidator.esValido(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno inválido.");
        }
    }
}
