package com.sonidovivo.pedidos.dto;

import com.sonidovivo.pedidos.model.MetodoEntrega;
import com.sonidovivo.pedidos.model.MetodoPago;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PedidoCreateRequestDTO(
        @NotNull MetodoEntrega metodoEntrega,
        @NotNull MetodoPago metodoPago,
        @NotNull @Valid DatosClienteRequestDTO cliente) {
}
