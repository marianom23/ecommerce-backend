-- Migración: crea tablas para plantillas de atributos y especificaciones
-- También agrega el campo specifications_json a products

CREATE TABLE IF NOT EXISTS attribute_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    scope VARCHAR(20) NOT NULL, -- VARIANT o PRODUCT
    product_type VARCHAR(30) NULL,
    category_id BIGINT NULL,
    CONSTRAINT fk_attr_templates_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS attribute_values (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL,
    CONSTRAINT fk_attr_values_template FOREIGN KEY (template_id) REFERENCES attribute_templates(id) ON DELETE CASCADE
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='specifications_json') THEN
        ALTER TABLE products ADD COLUMN specifications_json VARCHAR(2000) NULL;
    END IF;
END $$;

CREATE INDEX idx_attr_templates_scope ON attribute_templates(scope);
CREATE INDEX idx_attr_templates_type ON attribute_templates(product_type);
