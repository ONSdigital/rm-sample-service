--create sequence for message logging
CREATE SEQUENCE sample.messagelogseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;

--create messagelog table
CREATE TABLE sample.messagelog
(
  messagelogpk bigint NOT NULL DEFAULT nextval('sample.messagelogseq'::regclass),
  messagetext character varying,
  jobid numeric,
  messagelevel character varying,
  functionname character varying,
  createddatetime timestamp with time zone,
  CONSTRAINT messagelogpk_pkey PRIMARY KEY (messagelogpk)
);


-- Sequence: sample.reportPKseq
-- DROP SEQUENCE sample.reportPKseq;

CREATE SEQUENCE sample.reportPKseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;


-- Table: sample.reporttype
-- DROP TABLE sample.reporttype;

CREATE TABLE sample.reporttype
(
    reporttypePK  character varying (20),
    displayorder  integer,
    displayname   character varying(40),
    CONSTRAINT reporttype_pkey PRIMARY KEY (reporttypePK)
);


-- Table: sample.report
-- DROP TABLE sample.report;

CREATE TABLE sample.report
(
    id             uuid NOT NULL,
    reportPK       bigint NOT NULL,
    reporttypeFK   character varying (20),
    contents       text ,
    createddatetime timestamp with time zone,
    CONSTRAINT report_pkey PRIMARY KEY (reportpk),
    CONSTRAINT report_uuid_key UNIQUE (id),
    CONSTRAINT reporttypefk_fkey FOREIGN KEY (reporttypefk)
    REFERENCES sample.reporttype (reporttypepk) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Function: sample.logmessage(text, numeric, text, text)

-- DROP FUNCTION sample.logmessage(text, numeric, text, text);

CREATE OR REPLACE FUNCTION sample.logmessage(p_messagetext text DEFAULT NULL::text, p_jobid numeric DEFAULT NULL::numeric, p_messagelevel text DEFAULT NULL::text, p_functionname text DEFAULT NULL::text)
  RETURNS boolean AS
$BODY$
DECLARE
v_text TEXT ;
v_function TEXT;
BEGIN
INSERT INTO sample.messagelog
(messagetext, jobid, messagelevel, functionname, createddatetime )
values (p_messagetext, p_jobid, p_messagelevel, p_functionname, current_timestamp);
  RETURN TRUE;
EXCEPTION
WHEN OTHERS THEN
RETURN FALSE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


-- Insert reports into report tables 
INSERT INTO sample.reporttype(reporttypePK,displayorder,displayname) VALUES('SAMPLE',20,'Sample Units');




-- Function: sample.generate_sample_mi()

-- DROP FUNCTION sample.generate_sample_mi();

CREATE OR REPLACE FUNCTION sample.generate_sample_mi()
  RETURNS boolean AS
$BODY$
DECLARE

v_contents      text;
r_dataline      record;
v_rows          integer;

BEGIN
    
    PERFORM sample.logmessage(p_messagetext := 'GENERATING SAMPLE MI REPORTS'
                              ,p_jobid := 0
                              ,p_messagelevel := 'INFO'
                              ,p_functionname := 'sample.generate_sample_mi');  
    
       v_rows     := 0;
       v_contents := '';
       v_contents := 'Sample Unit Ref,Form Type';

-- sample Report

       FOR r_dataline IN (SELECT  s.sampleunitref, s.formtype FROM sample.sampleunit s ORDER BY s.sampleunitref) LOOP

                           v_contents := v_contents     || chr(10) 
                           || r_dataline.sampleunitref  || ','
                           || r_dataline.formtype  ;   
             v_rows := v_rows+1;  
       END LOOP;       

       -- Insert the data into the report table
       INSERT INTO sample.report (id, reportPK,reporttypeFK,contents, createddatetime) VALUES(gen_random_uuid(), nextval('sample.reportPKseq'), 'SAMPLE', v_contents, CURRENT_TIMESTAMP); 

               
       PERFORM sample.logmessage(p_messagetext := 'GENERATING SAMPLE MI REPORT COMPLETED ROWS WRIITEN = ' || v_rows
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'sample.generate_sample_mi'); 
      
    
       PERFORM sample.logmessage(p_messagetext := 'SAMPLE MI REPORT GENERATED'
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'sample.generate_sample_mi');
  
  RETURN TRUE;

  EXCEPTION
  WHEN OTHERS THEN   
     PERFORM sample.logmessage(p_messagetext := 'GENERATE REPORTS EXCEPTION TRIGGERED SQLERRM: ' || SQLERRM || ' SQLSTATE : ' || SQLSTATE
                               ,p_jobid := 0
                               ,p_messagelevel := 'FATAL'
                               ,p_functionname := 'sample.generate_sample_mi');
                               
  RETURN FALSE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  COST 100;