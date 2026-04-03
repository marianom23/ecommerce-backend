-- V5__Clean_legacy_data.sql
-- 1. Asignar tipos a productos que quedaron "huérfanos" (legacy)
-- Dejamos como CONSOLE los que digan Anbernic, el resto como GAME
UPDATE products 
SET product_type = 'CONSOLE' 
WHERE product_type IS NULL AND name ILIKE '%Anbernic%';

UPDATE products 
SET product_type = 'GAME' 
WHERE product_type IS NULL;

-- 2. Limpiar todos los atributos viejos de las variantes 
-- para forzar el uso del nuevo sistema de la V4
UPDATE product_variants 
SET attributes_json = NULL;
