-- V6__Add_presale_fields_to_products.sql
-- Agregamos soporte para preventas
ALTER TABLE products 
    ADD COLUMN is_presale BOOLEAN DEFAULT FALSE,
    ADD COLUMN release_date TIMESTAMP NULL;
