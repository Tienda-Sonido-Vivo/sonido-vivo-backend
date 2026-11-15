package com.sonidovivo.auth.dto;

import java.time.LocalDate;
import java.util.List;

public record UsuarioResponseDTO(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String email,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        String direccion,
        String rol,
        String estado,
        List<String> auditoria,
        boolean requiereActivacion) {
}
