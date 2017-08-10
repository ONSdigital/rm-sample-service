CREATE OR REPLACE FUNCTION sample.testdata_teardown(p_sample boolean, p_collection_exercise boolean, p_case boolean, p_action boolean)
  RETURNS text AS
$BODY$
DECLARE 
v_message text;

BEGIN

-- select * from sample.testdata_teardown(true, true, true, true)

v_message := '';

-----------------
-----------------
-- Sample data --
-----------------
-----------------

IF p_sample = TRUE THEN 
   ALTER SEQUENCE sample.samplesummaryseq RESTART WITH 1;
   ALTER SEQUENCE sample.sampleunitseq    RESTART WITH 1;

   TRUNCATE sample.samplesummary CASCADE;
   TRUNCATE sample.sampleunit;   
   v_message := v_message || 'Sample ';
END IF;


------------------------------
------------------------------
-- Collection Exercise data --
------------------------------
------------------------------

IF p_collection_exercise = TRUE THEN 
   TRUNCATE collectionexercise.sampleunitgroup CASCADE;

   ALTER SEQUENCE collectionexercise.sampleunitgrouppkseq RESTART WITH 1;
   ALTER SEQUENCE collectionexercise.sampleunitpkseq      RESTART WITH 1;
   
   v_message := v_message || 'Collection Exercise ';
END IF;

---------------
---------------
-- Case data --
---------------
---------------

IF p_case = TRUE THEN 
   TRUNCATE casesvc.casegroup CASCADE;
   TRUNCATE casesvc.case      CASCADE;
   TRUNCATE casesvc.caseevent CASCADE;

   ALTER SEQUENCE casesvc.casegroupseq RESTART WITH 1;
   ALTER SEQUENCE casesvc.caseeventseq RESTART WITH 1;
   ALTER SEQUENCE casesvc.caseseq      RESTART WITH 1;
   
   v_message := v_message || 'Case ';
END IF;

-----------------
-----------------
-- Action data --
-----------------
-----------------

IF p_action = TRUE THEN 

   TRUNCATE action.case    CASCADE;
   TRUNCATE action.action  CASCADE;
   TRUNCATE action.actionplanjob;
   TRUNCATE action.messagelog;

   ALTER SEQUENCE action.actionpkseq      RESTART WITH 1;
   ALTER SEQUENCE action.casepkseq        RESTART WITH 1;
   ALTER SEQUENCE action.actionplanjobseq RESTART WITH 1;
   
   v_message := v_message || 'Action ';
END IF;


IF v_message = '' THEN 
   v_message := 'NOTHING DELETED'; 
ELSE 
   v_message := 'DELETED - ' || v_message;
END IF;
 
RETURN v_message;

 EXCEPTION
 WHEN OTHERS THEN  
    RETURN SQLERRM || ' SQLSTATE : ' || SQLSTATE ;
    
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;