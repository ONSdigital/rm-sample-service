CREATE TABLE sample.sampleattributes
(
  sampleunitfk UUID PRIMARY KEY REFERENCES sample.sampleunit(id),
  attributes JSONB NOT NULL
);