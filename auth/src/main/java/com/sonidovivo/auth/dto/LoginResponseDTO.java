package com.sonidovivo.auth.dto;

public record LoginResponseDTO(
        long expiraEnSegundos,
        UsuarioResponseDTO usuario) {
}
