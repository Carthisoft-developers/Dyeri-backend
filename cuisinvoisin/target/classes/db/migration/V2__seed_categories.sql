-- db/migration/V2__seed_categories.sql
INSERT INTO food_categories (id, name, icon, image) VALUES
    (gen_random_uuid(), 'Traditionnel',  '🍲', NULL),
    (gen_random_uuid(), 'Italien',       '🍕', NULL),
    (gen_random_uuid(), 'Asiatique',     '🍜', NULL),
    (gen_random_uuid(), 'Végétarien',    '🥗', NULL),
    (gen_random_uuid(), 'Fast-Food',     '🍔', NULL),
    (gen_random_uuid(), 'Pâtisseries',   '🥐', NULL),
    (gen_random_uuid(), 'Grillades',     '🥩', NULL),
    (gen_random_uuid(), 'Fruits de mer', '🦐', NULL);
