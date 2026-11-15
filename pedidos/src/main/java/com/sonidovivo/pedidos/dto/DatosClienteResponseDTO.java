package com.sonidovivo.pedidos.dto;

public record DatosClienteResponseDTO(
        String nombre,
        String apellidos,
        String telefono,
        String correo,
        String region,
        String comuna,
        String direccion,
        String numeroVivienda,
        String codigoPostal) {
}
