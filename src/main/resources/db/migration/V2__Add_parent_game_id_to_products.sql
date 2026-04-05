-- Migración: agrega la FK parent_game_id a la tabla products
-- para modelar la relación DLC → Juego padre (self-referencing, opcional)

ALTER TABLE products
    ADD COLUMN parent_game_id BIGINT NULL,
    ADD CONSTRAINT fk_products_parent_game
        FOREIGN KEY (parent_game_id) REFERENCES products(id)
        ON DELETE SET NULL;

CREATE INDEX idx_products_parent_game ON products(parent_game_id);
