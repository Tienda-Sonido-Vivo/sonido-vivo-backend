package com.sonidovivo.pagos.dto;

import java.math.BigDecimal;

import com.sonidovivo.pagos.model.MetodoPago;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PagoRegistroInternoDTO(
        @NotBlank @Size(max = 20) String codigoPedido,
        @NotBlank @Email @Size(max = 150) String correoCliente,
        @NotNull @Positive BigDecimal monto,
        @NotNull MetodoPago metodoPago) {
}
