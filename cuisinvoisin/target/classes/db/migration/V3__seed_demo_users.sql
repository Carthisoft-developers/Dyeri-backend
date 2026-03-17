-- db/migration/V3__seed_demo_users.sql
-- BCrypt hash of "password" (strength 12)
-- $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/oTePJqYs6

INSERT INTO users (id, role, name, email, password_hash, phone, is_active,
                   is_available, accepts_delivery, accepts_pickup, delivery_radius,
                   minimum_order, prep_time_min, rating, review_count)
VALUES
    (
        gen_random_uuid(), 'CLIENT', 'Demo Client', 'client@test.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/oTePJqYs6',
        '+216 99 000 001', TRUE,
        FALSE, FALSE, FALSE, 0, NULL, 0, 0.0, 0
    ),
    (
        gen_random_uuid(), 'COOK', 'Demo Cook', 'cook@test.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/oTePJqYs6',
        '+216 99 000 002', TRUE,
        TRUE, TRUE, TRUE, 10, 10.000, 30, 0.0, 0
    ),
    (
        gen_random_uuid(), 'DELIVERY', 'Demo Driver', 'delivery@test.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/oTePJqYs6',
        '+216 99 000 003', TRUE,
        FALSE, FALSE, FALSE, 0, NULL, 0, 0.0, 0
    );
