-- Sample schema change CTPA-1648

CREATE EXTENSION IF NOT EXISTS "pgcrypto" SCHEMA "public";

-- Create id columns to allow null
ALTER TABLE sample.samplesummary ADD COLUMN id uuid;
ALTER TABLE sample.sampleunit    ADD COLUMN id uuid;

-- Add uuids 
UPDATE  sample.samplesummary
SET id = gen_random_uuid();

UPDATE sample.sampleunit
SET id = gen_random_uuid();

-- Set id columns to not null
ALTER TABLE sample.samplesummary ALTER COLUMN id SET NOT NULL;
ALTER TABLE sample.sampleunit    ALTER COLUMN id SET NOT NULL;

-- Add unique constraint to uuids
ALTER TABLE sample.samplesummary ADD CONSTRAINT samplesummary_uuid_key UNIQUE (id);
ALTER TABLE sample.sampleunit    ADD CONSTRAINT sampleunit_uuid_key UNIQUE (id);
