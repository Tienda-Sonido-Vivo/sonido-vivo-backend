ALTER TABLE pedidos
    ADD COLUMN fecha_pago DATETIME NULL;

-- Los pedidos que ya estaban pagados no tenían una fecha de pago propia.
-- Se utiliza su fecha de creación como referencia histórica disponible.
UPDATE pedidos
SET fecha_pago = fecha_registro
WHERE pagado = TRUE AND fecha_pago IS NULL;
