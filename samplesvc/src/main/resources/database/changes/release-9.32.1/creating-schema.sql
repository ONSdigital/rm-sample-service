SET SCHEMA 'sample';

CREATE TABLE sample.samplestate
(
  state character varying NOT NULL,
  CONSTRAINT state_pkey PRIMARY KEY (state)
);


-- for primary keys
CREATE SEQUENCE sampleidseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

CREATE TABLE sample.samplesummary
(
  sampleid integer DEFAULT nextval('sampleidseq'::regclass) NOT NULL,
  effectivestartdatetime date[],
  effectiveenddatetime date[],
  surveyref character varying[],
  ingestdatetime date[],
  state character varying,
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

CREATE TABLE sample.samplingunit
(
  samplingunitid integer DEFAULT nextval('sampleunitidseq'::regclass) NOT NULL,
  sampleid bigint,
  sampleunitref character varying[],
  CONSTRAINT samplingunitid_pkey PRIMARY KEY (samplingunitid),
  CONSTRAINT summaryid_fkey FOREIGN KEY (sampleid)
      REFERENCES sample.samplesummary (sampleid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


