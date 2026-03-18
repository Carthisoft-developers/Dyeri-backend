-- db/migration/V3__seed_demo_users.sql — demo accounts managed via Keycloak
-- Insert local user profiles (Keycloak IDs must match)
INSERT INTO users (id, role, name, email, phone, is_active, is_available, accepts_delivery, accepts_pickup, delivery_radius, minimum_order, prep_time_min, rating, review_count)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'CLIENT',   'Demo Client',  'client@test.com',   '+216 99 000 001', TRUE, FALSE, FALSE, FALSE, 0, NULL, 0, 0.0, 0),
    ('00000000-0000-0000-0000-000000000002', 'COOK',     'Demo Cook',    'cook@test.com',     '+216 99 000 002', TRUE, TRUE,  TRUE,  TRUE,  10, 10.000, 30, 0.0, 0),
    ('00000000-0000-0000-0000-000000000003', 'DELIVERY', 'Demo Driver',  'delivery@test.com', '+216 99 000 003', TRUE, FALSE, FALSE, FALSE, 0, NULL, 0, 0.0, 0);
