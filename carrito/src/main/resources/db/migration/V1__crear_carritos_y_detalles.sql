CREATE TABLE carritos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    correo_cliente VARCHAR(150) NOT NULL,
    fecha_actualizacion DATETIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_carrito_correo UNIQUE (correo_cliente)
);

CREATE TABLE detalles_carrito (
    id BIGINT NOT NULL AUTO_INCREMENT,
    carrito_id BIGINT NOT NULL,
    producto_codigo VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_carrito_producto UNIQUE (carrito_id, producto_codigo),
    CONSTRAINT fk_detalle_carrito FOREIGN KEY (carrito_id) REFERENCES carritos(id) ON DELETE CASCADE,
    CONSTRAINT ck_carrito_cantidad CHECK (cantidad > 0)
);
