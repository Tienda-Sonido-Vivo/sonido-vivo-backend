package com.sonidovivo.mensajeria.dto;

import java.time.LocalDateTime;

public record MensajeResponseDTO(
        Long id,
        String nombreCompleto,
        String correo,
        String mensaje,
        String tipoMensaje,
        String tipoRemitente,
        boolean revisado,
        LocalDateTime fechaCreacion) {
}
