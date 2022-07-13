--
-- PostgreSQL database dump
--

-- Dumped from database version 11.6
-- Dumped by pg_dump version 14.4

--SET statement_timeout = 0;
--SET lock_timeout = 0;
--SET idle_in_transaction_session_timeout = 0;
--SET client_encoding = 'UTF8';
--SET standard_conforming_strings = on;
--SELECT pg_catalog.set_config('search_path', '', false);
--SET check_function_bodies = false;
--SET xmloption = content;
--SET client_min_messages = warning;
--SET row_security = off;

--
-- Name: sample; Type: SCHEMA; Schema: -; Owner: samplesvc
--

--CREATE SCHEMA sample;
--
--
--ALTER SCHEMA sample OWNER TO samplesvc;

--
-- Name: collectionexercisejobseq; Type: SEQUENCE; Schema: sample; Owner: postgres
--

CREATE SEQUENCE sample.collectionexercisejobseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


ALTER TABLE sample.collectionexercisejobseq OWNER TO postgres;

--SET default_tablespace = '';

--
-- Name: databasechangelog; Type: TABLE; Schema: sample; Owner: postgres
--

CREATE TABLE sample.databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);


ALTER TABLE sample.databasechangelog OWNER TO postgres;

--
-- Name: databasechangeloglock; Type: TABLE; Schema: sample; Owner: postgres
--

CREATE TABLE sample.databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


ALTER TABLE sample.databasechangeloglock OWNER TO postgres;

--
-- Name: messagelogseq; Type: SEQUENCE; Schema: sample; Owner: postgres
--

CREATE SEQUENCE sample.messagelogseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


ALTER TABLE sample.messagelogseq OWNER TO postgres;

--
-- Name: reportpkseq; Type: SEQUENCE; Schema: sample; Owner: postgres
--

CREATE SEQUENCE sample.reportpkseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


ALTER TABLE sample.reportpkseq OWNER TO postgres;

--
-- Name: sample_summary; Type: TABLE; Schema: sample; Owner: postgres
--

CREATE TABLE sample.sample_summary (
    sample_summary_pk bigint NOT NULL,
    state_fk character varying(20) NOT NULL,
    ingest_date_time timestamp with time zone,
    id uuid NOT NULL,
    description character varying(250),
    total_sample_units integer,
    expected_collection_instruments integer,
    notes text,
    collection_exercise_id uuid,
    survey_id uuid,
    mark_for_deletion boolean DEFAULT false
);


ALTER TABLE sample.sample_summary OWNER TO postgres;

--
-- Name: sample_summary_state; Type: TABLE; Schema: sample; Owner: postgres
--

CREATE TABLE sample.sample_summary_state (
    state_pk character varying(20) NOT NULL
);


ALTER TABLE sample.sample_summary_state OWNER TO postgres;

--
-- Name: sample_unit; Type: TABLE; Schema: sample; Owner: postgres
--

CREATE TABLE sample.sample_unit (
    sample_unit_pk bigint NOT NULL,
    sample_summary_fk bigint NOT NULL,
    sample_unit_ref character varying(20),
    sample_unit_type character varying(2),
    form_type character varying(10),
    state_fk character varying(20) NOT NULL,
    id uuid NOT NULL,
    party_id uuid,
    active_enrolment boolean DEFAULT false,
    collection_instrument_id uuid
);


ALTER TABLE sample.sample_unit OWNER TO postgres;

--
-- Name: sample_unit_state; Type: TABLE; Schema: sample; Owner: postgres
--

CREATE TABLE sample.sample_unit_state (
    state_pk character varying(20) NOT NULL
);


ALTER TABLE sample.sample_unit_state OWNER TO postgres;

--
-- Name: samplesummaryseq; Type: SEQUENCE; Schema: sample; Owner: postgres
--

CREATE SEQUENCE sample.samplesummaryseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


ALTER TABLE sample.samplesummaryseq OWNER TO postgres;

--
-- Name: sampleunitseq; Type: SEQUENCE; Schema: sample; Owner: postgres
--

CREATE SEQUENCE sample.sampleunitseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


ALTER TABLE sample.sampleunitseq OWNER TO postgres;

--
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);


--
-- Name: sample_unit sample_unit_sample_unit_ref_sample_summary_fk_key; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_unit
    ADD CONSTRAINT sample_unit_sample_unit_ref_sample_summary_fk_key UNIQUE (sample_unit_ref, sample_summary_fk);


--
-- Name: sample_summary samplesummary_pkey; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_summary
    ADD CONSTRAINT samplesummary_pkey PRIMARY KEY (sample_summary_pk);


--
-- Name: sample_summary samplesummary_uuid_key; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_summary
    ADD CONSTRAINT samplesummary_uuid_key UNIQUE (id);


--
-- Name: sample_summary_state samplesummarystate_pkey; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_summary_state
    ADD CONSTRAINT samplesummarystate_pkey PRIMARY KEY (state_pk);


--
-- Name: sample_unit sampleunit_pkey; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_unit
    ADD CONSTRAINT sampleunit_pkey PRIMARY KEY (sample_unit_pk);


--
-- Name: sample_unit sampleunit_uuid_key; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_unit
    ADD CONSTRAINT sampleunit_uuid_key UNIQUE (id);


--
-- Name: sample_unit_state sampleunitstate_pkey; Type: CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_unit_state
    ADD CONSTRAINT sampleunitstate_pkey PRIMARY KEY (state_pk);


--
-- Name: sample_unit_sample_unit_ref_index; Type: INDEX; Schema: sample; Owner: postgres
--

CREATE INDEX sample_unit_sample_unit_ref_index ON sample.sample_unit USING btree (sample_unit_ref);


--
-- Name: samplesummary_statefk_index; Type: INDEX; Schema: sample; Owner: postgres
--

CREATE INDEX samplesummary_statefk_index ON sample.sample_summary USING btree (state_fk);


--
-- Name: sampleunit_samplesummaryfk_index; Type: INDEX; Schema: sample; Owner: postgres
--

CREATE INDEX sampleunit_samplesummaryfk_index ON sample.sample_unit USING btree (sample_summary_fk);


--
-- Name: sampleunit_statefk_index; Type: INDEX; Schema: sample; Owner: postgres
--

CREATE INDEX sampleunit_statefk_index ON sample.sample_unit USING btree (state_fk);


--
-- Name: sample_unit samplesummary_fkey; Type: FK CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_unit
    ADD CONSTRAINT samplesummary_fkey FOREIGN KEY (sample_summary_fk) REFERENCES sample.sample_summary(sample_summary_pk);


--
-- Name: sample_summary statefk_fkey; Type: FK CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_summary
    ADD CONSTRAINT statefk_fkey FOREIGN KEY (state_fk) REFERENCES sample.sample_summary_state(state_pk);


--
-- Name: sample_unit statefk_fkey; Type: FK CONSTRAINT; Schema: sample; Owner: postgres
--

ALTER TABLE ONLY sample.sample_unit
    ADD CONSTRAINT statefk_fkey FOREIGN KEY (state_fk) REFERENCES sample.sample_unit_state(state_pk);


--
-- PostgreSQL database dump complete
--

