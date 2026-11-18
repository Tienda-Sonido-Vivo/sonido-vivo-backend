CREATE TABLE descuentos_stock_pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_pedido VARCHAR(20) NOT NULL,
    fecha_registro DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_descuentos_stock_codigo_pedido UNIQUE (codigo_pedido)
);
