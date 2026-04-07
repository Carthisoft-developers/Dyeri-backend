-- db/migration/V1__init_schema.sql — Dyeri Platform Schema
-- PostgreSQL 16
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role            VARCHAR(20) NOT NULL CHECK (role IN ('CLIENT','COOK','DELIVERY','ADMIN')),
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL DEFAULT '',
    phone           VARCHAR(30),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    title           VARCHAR(255),
    banner          VARCHAR(500),
    bio             TEXT,
    avatar          VARCHAR(500),
    specialties     TEXT,
    address         VARCHAR(500),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    delivery_radius INT,
    minimum_order   NUMERIC(10,3),
    prep_time_min   INT,
    is_available    BOOLEAN DEFAULT FALSE,
    accepts_delivery BOOLEAN DEFAULT FALSE,
    accepts_pickup  BOOLEAN DEFAULT FALSE,
    verified        BOOLEAN DEFAULT FALSE,
    rating          DOUBLE PRECISION DEFAULT 0.0,
    review_count    INT DEFAULT 0,
    driver_rating       DOUBLE PRECISION DEFAULT 0.0,
    driver_is_available BOOLEAN DEFAULT FALSE,
    current_lat     DOUBLE PRECISION,
    current_lng     DOUBLE PRECISION,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_cook_location ON users(latitude, longitude) WHERE role='COOK';
CREATE INDEX idx_driver_location ON users(current_lat, current_lng) WHERE role='DELIVERY';
CREATE INDEX idx_cook_name_trgm ON users USING GIN(name gin_trgm_ops) WHERE role='COOK';

CREATE TABLE food_categories (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name  VARCHAR(100) NOT NULL UNIQUE,
    icon  VARCHAR(255),
    image VARCHAR(500)
);

CREATE TABLE dishes (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cook_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id       UUID REFERENCES food_categories(id) ON DELETE SET NULL,
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    image             VARCHAR(500),
    price             NUMERIC(10,3) NOT NULL CHECK (price > 0),
    rating            DOUBLE PRECISION DEFAULT 0.0,
    review_count      INT DEFAULT 0,
    portions          INT DEFAULT 1 CHECK (portions > 0),
    ingredients       TEXT,
    allergens         TEXT,
    prep_time_min     INT DEFAULT 0,
    available         BOOLEAN NOT NULL DEFAULT TRUE,
    delivery_available BOOLEAN DEFAULT TRUE,
    pickup_available  BOOLEAN DEFAULT TRUE,
    stock_qty         INT DEFAULT 0 CHECK (stock_qty >= 0),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_dish_cook_id ON dishes(cook_id);
CREATE INDEX idx_dish_category_id ON dishes(category_id);
CREATE INDEX idx_dish_available ON dishes(available);
CREATE INDEX idx_dish_name_trgm ON dishes USING GIN(name gin_trgm_ops);

CREATE TABLE orders (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id        UUID NOT NULL REFERENCES users(id),
    cook_id          UUID NOT NULL REFERENCES users(id),
    driver_id        UUID REFERENCES users(id),
    status           VARCHAR(25) NOT NULL DEFAULT 'PENDING',
    mode             VARCHAR(10) NOT NULL DEFAULT 'DELIVERY',
    total            NUMERIC(10,3) NOT NULL CHECK (total >= 0),
    subtotal         NUMERIC(10,3),
    delivery_fee     NUMERIC(10,3),
    service_fee      NUMERIC(10,3),
    pickup_address   VARCHAR(500),
    pickup_lat       DOUBLE PRECISION,
    pickup_lng       DOUBLE PRECISION,
    delivery_address VARCHAR(500),
    delivery_lat     DOUBLE PRECISION,
    delivery_lng     DOUBLE PRECISION,
    delivery_proof_photo VARCHAR(500),
    eta              INT DEFAULT 30,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_order_client_id ON orders(client_id);
CREATE INDEX idx_order_cook_id ON orders(cook_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at DESC);

CREATE TABLE order_items (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    dish_id  UUID REFERENCES dishes(id) ON DELETE SET NULL,
    name     VARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price    NUMERIC(10,3) NOT NULL CHECK (price > 0)
);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

CREATE TABLE carts (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id    UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    cook_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    subtotal     NUMERIC(10,3) DEFAULT 0,
    service_fee  NUMERIC(10,3) DEFAULT 0,
    delivery_fee NUMERIC(10,3) DEFAULT 0,
    total        NUMERIC(10,3) DEFAULT 0,
    updated_at   TIMESTAMPTZ
);

CREATE TABLE cart_items (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id  UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    dish_id  UUID NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    price    NUMERIC(10,3) NOT NULL CHECK (price > 0)
);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);

CREATE TABLE reviews (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cook_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dish_id    UUID REFERENCES dishes(id) ON DELETE SET NULL,
    rating     INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_review_cook_id ON reviews(cook_id);
CREATE INDEX idx_review_dish_id ON reviews(dish_id);

CREATE TABLE saved_addresses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label           VARCHAR(100) NOT NULL,
    address         VARCHAR(500) NOT NULL,
    additional_info VARCHAR(255),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_saved_addr_client ON saved_addresses(client_id);

CREATE TABLE favorites (
    client_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dish_id   UUID NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    saved_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (client_id, dish_id)
);

CREATE TABLE follows (
    client_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cook_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (client_id, cook_id)
);

CREATE TABLE delivery_assignments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    driver_id    UUID NOT NULL REFERENCES users(id),
    assigned_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    picked_up_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    proof_photo  VARCHAR(500)
);
