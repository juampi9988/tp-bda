-- Datos iniciales reproducibles para ms-premios
INSERT INTO premios (id, nombre, puntos_requeridos, porcentaje_descuento, activo)
VALUES (1, 'Descuento Bronce 10%', 100, 10.00, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO premios (id, nombre, puntos_requeridos, porcentaje_descuento, activo)
VALUES (2, 'Descuento Plata 25%', 250, 25.00, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO premios (id, nombre, puntos_requeridos, porcentaje_descuento, activo)
VALUES (3, 'Corte Mitad de Precio 50%', 500, 50.00, true)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('premios', 'id'), COALESCE((SELECT MAX(id) FROM premios), 1));

