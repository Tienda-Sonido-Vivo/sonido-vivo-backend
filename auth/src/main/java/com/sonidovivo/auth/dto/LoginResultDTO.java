package com.sonidovivo.auth.dto;

public record LoginResultDTO(
        String token,
        long expiraEnSegundos,
        UsuarioResponseDTO usuario) {
}
