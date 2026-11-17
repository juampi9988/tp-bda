-- Datos iniciales reproducibles para ms-turnos
INSERT INTO peluqueros (id, nombre, email_calendar, activo)
VALUES (1, 'Mateo Rossi', 'cuentarandomtest7@gmail.com', true)
ON CONFLICT (id) DO UPDATE SET email_calendar = EXCLUDED.email_calendar;

INSERT INTO peluqueros (id, nombre, email_calendar, activo)
VALUES (2, 'Valentina Gómez', 'valentina.gomez@peluqueria.utn.edu.ar', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO servicios (id, nombre, duracion_minutos, precio, puntos_que_otorga, activo)
VALUES (1, 'Corte Clásico', 30, 12000.00, 100, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO servicios (id, nombre, duracion_minutos, precio, puntos_que_otorga, activo)
VALUES (2, 'Corte y Perfilado de Barba', 45, 18000.00, 150, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO servicios (id, nombre, duracion_minutos, precio, puntos_que_otorga, activo)
VALUES (3, 'Coloración Completa', 90, 32000.00, 300, true)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('peluqueros', 'id'), COALESCE((SELECT MAX(id) FROM peluqueros), 1));
SELECT setval(pg_get_serial_sequence('servicios', 'id'), COALESCE((SELECT MAX(id) FROM servicios), 1));

