-- Migration V9: Add missing billing fields to orders table safely
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_document_type VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_document_number VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_tax_condition VARCHAR(30);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_business_name VARCHAR(150);

-- Migrate legacy data safely from tax_id if it exists
DO $$ 
BEGIN 
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='orders' AND column_name='billing_tax_id') THEN
        UPDATE orders SET billing_document_number = billing_tax_id WHERE billing_document_number IS NULL;
    END IF;
END $$;
