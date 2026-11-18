INSERT INTO usuarios (
    run, nombre, apellidos, email, fecha_nacimiento, region, comuna, direccion,
    rol, estado, password_hash, requiere_cambio_clave
) VALUES
(
    '123456789', 'Juan', 'Pérez Gómez', 'admin@demo.cl', '1985-03-12',
    'Metropolitana de Santiago', 'Santiago', 'Calle Falsa 123',
    'ADMIN', 'ACTIVO', NULL, TRUE
),
(
    '987654321', 'María', 'González Soto', 'vendedor1@demo.cl', '1992-07-21',
    'Valparaíso', 'Viña del Mar', 'Avenida Demo 456',
    'VENDEDOR', 'ACTIVO', NULL, TRUE
),
(
    '112233445', 'Pedro', 'Ramírez López', 'vendedor2@demo.cl', '1995-11-08',
    'Biobío', 'Concepción', 'Calle Principal 789',
    'VENDEDOR', 'ACTIVO', NULL, TRUE
),
(
    '556677889', 'Ana', 'Martínez Díaz', 'usuario@demo.cl', '1998-01-25',
    'Metropolitana de Santiago', 'Ñuñoa', 'Avenida Siempre Viva 123',
    'CLIENTE', 'ACTIVO', NULL, TRUE
);

INSERT INTO usuario_auditoria (usuario_id, registro)
SELECT id, 'Inició sesión el 01/09/2026' FROM usuarios WHERE run = '123456789';

INSERT INTO usuario_auditoria (usuario_id, registro)
SELECT id, 'Actualizó el stock del producto #245' FROM usuarios WHERE run = '123456789';

INSERT INTO usuario_auditoria (usuario_id, registro)
SELECT id, 'Creó un nuevo usuario vendedor' FROM usuarios WHERE run = '123456789';

INSERT INTO usuario_auditoria (usuario_id, registro)
SELECT id, 'Inició sesión el 02/09/2026' FROM usuarios WHERE run = '987654321';

INSERT INTO usuario_auditoria (usuario_id, registro)
SELECT id, 'Actualizó el stock del producto #102' FROM usuarios WHERE run = '987654321';

INSERT INTO usuario_auditoria (usuario_id, registro)
SELECT id, 'Inició sesión el 03/09/2026' FROM usuarios WHERE run = '112233445';
