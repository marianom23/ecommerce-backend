-- V8__Add_is_visible_to_products.sql
-- Agregamos la columna is_visible de forma segura
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_visible BOOLEAN DEFAULT TRUE;

-- Si ya había filas, nos aseguramos de que no tengan NULL
UPDATE products SET is_visible = TRUE WHERE is_visible IS NULL;

-- Aplicamos el constraint NOT NULL para consistencia
ALTER TABLE products ALTER COLUMN is_visible SET NOT NULL;
