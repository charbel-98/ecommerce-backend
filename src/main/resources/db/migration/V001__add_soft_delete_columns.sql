-- Add soft delete columns to all tables
-- First add nullable columns, then update with defaults, then make NOT NULL

-- Addresses table
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE addresses SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE addresses ALTER COLUMN is_deleted SET NOT NULL;

-- Brands table  
ALTER TABLE brands ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE brands ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE brands SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE brands ALTER COLUMN is_deleted SET NOT NULL;

-- Categories table
ALTER TABLE categories ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE categories SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE categories ALTER COLUMN is_deleted SET NOT NULL;

-- Events table
ALTER TABLE events ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE events ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE events SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE events ALTER COLUMN is_deleted SET NOT NULL;

-- Reviews table
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE reviews SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE reviews ALTER COLUMN is_deleted SET NOT NULL;

-- Users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE users SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE users ALTER COLUMN is_deleted SET NOT NULL;

-- Products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE products SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE products ALTER COLUMN is_deleted SET NOT NULL;

-- Product variants table
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE product_variants SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE product_variants ALTER COLUMN is_deleted SET NOT NULL;

-- Product images table
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE product_images SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE product_images ALTER COLUMN is_deleted SET NOT NULL;

-- Discounts table
ALTER TABLE discounts ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE discounts ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE discounts SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE discounts ALTER COLUMN is_deleted SET NOT NULL;

-- Review images table
ALTER TABLE review_images ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE review_images ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE review_images SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE review_images ALTER COLUMN is_deleted SET NOT NULL;

-- Review helpful votes table
ALTER TABLE review_helpful_votes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE review_helpful_votes ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE review_helpful_votes SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE review_helpful_votes ALTER COLUMN is_deleted SET NOT NULL;

-- Refresh tokens table
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE refresh_tokens SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE refresh_tokens ALTER COLUMN is_deleted SET NOT NULL;

-- Orders table
ALTER TABLE orders ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE orders SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE orders ALTER COLUMN is_deleted SET NOT NULL;

-- Order items table
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
UPDATE order_items SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE order_items ALTER COLUMN is_deleted SET NOT NULL;