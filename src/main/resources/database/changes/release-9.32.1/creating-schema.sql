SET SCHEMA 'sample';

CREATE TABLE sample.samplesummarystate
(
  state character varying (20) NOT NULL,
  CONSTRAINT state_pkey PRIMARY KEY (state)
);

INSERT INTO sample.samplesummarystate(state) VALUES('INIT');
INSERT INTO sample.samplesummarystate(state) VALUES('ACTIVE');

CREATE TABLE sample.sampleunitstate
(
  state character varying (20) NOT NULL,
  CONSTRAINT sampleunitstate_pkey PRIMARY KEY (state)
);

INSERT INTO sample.sampleunitstate(state) VALUES('INIT');
INSERT INTO sample.sampleunitstate(state) VALUES('DELIVERED');

-- for primary keys
CREATE SEQUENCE samplesummaryseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE TABLE sample.samplesummary
(
  sampleid bigint DEFAULT nextval('samplesummaryseq'::regclass) NOT NULL,
  surveyref character varying(100),
  effectivestartdatetime timestamp with time zone,
  effectiveenddatetime timestamp with time zone,
  state character varying(20) NOT NULL,
  ingestdatetime timestamp with time zone,
  CONSTRAINT sampleid_pkey PRIMARY KEY (sampleid),
  CONSTRAINT state_fkey FOREIGN KEY (state)
      REFERENCES sample.samplesummarystate (state) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- for primary keys
CREATE SEQUENCE sampleunitidseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE TABLE sample.sampleunit
(
  sampleunitid bigint DEFAULT nextval('sampleunitidseq'::regclass) NOT NULL,
  sampleid bigint NOT NULL,
  sampleunitref character varying(20),
  sampleunittype character varying(2),
  formtype character varying(10),
  state character varying(20) NOT NULL,
  CONSTRAINT sampleunitid_pkey PRIMARY KEY (sampleunitid) ,
  CONSTRAINT summaryid_fkey FOREIGN KEY (sampleid)
      REFERENCES sample.samplesummary (sampleid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT sampleunitstate_fkey FOREIGN KEY (state)
      REFERENCES sample.sampleunitstate (state) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- for primary keys
CREATE SEQUENCE collectionexercisejobidseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE TABLE sample.collectionexercisejob
(
  collectionexerciseid bigint NOT NULL,
  surveyref character varying(100),
  exercisedatetime timestamp with time zone,
  createddatetime timestamp with time zone,
  CONSTRAINT collectionexerciseid_pkey PRIMARY KEY (collectionexerciseid)
);

