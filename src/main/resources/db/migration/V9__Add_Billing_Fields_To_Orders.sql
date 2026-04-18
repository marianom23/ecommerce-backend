-- Migration V9: Add missing billing fields to orders table safely
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_full_name VARCHAR(150);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_email VARCHAR(150);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_phone VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_document_type VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_document_number VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_tax_condition VARCHAR(30);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_business_name VARCHAR(150);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_street VARCHAR(150);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_street_number VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_city VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_state VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_postal_code VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_country VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_apartment_number VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_floor VARCHAR(20);

-- Migrate legacy data safely from tax_id if it exists
DO $$ 
BEGIN 
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='orders' AND column_name='billing_tax_id') THEN
        UPDATE orders SET billing_document_number = billing_tax_id WHERE billing_document_number IS NULL;
    END IF;
END $$;
