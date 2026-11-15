package com.sonidovivo.pedidos.model;

public enum EstadoPedido {
    CONFIRMADO,
    EN_PREPARACION,
    DESPACHADO,
    ENTREGADO;

    public String etiqueta() {
        return switch (this) {
            case CONFIRMADO -> "Confirmado";
            case EN_PREPARACION -> "En preparación";
            case DESPACHADO -> "Despachado";
            case ENTREGADO -> "Entregado";
        };
    }

    public static EstadoPedido desdeEtiqueta(String valor) {
        return switch (valor.trim().toLowerCase()) {
            case "confirmado" -> CONFIRMADO;
            case "en preparación", "en preparacion" -> EN_PREPARACION;
            case "despachado" -> DESPACHADO;
            case "entregado" -> ENTREGADO;
            default -> throw new IllegalArgumentException("El estado de pedido no es válido.");
        };
    }
}
