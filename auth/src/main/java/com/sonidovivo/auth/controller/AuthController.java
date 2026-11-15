package com.sonidovivo.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonidovivo.auth.dto.CompletarActivacionRequestDTO;
import com.sonidovivo.auth.dto.LoginRequestDTO;
import com.sonidovivo.auth.dto.LoginResponseDTO;
import com.sonidovivo.auth.dto.LoginResultDTO;
import com.sonidovivo.auth.dto.RegistroRequestDTO;
import com.sonidovivo.auth.dto.UsuarioResponseDTO;
import com.sonidovivo.auth.service.ActivacionService;
import com.sonidovivo.auth.service.AuthService;
import com.sonidovivo.auth.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@Tag(name = "Autenticación", description = "Operaciones de inicio y cierre de sesión, registro de clientes y activación de cuentas.")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final ActivacionService activacionService;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            UsuarioService usuarioService,
            ActivacionService activacionService,
            @Value("${app.cookie.secure:false}") boolean cookieSecure) {

        this.authService = authService;
        this.usuarioService = usuarioService;
        this.activacionService = activacionService;
        this.cookieSecure = cookieSecure;
    }
    @Operation(summary = "Iniciar sesión",
            description = "Autentica a un usuario con sus credenciales y establece la cookie segura SV_ACCESS.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso; se establece la cookie de autenticación."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos."),
            @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos."),
            @ApiResponse(responseCode = "409", description = "La cuenta requiere completar su activación.")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request) {
        LoginResultDTO resultado = authService.iniciarSesion(request);

        ResponseCookie cookie = ResponseCookie.from("SV_ACCESS", resultado.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofSeconds(resultado.expiraEnSegundos()))
                .build();

        LoginResponseDTO respuesta = new LoginResponseDTO(
                resultado.expiraEnSegundos(),
                resultado.usuario());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(respuesta);
    }
    @Operation(summary = "Cerrar sesión",
            description = "Invalida la cookie SV_ACCESS del navegador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente.")
    })

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion() {
        ResponseCookie cookie = ResponseCookie.from("SV_ACCESS", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
    @Operation(summary = "Registrar cliente",
            description = "Registra una nueva cuenta de cliente en Sonido Vivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente registrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no superan las validaciones o la cuenta ya existe.")
    })
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarCliente(request));
    }
    @Operation(summary = "Completar activación de cuenta",
            description = "Completa la activación de una cuenta mediante los datos de activación recibidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta activada correctamente."),
            @ApiResponse(responseCode = "400", description = "El token de activación no es válido, expiró o ya fue utilizado."),
            @ApiResponse(responseCode = "409", description = "No fue posible procesar la activación en el estado actual.")
    })
    @PostMapping("/activaciones/completar")
    public ResponseEntity<Void> completarActivacion(
            @Valid @RequestBody CompletarActivacionRequestDTO request) {

        activacionService.completarActivacion(request);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Obtener usuario autenticado",
            description = "Obtiene los datos del usuario asociado a la sesión autenticada.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos del usuario obtenidos correctamente."),
            @ApiResponse(responseCode = "400", description = "No se encontró el usuario asociado a la sesión."),
            @ApiResponse(responseCode = "401", description = "No existe una sesión autenticada.")
    })
    @GetMapping("/me")
    public UsuarioResponseDTO obtenerSesion(Authentication authentication) {
        return usuarioService.buscarPorCorreo(authentication.getName());
    }
}
