SET SCHEMA 'sample';

CREATE TABLE sample.samplestate
(
  state character varying (20) NOT NULL,
  CONSTRAINT state_pkey PRIMARY KEY (state)
);

INSERT INTO sample.samplestate(state) VALUES('INIT');
INSERT INTO sample.samplestate(state) VALUES('ACTIVE');

-- for primary keys
CREATE SEQUENCE sampleidseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE TABLE sample.samplesummary
(
  sampleid bigint DEFAULT nextval('sampleidseq'::regclass) NOT NULL,
  effectivestartdatetime timestamp with time zone,
  effectiveenddatetime timestamp with time zone,
  surveyref character varying,
  ingestdatetime timestamp with time zone,
  state character varying NOT NULL,
  CONSTRAINT sampleid_pkey PRIMARY KEY (sampleid),
  CONSTRAINT state_fkey FOREIGN KEY (state)
      REFERENCES sample.samplestate (state) MATCH SIMPLE
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
  sampleunitref character varying,
  sampleunittype character varying(10),
  CONSTRAINT sampleunitid_pkey PRIMARY KEY (sampleunitid) ,
  CONSTRAINT summaryid_fkey FOREIGN KEY (sampleid)
      REFERENCES sample.samplesummary (sampleid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


