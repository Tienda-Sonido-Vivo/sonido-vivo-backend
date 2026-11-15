package com.sonidovivo.auth.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.auth.dto.ActivacionResponseDTO;
import com.sonidovivo.auth.dto.UsuarioAdminRequestDTO;
import com.sonidovivo.auth.dto.UsuarioActualizarRequestDTO;
import com.sonidovivo.auth.dto.UsuarioResponseDTO;
import com.sonidovivo.auth.model.EstadoUsuario;
import com.sonidovivo.auth.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Usuarios", description = "Administración de cuentas de usuario, operación reservada al rol Administrador.")
@RequestMapping("/api/auth/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    @Operation(summary = "Listar usuarios",
            description = "Obtiene todas las cuentas registradas. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de usuarios obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResponseDTO> listar() {
        return usuarioService.listar();
    }
    @Operation(summary = "Buscar usuario por RUN",
            description = "Obtiene los datos de una cuenta a partir de su RUN. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado."),
            @ApiResponse(responseCode = "400", description = "No se encontró el usuario solicitado."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })
    @GetMapping("/{run}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO buscar(@PathVariable String run) {
        return usuarioService.buscarPorRun(run);
    }
    @Operation(summary = "Crear usuario interno",
            description = "Crea una cuenta interna y permite asignar el rol correspondiente. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario interno creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no superan las validaciones o la cuenta ya existe."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "Solo un administrador puede crear usuarios internos.")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioAdminRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }
    @Operation(summary = "Actualizar usuario",
            description = "Actualiza los datos de una cuenta existente. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos o no se encontró el usuario."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })
    @PutMapping("/{run}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO actualizar(
            @PathVariable String run,
            @Valid @RequestBody UsuarioActualizarRequestDTO request,
            Authentication authentication) {

        return usuarioService.actualizar(run, request, authentication.getName());
    }
    @Operation(summary = "Cambiar estado de usuario",
            description = "Activa o desactiva una cuenta existente. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del usuario actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "El estado indicado no es válido o no se encontró el usuario."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })
    @PatchMapping("/{run}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO cambiarEstado(
            @PathVariable String run,
            @RequestBody EstadoUsuario estado,
            Authentication authentication) {

        return usuarioService.cambiarEstado(run, estado, authentication.getName());
    }
    @Operation(summary = "Generar activación de usuario",
            description = "Genera o renueva los datos necesarios para activar una cuenta. Disponible exclusivamente para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos de activación generados correctamente."),
            @ApiResponse(responseCode = "400", description = "No se encontró el usuario o la cuenta ya fue activada."),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos de administrador.")
    })
    @PostMapping("/{run}/activacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ActivacionResponseDTO generarActivacion(@PathVariable String run) {
        return usuarioService.generarActivacion(run);
    }
}
