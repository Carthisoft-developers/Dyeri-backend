-- db/migration/V2__seed_categories.sql
INSERT INTO food_categories (id, name, icon) VALUES
    (gen_random_uuid(), 'Traditionnel',  '🍲'),
    (gen_random_uuid(), 'Italien',       '🍕'),
    (gen_random_uuid(), 'Asiatique',     '🍜'),
    (gen_random_uuid(), 'Végétarien',    '🥗'),
    (gen_random_uuid(), 'Fast-Food',     '🍔'),
    (gen_random_uuid(), 'Pâtisseries',   '🥐'),
    (gen_random_uuid(), 'Grillades',     '🥩'),
    (gen_random_uuid(), 'Fruits de mer', '🦐');
