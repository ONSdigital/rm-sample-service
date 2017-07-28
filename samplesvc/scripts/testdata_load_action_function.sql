-- Function: sample.testdata_load_action(text)

-- DROP FUNCTION sample.testdata_load_action(text);

CREATE OR REPLACE FUNCTION sample.testdata_load_action(p_actionplanstartdate text)
  RETURNS text AS
$BODY$
DECLARE 

-- example call -  select * from sample.testdata_load_action('2016-07-20')

-- actions
v_actionplanstartdate date;
v_createactions_return boolean;
v_ac_rows integer;
v_a_rows_before integer;
v_a_rows_after integer;
v_a_rows integer;
v_loop integer;
r_job record;
v_message text;

BEGIN

-----------------
-----------------
-- Action data --
-----------------
-----------------

   v_message := '';

   -- Get the collection exercise start date
   -- Load into action.case cases that haven't already been loaded
   -- Override the start date if supplied

   INSERT INTO action.case
   SELECT  c.actionplanid
         , c.id
         , nextval('action.casepkseq')
         , ap.actionplanpk
         , CASE WHEN UPPER(p_actionplanstartdate) <> 'DEFAULT' THEN  p_actionplanstartdate::date ELSE ce.scheduledstartdatetime END
         , ce.scheduledenddatetime
   FROM casesvc.case c
      , casesvc.casegroup cg
      , action.actionplan ap
      , collectionexercise.collectionexercise ce
   WHERE cg.casegrouppk = c.casegroupfk
   AND  ce.id = cg.collectionexerciseid
   AND  ap.id = c.actionplanid
   AND  c.id IN(SELECT  c.id FROM casesvc.case c EXCEPT(SELECT a.id FROM action.case a));

   GET DIAGNOSTICS v_ac_rows = ROW_COUNT;

   SELECT COUNT(*) from action.action INTO v_a_rows_before;

   -- For each unique actionplan on the case table create a actionplanjob and create the actions
   FOR r_job IN (SELECT DISTINCT c.actionplanfk FROM action.case c) LOOP
      INSERT INTO action.actionplanjob(id, actionplanjobpk, actionplanfk, createdby, statefk,createddatetime)            
      SELECT gen_random_uuid()
           , nextval('action.actionplanjobseq')
           , r_job.actionplanfk
           , 'TESTDATA'
           , 'COMPLETED'
           , now();

       -- create actions using the function
       SELECT * FROM action.createactions(currval('action.actionplanjobseq')::integer) INTO v_createactions_return;
   END LOOP;

   SELECT COUNT(*) from action.action INTO v_a_rows_after;

   v_a_rows := v_a_rows_after - v_a_rows_before;
   v_message := v_message || v_ac_rows || ' NEW ROWS INSERTED action.case, ' || v_a_rows || ' NEW ROWS INSERTED action.action';

RETURN v_message;

 EXCEPTION
 WHEN OTHERS THEN RETURN 'SQLERRM: ' || SQLERRM || ' SQLSTATE : ' || SQLSTATE;    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
