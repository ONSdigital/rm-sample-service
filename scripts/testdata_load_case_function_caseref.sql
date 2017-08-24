-- Function: sample.testdata_load_case(text, text, text)

-- DROP FUNCTION sample.testdata_load_case(text, text, text);

CREATE OR REPLACE FUNCTION sample.testdata_load_case(p_surveyref text, p_exercise_name text, p_case_state text)
  RETURNS text AS
$BODY$
DECLARE 

-- select * from sample.testdata_load_case('221', 'BRES_2016',  'SAMPLED_INIT')

-- survey
v_surveyref text;
v_surveypk integer;
v_exercise_name text;
v_exercisepk integer;
v_exerciseid uuid;
v_case_state text;
v_cg_rows integer;
v_c_rows  integer;
v_ce_rows  integer;

BEGIN
v_surveyref       := p_surveyref;
v_exercise_name   := p_exercise_name;     

-- Get the exercise
SELECT s.surveypk, ce.exercisepk, ce.id 
FROM  collectionexercise.survey s 
    , collectionexercise.collectionexercise ce
WHERE s.surveyref = v_surveyref 
AND  ce.name = v_exercise_name
AND  ce.surveyfk = s.surveypk INTO v_surveypk, v_exercisepk, v_exerciseid;

---------------
---------------
-- Case data --
---------------
---------------

SELECT statepk FROM casesvc.casestate WHERE statepk = p_case_state INTO v_case_state;

-- Check if cases already created for this exercise
IF NOT EXISTS (SELECT 1 FROM casesvc.casegroup cg WHERE cg.collectionexerciseid = v_exerciseid) THEN 

INSERT INTO casesvc.casegroup(casegrouppk, id, collectionexerciseid, sampleunitref, sampleunittype) 
   SELECT  nextval('casesvc.casegroupseq')
         , gen_random_uuid()
         , v_exerciseid
         , u.sampleunitref
         , u.sampleunittypefk
   FROM   collectionexercise.sampleunitgroup g
        , collectionexercise.sampleunit u
    WHERE g.exercisefk = v_exercisepk
    AND u.sampleunitgroupfk = g.sampleunitgrouppk;

   GET DIAGNOSTICS v_cg_rows = ROW_COUNT;

  
   INSERT INTO casesvc.case(casepk, id, casegroupfk, casegroupid,caseref, sampleunittype, statefk, actionplanid, createddatetime, createdby) 
   SELECT  nextval('casesvc.caseseq')
         , gen_random_uuid()
         , cg.casegrouppk
         , cg.id
         , nextval('casesvc.caserefseq')
         , cg.sampleunittype
         , v_case_state
         , ap.plan
         , current_timestamp
         , 'SYSTEM'
   FROM  casesvc.casegroup cg
       , (SELECT COALESCE(ovr.actionplanid,df.actionplanid) AS plan,  COALESCE(ovr.sampleunittypefk ,df.sampleunittypefk) as type
          FROM (SELECT o.* FROM collectionexercise.casetypeoverride o WHERE o.exercisefk = v_exercisepk) ovr
          RIGHT OUTER JOIN (SELECT d.* FROM collectionexercise.casetypedefault d WHERE d.surveyfk = v_surveypk) df
          ON ovr.sampleunittypeFK = df.sampleunittypeFK)ap
   WHERE cg.sampleunittype = ap.type
   AND cg.collectionexerciseid = v_exerciseid;

   GET DIAGNOSTICS v_c_rows = ROW_COUNT;

   INSERT INTO casesvc.caseevent
   SELECT   nextval('casesvc.caseeventseq')
          , c.casepk
          , 'Case Created'
          , 'SYSTEM'
          , current_timestamp
          , 'CASE_CREATED'
   FROM  casesvc.case c
       , casesvc.casegroup cg       
   WHERE cg.casegrouppk = c.casegroupfk
   AND   cg.collectionexerciseid = v_exerciseid;
       
   GET DIAGNOSTICS v_ce_rows = ROW_COUNT; 

   RETURN 'SURVEY ' || v_surveyref || ', Exercise Name ' || v_exercise_name 
   || ' - ROWS INSERTED casesvc.casegroup = ' || v_cg_rows || ' ROWS INSERTED casesvc.case (' ||v_case_state || ') = '  || v_c_rows || ' ROWS INSERTED casesvc.caseevent = ' || v_ce_rows;

ELSE
   RETURN 'This Collection Exercise has already been loaded into the case schema';
END IF;

EXCEPTION
WHEN OTHERS THEN RETURN 'SQLERRM: ' || SQLERRM || ' SQLSTATE : ' || SQLSTATE;    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
