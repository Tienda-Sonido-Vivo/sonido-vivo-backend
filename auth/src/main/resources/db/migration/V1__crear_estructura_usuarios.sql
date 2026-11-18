CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    run VARCHAR(15) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    fecha_nacimiento DATE NULL,
    region VARCHAR(100) NOT NULL,
    comuna VARCHAR(100) NOT NULL,
    direccion VARCHAR(180) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    password_hash VARCHAR(100) NULL,
    requiere_cambio_clave BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_run UNIQUE (run),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE TABLE usuario_auditoria (
    usuario_id BIGINT NOT NULL,
    registro VARCHAR(255) NOT NULL,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE tokens_activacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    fecha_uso DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_token_activacion_hash UNIQUE (token_hash),
    CONSTRAINT fk_token_activacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
