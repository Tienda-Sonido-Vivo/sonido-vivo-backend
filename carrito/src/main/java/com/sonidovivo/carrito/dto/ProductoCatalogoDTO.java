package com.sonidovivo.carrito.dto;

import java.math.BigDecimal;

public record ProductoCatalogoDTO(
        Long id,
        String codigo,
        String nombre,
        Object categoria,
        String marca,
        String modelo,
        BigDecimal precio,
        Integer stock,
        String imagen,
        String descripcion) {
}
