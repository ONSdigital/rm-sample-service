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
-- Data for Name: sample_summary_state; Type: TABLE DATA; Schema: sample; Owner: postgres
--

INSERT INTO sample.sample_summary_state (state_pk) VALUES ('INIT');
INSERT INTO sample.sample_summary_state (state_pk) VALUES ('ACTIVE');
INSERT INTO sample.sample_summary_state (state_pk) VALUES ('FAILED');
INSERT INTO sample.sample_summary_state (state_pk) VALUES ('COMPLETE');


--
-- Data for Name: sample_summary; Type: TABLE DATA; Schema: sample; Owner: postgres
--



--
-- Data for Name: sample_unit_state; Type: TABLE DATA; Schema: sample; Owner: postgres
--

INSERT INTO sample.sample_unit_state (state_pk) VALUES ('INIT');
INSERT INTO sample.sample_unit_state (state_pk) VALUES ('DELIVERED');
INSERT INTO sample.sample_unit_state (state_pk) VALUES ('PERSISTED');
INSERT INTO sample.sample_unit_state (state_pk) VALUES ('FAILED');


--
-- Data for Name: sample_unit; Type: TABLE DATA; Schema: sample; Owner: postgres
--



--
-- Name: collectionexercisejobseq; Type: SEQUENCE SET; Schema: sample; Owner: postgres
--

SELECT pg_catalog.setval('sample.collectionexercisejobseq', 1, false);


--
-- Name: messagelogseq; Type: SEQUENCE SET; Schema: sample; Owner: postgres
--

SELECT pg_catalog.setval('sample.messagelogseq', 1, false);


--
-- Name: reportpkseq; Type: SEQUENCE SET; Schema: sample; Owner: postgres
--

SELECT pg_catalog.setval('sample.reportpkseq', 1, false);


--
-- Name: samplesummaryseq; Type: SEQUENCE SET; Schema: sample; Owner: postgres
--

SELECT pg_catalog.setval('sample.samplesummaryseq', 1, false);


--
-- Name: sampleunitseq; Type: SEQUENCE SET; Schema: sample; Owner: postgres
--

SELECT pg_catalog.setval('sample.sampleunitseq', 1, false);


--
-- PostgreSQL database dump complete
--

