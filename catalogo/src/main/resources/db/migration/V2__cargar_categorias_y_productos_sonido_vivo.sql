INSERT INTO categorias (nombre) VALUES
('Guitarras Acústicas'),
('Guitarras Eléctricas'),
('Bajos Eléctricos'),
('Baterías'),
('Teclados y Pianos'),
('Amplificadores de sonido'),
('Micrófonos'),
('Pedales de Efectos'),
('Accesorios'),
('Estudio y Grabación');

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'BA001',
    'Bajo Eléctrico 4 Cuerdas',
    categoria.id,
    'Squier',
    'Affinity PJ',
    299990,
    5,
    '/img/tarjetas/Bajo Eléctrico 4 Cuerdas squier Affinity PJ.webp',
    'Pickup PJ, cuerpo álamo, mástil arce.'
FROM categorias categoria
WHERE categoria.nombre = 'Bajos Eléctricos';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'BA002',
    'Bajo Eléctrico Jazz Bass',
    categoria.id,
    'Fender',
    'Player Jazz',
    699990,
    2,
    '/img/tarjetas/Bajo Fender Player Jazz.jpg',
    'Alder body, 2 Alnico V Jazz single-coil.'
FROM categorias categoria
WHERE categoria.nombre = 'Bajos Eléctricos';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'BA003',
    'Bajo Acústico 4 Cuerdas',
    categoria.id,
    'Yamaha',
    'APX700II',
    429990,
    2,
    '/img/tarjetas/Bajo Acústico 4 Cuerdas Yamaha APX700II.jpg',
    'Electroacústico, afinador incorporado.'
FROM categorias categoria
WHERE categoria.nombre = 'Bajos Eléctricos';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'GA001',
    'Guitarra Acústica Folk',
    categoria.id,
    'Yamaha',
    'F310',
    129990,
    8,
    '/img/tarjetas/Guitarra Acustica Yamaha F310.webp',
    'Tapa de abeto, aros y fondo de meranti. Ideal para iniciantes.'
FROM categorias categoria
WHERE categoria.nombre = 'Guitarras Acústicas';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'GA002',
    'Guitarra Acústica Dreadnought',
    categoria.id,
    'Fender',
    'CD-60S',
    189990,
    5,
    '/img/tarjetas/Guitarra Acústica Dreadnought Fender CD-60S.jpg',
    'Tapa de abeto macizo, brazo de caoba. Sonido cálido y proyectado.'
FROM categorias categoria
WHERE categoria.nombre = 'Guitarras Acústicas';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'GA003',
    'Guitarra Acústica Clásica 4/4',
    categoria.id,
    'Yamaha',
    'C40',
    89990,
    10,
    '/img/tarjetas/Guitarra Acústica Clásica Yamaha C40.webp',
    'Nailon, tapa de abeto. Ideal para estudio y flamenco.'
FROM categorias categoria
WHERE categoria.nombre = 'Guitarras Acústicas';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'BT001',
    'Batería Acústica 5 piezas',
    categoria.id,
    'Pearl',
    'Roadshow',
    599990,
    2,
    '/img/tarjetas/Bateria Pearl Roadshow.jpeg',
    'Incluye stands, platillos y pedal de bombo.'
FROM categorias categoria
WHERE categoria.nombre = 'Baterías';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'BT002',
    'Batería Electrónica 8 pads',
    categoria.id,
    'Roland',
    'TD-02KV',
    799990,
    2,
    '/img/tarjetas/Bateria Roland TD-02KV.webp',
    'Módulo TD-02, 8 pads de goma, pedal hi-hat.'
FROM categorias categoria
WHERE categoria.nombre = 'Baterías';

INSERT INTO productos (
    codigo, nombre, categoria_id, marca, modelo, precio, stock, imagen, descripcion
)
SELECT
    'BT003',
    'Caja Snare 14"',
    categoria.id,
    'Pearl',
    'STE1450',
    89990,
    4,
    '/img/tarjetas/Caja Snare 14 Pearl STE1450.jpeg',
    'Acero, 14x5", 10 tensores.'
FROM categorias categoria
WHERE categoria.nombre = 'Baterías';
