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
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: sample; Owner: postgres
--

--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('9.39.0-1', 'Narinder Birk', 'database/changes/release-9.39.0/changelog.yml', '2022-07-13 07:09:33.947082', 1, 'EXECUTED', '8:eb257c20f655dfc6c797cfd13dedb436', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('9.39.0-2', 'Narinder Birk', 'database/changes/release-9.39.0/changelog.yml', '2022-07-13 07:09:34.025715', 2, 'EXECUTED', '8:9294d112dee117e6331d335bafbb1a81', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.43.1', 'Kieran Wardle', 'database/changes/release-10.43.0/changelog.yml', '2022-07-13 07:09:34.037325', 3, 'EXECUTED', '8:2f44d192ad500de0ade04205005b443a', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.45.0-1', 'Sarah Radford', 'database/changes/release-10.45.0/changelog.yml', '2022-07-13 07:09:34.066689', 4, 'EXECUTED', '8:94994e44c31fbfab8fcb507032b25333', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.45.0-2', 'Sarah Radford', 'database/changes/release-10.45.0/changelog.yml', '2022-07-13 07:09:34.128411', 5, 'EXECUTED', '8:312ee4ced14aceb41a0af89eea476e0f', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.46.0-1', 'Sarah Radford', 'database/changes/release-10.46.0/changelog.yml', '2022-07-13 07:09:34.247701', 6, 'EXECUTED', '8:dd38940097e11a0575734096db23d929', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.0-1', 'Sarah Radford', 'database/changes/release-10.49.0/changelog.yml', '2022-07-13 07:09:34.310742', 7, 'EXECUTED', '8:d423ad51cebb170762d8cad78c0100f2', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.0-2', 'Kieran Wardle', 'database/changes/release-10.49.0/changelog.yml', '2022-07-13 07:09:34.323597', 8, 'EXECUTED', '8:9e111cd080a614e709a9c263a08e6fd8', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.0-3', 'Edward Stevens', 'database/changes/release-10.49.0/changelog.yml', '2022-07-13 07:09:34.336345', 9, 'EXECUTED', '8:a55b6be2529dce0a7ac0012d15e8043a', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.0-4', 'Tejas Patel', 'database/changes/release-10.49.0/changelog.yml', '2022-07-13 07:09:34.348343', 10, 'EXECUTED', '8:9d13a01910348e344f025c6beb4f9306', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.0-5', 'Matt Innes', 'database/changes/release-10.49.0/changelog.yml', '2022-07-13 07:09:34.416244', 11, 'EXECUTED', '8:234c4470d88e52ba1338624b13ea0257', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.0-6', 'Matt Innes', 'database/changes/release-10.49.0/changelog.yml', '2022-07-13 07:09:34.433152', 12, 'EXECUTED', '8:26e9e3f96d185c10d21ebbd5b6b93141', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.0-7', 'Matt Innes', 'database/changes/release-10.49.0/changelog.yml', '2022-07-13 07:09:34.445027', 13, 'EXECUTED', '8:79b42681787f9e6007280eee748f341a', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('10.49.1-1', 'Adam Hawtin', 'database/changes/release-10.49.1/changelog.yml', '2022-07-13 07:09:34.465607', 14, 'EXECUTED', '8:76e0b7144a541b53df521dd3542c31e0', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('11-1', 'Nick Grant', 'database/changes/release-11/changelog.yml', '2022-07-13 07:09:34.522363', 15, 'EXECUTED', '8:c2a7b3a3fab7f0d8ea4da9b8614d4932', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('12-1', 'Adam Wilkie', 'database/changes/release-12/changelog.yml', '2022-07-13 07:09:34.535323', 16, 'EXECUTED', '8:6289de9b7df3e9b78bed3d932c471249', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('13-1', 'Amit Sinha', 'database/changes/release-13/changelog.yml', '2022-07-13 07:09:34.550536', 17, 'EXECUTED', '8:dd79bc12f0063cfe530e663988365bb8', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('14-1', 'Adam Wilkie', 'database/changes/release-14/changelog.yml', '2022-07-13 07:09:34.616906', 18, 'EXECUTED', '8:4c20e4f2d3daef69286eca80e89bf1f6', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('15-1', 'Warren Bailey', 'database/changes/release-15/changelog.yml', '2022-07-13 07:09:34.628716', 19, 'EXECUTED', '8:c38256297dd1e690f6e2df4ef45866e9', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('16-1', 'Matt Crocker', 'database/changes/release-16/changelog.yml', '2022-07-13 07:09:34.650021', 20, 'EXECUTED', '8:4fbf6968048dd950362b25052b58b648', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('17-1', 'Warren Bailey', 'database/changes/release-17/changelog.yml', '2022-07-13 07:09:34.711359', 21, 'EXECUTED', '8:817c5fc5313c5591aac0a5fe7665ef22', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('17-2', 'Warren Bailey', 'database/changes/release-17/changelog.yml', '2022-07-13 07:09:34.725951', 22, 'EXECUTED', '8:fadf923dc7d02fdad95276278dd61f93', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('17-3', 'Warren Bailey', 'database/changes/release-17/changelog.yml', '2022-07-13 07:09:34.738014', 23, 'EXECUTED', '8:691b021af6bcf588f89a6a0ee44eec6d', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('17-4', 'Warren Bailey', 'database/changes/release-17/changelog.yml', '2022-07-13 07:09:34.818492', 24, 'EXECUTED', '8:5b6f8eca61dee9911542407723f33a89', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('17-5', 'Warren Bailey', 'database/changes/release-17/changelog.yml', '2022-07-13 07:09:34.830871', 25, 'EXECUTED', '8:4af455fb496c2aa539a5dd3b2c252517', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('17-6', 'Adam Mann', 'database/changes/release-17/changelog.yml', '2022-07-13 07:09:34.842984', 26, 'EXECUTED', '8:a29e4d06de86dafd30608121e7ecf3fe', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('18-1', 'Adam Mann', 'database/changes/release-18/changelog.yml', '2022-07-13 07:09:34.855102', 27, 'EXECUTED', '8:98e069723a47ec30d097fc37b796f100', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('19-1', 'Warren Bailey', 'database/changes/release-19/changelog.yml', '2022-07-13 07:09:34.869551', 28, 'EXECUTED', '8:d7393d46834b2217addda4ce7af9f087', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20-1', 'Warren Bailey', 'database/changes/release-20/changelog.yml', '2022-07-13 07:09:34.884269', 29, 'EXECUTED', '8:af526013ab311ebde2667a97887a31b6', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');
--INSERT INTO sample.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('21-1', 'Jacob Harrison', 'database/changes/release-21/changelog.yml', '2022-07-13 07:09:34.894872', 30, 'EXECUTED', '8:43677b5a1e14d6e462abbaa76a3506e9', 'sqlFile', '', NULL, '3.10.3', NULL, NULL, '7696173721');


--
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: sample; Owner: postgres
--

--INSERT INTO sample.databasechangeloglock (id, locked, lockgranted, lockedby) VALUES (1, false, NULL, NULL);


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

