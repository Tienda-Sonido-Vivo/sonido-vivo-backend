package com.sonidovivo.mensajeria.dto;

import com.sonidovivo.mensajeria.model.TipoMensaje;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MensajeRequestDTO(
        @NotBlank @Size(max = 150) String nombreCompleto,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Size(max = 2000) String mensaje,
        @NotNull TipoMensaje tipoMensaje) {
}
