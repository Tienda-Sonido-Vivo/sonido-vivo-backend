package com.sonidovivo.mensajeria.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.mensajeria.dto.MensajeRequestDTO;
import com.sonidovivo.mensajeria.dto.MensajeResponseDTO;
import com.sonidovivo.mensajeria.dto.MensajeRevisionRequestDTO;
import com.sonidovivo.mensajeria.service.MensajeriaService;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Mensajería", description = "Envío y administración de mensajes de contacto.")
@RequestMapping("/api/contactos")
public class MensajeriaController {

    private final MensajeriaService mensajeriaService;

    public MensajeriaController(MensajeriaService mensajeriaService) {
        this.mensajeriaService = mensajeriaService;
    }
    @Operation(summary = "Enviar mensaje de contacto",
            description = "Registra un mensaje enviado desde el formulario de contacto del sitio.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mensaje registrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no superan las validaciones."),
            @ApiResponse(responseCode = "403", description = "El rol autenticado no puede enviar mensajes.")
    })
    @PostMapping
    public ResponseEntity<MensajeResponseDTO> crear(
            @Valid @RequestBody MensajeRequestDTO request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensajeriaService.crear(request, authentication));
    }
    @Operation(summary = "Listar mensajes recibidos",
            description = "Obtiene los mensajes recibidos. Disponible para administradores y vendedores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de mensajes obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para consultar los mensajes.")
    })

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public List<MensajeResponseDTO> listar() {
        return mensajeriaService.listar();
    }
    @Operation(summary = "Actualizar revisión de mensaje",
            description = "Marca un mensaje como revisado o pendiente. Disponible para administradores y vendedores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de revisión actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "El identificador o estado indicado no es válido."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para modificar mensajes.")
    })
    @PatchMapping("/{id}/revision")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public MensajeResponseDTO cambiarRevision(
            @PathVariable Long id,
            @RequestBody MensajeRevisionRequestDTO request) {

        return mensajeriaService.cambiarRevision(id, request.revisado());
    }
}
