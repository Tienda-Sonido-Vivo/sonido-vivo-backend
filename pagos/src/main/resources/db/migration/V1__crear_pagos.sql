CREATE TABLE pagos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_pedido VARCHAR(20) NOT NULL,
    correo_cliente VARCHAR(150) NOT NULL,
    monto DECIMAL(12, 2) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    fecha_pago DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pagos_codigo_pedido UNIQUE (codigo_pedido),
    INDEX idx_pagos_correo_fecha (correo_cliente, fecha_pago),
    CONSTRAINT ck_pagos_monto CHECK (monto > 0)
);
