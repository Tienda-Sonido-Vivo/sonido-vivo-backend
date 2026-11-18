CREATE TABLE ventas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_pedido VARCHAR(20) NOT NULL,
    fecha_venta DATETIME NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    cantidad_productos INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ventas_codigo_pedido UNIQUE (codigo_pedido),
    INDEX idx_ventas_fecha (fecha_venta),
    CONSTRAINT ck_ventas_total CHECK (total > 0),
    CONSTRAINT ck_ventas_cantidad CHECK (cantidad_productos >= 0)
);
