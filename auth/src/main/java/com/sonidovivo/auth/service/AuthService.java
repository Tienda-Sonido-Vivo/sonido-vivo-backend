package com.sonidovivo.auth.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sonidovivo.auth.dto.LoginRequestDTO;
import com.sonidovivo.auth.dto.LoginResultDTO;
import com.sonidovivo.auth.dto.RegistroRequestDTO;
import com.sonidovivo.auth.dto.UsuarioResponseDTO;
import com.sonidovivo.auth.model.EstadoUsuario;
import com.sonidovivo.auth.model.RolUsuario;
import com.sonidovivo.auth.model.Usuario;
import com.sonidovivo.auth.repository.UsuarioRepository;
import com.sonidovivo.security.JwtTokenService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public LoginResultDTO iniciarSesion(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(
                request.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos."));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO
                || usuario.getPasswordHash() == null
                || !passwordEncoder.matches(request.clave(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Correo o contraseña incorrectos.");
        }

        if (usuario.isRequiereCambioClave()) {
            throw new IllegalStateException("La cuenta requiere completar su activación.");
        }

        usuario.agregarAuditoria("Inicio de sesión " + LocalDateTime.now());

        String token = jwtTokenService.generarToken(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol().name());

        return new LoginResultDTO(
                token,
                jwtTokenService.obtenerDuracionSegundos(),
                UsuarioMapper.aResponse(usuario));
    }

    @Transactional
    public UsuarioResponseDTO registrarCliente(RegistroRequestDTO request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String run = RutChileno.normalizar(request.run());

        if (!RutChileno.esValido(request.run())) {
            throw new IllegalArgumentException("El RUN ingresado no es válido.");
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }

        if (usuarioRepository.existsByRun(run)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese RUN.");
        }

        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setEmail(email);
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRegion(request.region().trim());
        usuario.setComuna(request.comuna().trim());
        usuario.setDireccion(request.direccion().trim());
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setPasswordHash(passwordEncoder.encode(request.clave()));
        usuario.setRequiereCambioClave(false);
        usuario.agregarAuditoria("Cuenta registrada " + LocalDateTime.now());

        return UsuarioMapper.aResponse(usuarioRepository.save(usuario));
    }
}
