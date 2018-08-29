CREATE ROLE samplesvc PASSWORD 'samplesvc' NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION INHERIT LOGIN;

-- create postgres extension to allow generation of v4 UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto" SCHEMA "public";

CREATE SCHEMA sample;

SET search_path TO sample, public;
