package com.sonidovivo.catalogo.dto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long id,
        String codigo,
        String nombre,
        CategoriaResponseDTO categoria,
        String marca,
        String modelo,
        BigDecimal precio,
        Integer stock,
        String imagen,
        String descripcion) {
}
