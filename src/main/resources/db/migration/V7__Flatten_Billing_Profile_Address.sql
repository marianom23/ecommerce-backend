-- 1. Agregamos las columnas de la dirección directamente al perfil
ALTER TABLE billing_profiles
ADD COLUMN IF NOT EXISTS street VARCHAR(150),
ADD COLUMN IF NOT EXISTS street_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS city VARCHAR(100),
ADD COLUMN IF NOT EXISTS state VARCHAR(100),
ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20),
ADD COLUMN IF NOT EXISTS country VARCHAR(100),
ADD COLUMN IF NOT EXISTS apartment_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS floor VARCHAR(10);

-- 2. Migramos los datos desde la tabla addresses
UPDATE billing_profiles
SET street = a.street,
    street_number = a.street_number,
    city = a.city,
    state = a.state,
    postal_code = a.postal_code,
    country = a.country,
    apartment_number = a.apartment_number,
    floor = a.floor
FROM addresses a
WHERE billing_profiles.billing_address_id = a.id;

-- 3. Hacemos que el Foreign Key actual sea opcional (nullable) para que el backend deje de usarlo
ALTER TABLE billing_profiles ALTER COLUMN billing_address_id DROP NOT NULL;
