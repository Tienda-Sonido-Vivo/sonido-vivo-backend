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

import com.sonidovivo.catalogo.dto.ProductoResponseDTO;
import com.sonidovivo.catalogo.dto.ReservaStockRequestDTO;
import com.sonidovivo.catalogo.service.CatalogoService;
import com.sonidovivo.security.InternalTokenValidator;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Inventario interno", description = "Operaciones de reserva y liberación de stock utilizadas entre microservicios.")
@RequestMapping("/api/catalogo/interno/productos")
public class InventarioInternoController {

    private final CatalogoService catalogoService;
    private final InternalTokenValidator tokenValidator;

    public InventarioInternoController(
            CatalogoService catalogoService,
            InternalTokenValidator tokenValidator) {

        this.catalogoService = catalogoService;
        this.tokenValidator = tokenValidator;
    }
    @Operation(summary = "Reservar stock",
            description = "Reserva unidades de un producto para un proceso interno de compra. Requiere el token de comunicación entre microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock reservado correctamente."),
            @ApiResponse(responseCode = "400", description = "La cantidad indicada no es válida."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido."),
            @ApiResponse(responseCode = "409", description = "El producto no existe o no tiene stock suficiente.")
    })
    @PostMapping("/{codigo}/reservar")
    public ProductoResponseDTO reservar(
            @PathVariable String codigo,
            @Valid @RequestBody ReservaStockRequestDTO request,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para autenticar la comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER) @RequestHeader("X-Internal-Token") String token) {

        validarToken(token);
        return catalogoService.reservarStock(codigo, request.cantidad());
    }
    @Operation(summary = "Liberar stock reservado",
            description = "Libera unidades previamente reservadas. Requiere el token de comunicación entre microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Stock liberado correctamente."),
            @ApiResponse(responseCode = "400", description = "La cantidad indicada o el producto no son válidos."),
            @ApiResponse(responseCode = "401", description = "El token interno no es válido.")
    })
    @PostMapping("/{codigo}/liberar")
    public ResponseEntity<Void> liberar(
            @PathVariable String codigo,
            @Valid @RequestBody ReservaStockRequestDTO request,
            @Parameter(name = "X-Internal-Token", description = "Token compartido para autenticar la comunicación interna entre microservicios.", required = true, in = ParameterIn.HEADER) @RequestHeader("X-Internal-Token") String token) {

        validarToken(token);
        catalogoService.liberarStock(codigo, request.cantidad());

        return ResponseEntity.noContent().build();
    }

    private void validarToken(String token) {
        if (!tokenValidator.esValido(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno inválido.");
        }
    }
}
