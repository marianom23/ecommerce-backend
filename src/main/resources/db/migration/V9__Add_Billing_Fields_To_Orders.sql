-- Migration V9: Add missing billing fields to orders table
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS billing_document_type VARCHAR(20),
ADD COLUMN IF NOT EXISTS billing_document_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS billing_tax_condition VARCHAR(30),
ADD COLUMN IF NOT EXISTS billing_business_name VARCHAR(150);

-- Opcional: Migrar datos existentes de tax_id a document_number si el nombre cambió
UPDATE orders SET billing_document_number = billing_tax_id WHERE billing_tax_id IS NOT NULL AND billing_document_number IS NULL;
