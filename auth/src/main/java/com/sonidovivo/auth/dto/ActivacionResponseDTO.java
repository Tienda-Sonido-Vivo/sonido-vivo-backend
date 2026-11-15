package com.sonidovivo.auth.dto;

import java.time.LocalDateTime;

public record ActivacionResponseDTO(
        String tokenActivacion,
        LocalDateTime expiraEn) {
}
