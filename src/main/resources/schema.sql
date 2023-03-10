CREATE SCHEMA sample;

SET search_path TO sample, public;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

GRANT USAGE ON SCHEMA sample TO samplesvc;