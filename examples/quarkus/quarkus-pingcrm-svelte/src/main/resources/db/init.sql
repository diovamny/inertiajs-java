-- PostgreSQL initialization script for Dev Services
-- This runs when the Dev Services PostgreSQL container starts

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- The actual schema is managed by Flyway migrations
-- This script just ensures the database is ready

-- Create pingcrm user if not exists (for Dev Services)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pingcrm') THEN
        CREATE ROLE pingcrm WITH LOGIN PASSWORD 'pingcrm';
    END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE pingcrm TO pingcrm;
GRANT ALL ON SCHEMA public TO pingcrm;