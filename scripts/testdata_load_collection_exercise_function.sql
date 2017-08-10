-- Function: sample.testdata_load_collection_exercise(text)

-- DROP FUNCTION sample.testdata_load_collection_exercise(text);

CREATE OR REPLACE FUNCTION sample.testdata_load_collection_exercise(p_surveyref text)
  RETURNS text AS
$BODY$
DECLARE 
-- survey
v_surveyref text;
r_record record;
v_su_rows integer;
v_sg_rows integer;
v_sampleunitgrouppkseq bigint;

BEGIN

v_surveyref := p_surveyref;
v_su_rows := 0;
v_sg_rows := 0;

-- select * from sample.testdata_load_collection_exercise('221')

-----------------------------------
-----------------------------------
-- Load Collection Exercise data --
-----------------------------------
-----------------------------------

FOR r_record IN (SELECT e.exercisepk,s.formtype,s.sampleunitref, s.sampleunittype
                 FROM  sample.samplesummary ss
                     , sample.sampleunit s
                     , collectionexercise.collectionexercise e
                     , collectionexercise.survey es
                 WHERE ss.surveyref = v_surveyref
                 AND e.surveyfk = es.surveypk
                 AND es.surveyref = ss.surveyref
                 AND s.samplesummaryFK = ss.samplesummarypk
                 AND e.exercisepk NOT IN (SELECT DISTINCT sg.exercisefk FROM collectionexercise.sampleunitgroup sg)) LOOP

     INSERT INTO collectionexercise.sampleunitgroup (sampleunitgrouppk, exercisefk, formtype,statefk,createddatetime,modifieddatetime)
     VALUES (nextval('collectionexercise.sampleunitgrouppkseq'),r_record.exercisepk, r_record.formtype, 'INIT', now(), now());     

     v_sampleunitgrouppkseq := currval('collectionexercise.sampleunitgrouppkseq');
 
     INSERT INTO collectionexercise.sampleunit (sampleunitgroupfk, collectioninstrumentid, partyid,sampleunitref, sampleunittypefk)
     VALUES (v_sampleunitgrouppkseq, NULL, NULL, r_record.sampleunitref, r_record.sampleunittype);

     v_su_rows := v_su_rows + 1;
     v_sg_rows := v_sg_rows + 1;
END LOOP;


RETURN 'RECORDS INSERTED INTO collectionexercise.sampleunitgroup = ' || v_sg_rows || ' RECORDS INSERTED INTO collectionexercise.sampleunit = ' || v_su_rows ;

 EXCEPTION
 WHEN OTHERS THEN  
    RETURN 'SQLERRM: ' || SQLERRM || ' SQLSTATE : ' || SQLSTATE;    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
