package com.sonidovivo.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sonidovivo.auth.dto.ActivacionResponseDTO;
import com.sonidovivo.auth.dto.CompletarActivacionRequestDTO;
import com.sonidovivo.auth.model.TokenActivacion;
import com.sonidovivo.auth.model.Usuario;
import com.sonidovivo.auth.repository.TokenActivacionRepository;
import com.sonidovivo.auth.repository.UsuarioRepository;

@Service
public class ActivacionService {

    private final TokenActivacionRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public ActivacionService(
            TokenActivacionRepository tokenRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ActivacionResponseDTO generarToken(Usuario usuario) {
        tokenRepository.deleteByUsuario_IdAndFechaUsoIsNull(usuario.getId());

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = HexFormat.of().formatHex(bytes);
        LocalDateTime expiracion = LocalDateTime.now().plusHours(24);

        TokenActivacion activacion = new TokenActivacion();
        activacion.setUsuario(usuario);
        activacion.setTokenHash(calcularHash(token));
        activacion.setFechaExpiracion(expiracion);

        tokenRepository.save(activacion);

        return new ActivacionResponseDTO(token, expiracion);
    }

    @Transactional
    public void completarActivacion(CompletarActivacionRequestDTO request) {
        TokenActivacion activacion = tokenRepository.findByTokenHash(calcularHash(request.token()))
                .orElseThrow(() -> new IllegalArgumentException("El token de activación no es válido."));

        if (activacion.getFechaUso() != null
                || activacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token de activación expiró o ya fue utilizado.");
        }

        Usuario usuario = activacion.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(request.nuevaClave()));
        usuario.setRequiereCambioClave(false);
        usuario.agregarAuditoria("Cuenta activada " + LocalDateTime.now());

        activacion.setFechaUso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        tokenRepository.save(activacion);
    }

    private String calcularHash(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible procesar el token de activación.", exception);
        }
    }
}
