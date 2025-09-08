-- PostgreSQL Database Initialization Script for E-commerce Backend
-- This script runs automatically when the PostgreSQL container starts for the first time

-- Create extensions if they don't exist
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set default configuration for better performance
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 1000; -- Log queries taking more than 1 second

-- Create database user if it doesn't exist (fallback, usually handled by POSTGRES_USER)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'ecommerce_user') THEN
        CREATE USER ecommerce_user WITH ENCRYPTED PASSWORD 'ecommerce_password';
    END IF;
END
$$;

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;
GRANT ALL ON SCHEMA public TO ecommerce_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecommerce_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecommerce_user;

-- Set default permissions for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ecommerce_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ecommerce_user;

-- Create indexes for commonly queried fields (these will be created if tables exist)
-- Spring Boot JPA will handle table creation, but we can add indexes later

-- Log successful initialization
INSERT INTO pg_catalog.pg_stat_statements_info (dealloc) VALUES (0) ON CONFLICT DO NOTHING;

-- Create a function to log when the database is ready
CREATE OR REPLACE FUNCTION log_database_ready() RETURNS void AS $$
BEGIN
    RAISE NOTICE 'E-commerce database initialization completed successfully';
END;
$$ LANGUAGE plpgsql;

-- Call the function
SELECT log_database_ready();