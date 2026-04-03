-- V4__Initial_attribute_templates.sql

-- 1. PRODUCT SCOPE TEMPLATES (Specifications)
-- General for any product
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Fabricante', 'PRODUCT', NULL, NULL);
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('País de Origen', 'PRODUCT', NULL, NULL);

-- Specific for Games
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Desarrollador', 'PRODUCT', 'GAME', NULL);
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Editor', 'PRODUCT', 'GAME', NULL);
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Idiomas', 'PRODUCT', 'GAME', NULL);
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Jugadores', 'PRODUCT', 'GAME', NULL);

-- 2. VARIANT SCOPE TEMPLATES (Attributes)
-- Region for Games/DLC
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Región', 'VARIANT', 'GAME', NULL);
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Región', 'VARIANT', 'DLC', NULL);

-- Format (Digital/Physical)
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Formato', 'VARIANT', NULL, NULL);

-- Capacity for Consoles
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Capacidad', 'VARIANT', 'CONSOLE', NULL);

-- Color for Accessories
INSERT INTO attribute_templates (name, scope, product_type, category_id) VALUES ('Color', 'VARIANT', 'ACCESSORY', NULL);


-- 3. VALUES FOR TEMPLATES
-- Values for Region (assuming IDs started at 1 for templates above, but let's use a more robust way if possible)
-- Since it's a fresh migration, we can expect IDs 1 to 11.

-- Fabricante (ID 1)
INSERT INTO attribute_values (template_id, label, value) VALUES (1, 'Nintendo', 'NINTENDO');
INSERT INTO attribute_values (template_id, label, value) VALUES (1, 'Sony', 'SONY');
INSERT INTO attribute_values (template_id, label, value) VALUES (1, 'Microsoft', 'MICROSOFT');

-- Idiomas (ID 5)
INSERT INTO attribute_values (template_id, label, value) VALUES (5, 'Español', 'ES');
INSERT INTO attribute_values (template_id, label, value) VALUES (5, 'Inglés', 'EN');
INSERT INTO attribute_values (template_id, label, value) VALUES (5, 'Multilenguaje', 'MULTI');

-- Región (ID 7 & 8)
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'USA', 'USA' FROM attribute_templates WHERE name = 'Región';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'Europa', 'EU' FROM attribute_templates WHERE name = 'Región';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'Global', 'GLOBAL' FROM attribute_templates WHERE name = 'Región';

-- Formato (ID 9)
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'Físico', 'PHYSICAL' FROM attribute_templates WHERE name = 'Formato';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'Digital', 'DIGITAL' FROM attribute_templates WHERE name = 'Formato';

-- Capacidad (ID 10)
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, '32GB', '32GB' FROM attribute_templates WHERE name = 'Capacidad';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, '64GB', '64GB' FROM attribute_templates WHERE name = 'Capacidad';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, '512GB', '512GB' FROM attribute_templates WHERE name = 'Capacidad';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, '1TB', '1TB' FROM attribute_templates WHERE name = 'Capacidad';

-- Color (ID 11)
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'Negro', 'BLACK' FROM attribute_templates WHERE name = 'Color';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'Blanco', 'WHITE' FROM attribute_templates WHERE name = 'Color';
INSERT INTO attribute_values (template_id, label, value) 
SELECT id, 'Neon Red/Blue', 'NEON' FROM attribute_templates WHERE name = 'Color';
