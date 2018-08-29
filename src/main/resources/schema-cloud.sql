CREATE SCHEMA sample;

-- create postgres extension to allow generation of v4 UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto" SCHEMA "public";
