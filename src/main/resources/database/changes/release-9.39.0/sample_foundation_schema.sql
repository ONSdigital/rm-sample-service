SET SCHEMA 'sample';

--create sequences

CREATE SEQUENCE IF NOT EXISTS samplesummaryseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

-- for primary keys
CREATE SEQUENCE IF NOT EXISTS sampleunitseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS collectionexercisejobseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE TABLE sample.samplesummary
(
  samplesummaryPK bigint,
  surveyref character varying(100),
  effectivestartdatetime timestamp with time zone,
  effectiveenddatetime timestamp with time zone,
  state character varying(20) NOT NULL,
  ingestdatetime timestamp with time zone
);

CREATE TABLE IF NOT EXISTS sample.sampleunit
(
  sampleunitPK bigint NOT NULL,
  samplesummaryFK bigint NOT NULL,
  sampleunitref character varying(20),
  sampleunittype character varying(2),
  formtype character varying(10),
  state character varying(20) NOT NULL
);

CREATE TABLE sample.collectionexercisejob
(
  collectionexercisejobPK bigint NOT NULL,
  collectionexerciseid uuid,
  surveyref character varying(100),
  exercisedatetime timestamp with time zone,
  createddatetime timestamp with time zone
);

CREATE TABLE sample.samplesummarystate
(
  state character varying (20) NOT NULL
);

CREATE TABLE sample.sampleunitstate
(
  state character varying (20) NOT NULL
);

--add primary keys
ALTER TABLE ONLY samplesummary
    ADD CONSTRAINT samplesummary_pkey PRIMARY KEY (samplesummaryPK);

ALTER TABLE ONLY sampleunit
    ADD CONSTRAINT sampleunit_pkey PRIMARY KEY (sampleunitPK);

ALTER TABLE ONLY collectionexercisejob
    ADD CONSTRAINT collectionexercisejob_pkey PRIMARY KEY (collectionexercisejobPK);

ALTER TABLE ONLY samplesummarystate
    ADD CONSTRAINT samplesummarystate_pkey PRIMARY KEY (state);

ALTER TABLE ONLY sampleunitstate
    ADD CONSTRAINT sampleunitstate_pkey PRIMARY KEY (state);

--add foreign keys
ALTER TABLE ONLY samplesummary
    ADD CONSTRAINT state_fkey FOREIGN KEY (state) REFERENCES samplesummarystate(state);

ALTER TABLE ONLY sampleunit
    ADD CONSTRAINT samplesummary_fkey FOREIGN KEY (samplesummaryFK) REFERENCES samplesummary(samplesummaryPK);

ALTER TABLE ONLY sampleunit
    ADD CONSTRAINT state_fkey FOREIGN KEY (state) REFERENCES sampleunitstate(state);