ALTER TABLE sample.sampleunit RENAME COLUMN sampleunitpk TO sample_unit_pk;
ALTER TABLE sample.sampleunit RENAME COLUMN samplesummaryfk TO sample_summary_fk;
ALTER TABLE sample.sampleunit RENAME COLUMN sampleunitref TO sample_unit_ref;
ALTER TABLE sample.sampleunit RENAME COLUMN sampleunittype TO sample_unit_type;
ALTER TABLE sample.sampleunit RENAME COLUMN formtype TO form_type;
ALTER TABLE sample.sampleunit RENAME COLUMN statefk TO state_fk;
ALTER TABLE sample.sampleunit RENAME TO sample_unit;

ALTER TABLE sample.sampleunitstate RENAME COLUMN statepk TO state_pk;
ALTER TABLE sample.sampleunitstate RENAME TO sample_unit_state;

ALTER TABLE sample.samplesummary RENAME COLUMN samplesummarypk TO sample_summary_pk;
ALTER TABLE sample.samplesummary RENAME COLUMN statefk TO state_fk;
ALTER TABLE sample.samplesummary RENAME COLUMN ingestdatetime TO ingest_date_time;
ALTER TABLE sample.samplesummary RENAME COLUMN totalsampleunits TO total_sample_units;
ALTER TABLE sample.samplesummary RENAME COLUMN expectedcollectioninstruments TO expected_collection_instruments;
ALTER TABLE sample.samplesummary RENAME TO sample_summary;

ALTER TABLE sample.samplesummarystate RENAME COLUMN statepk TO state_pk;
ALTER TABLE sample.samplesummarystate RENAME TO sample_summary_state;

ALTER TABLE sample.collectionexercisejob RENAME COLUMN collectionexercisejobpk TO collection_exercise_job_pk;
ALTER TABLE sample.collectionexercisejob RENAME COLUMN collectionexerciseid TO collection_exercise_id;
ALTER TABLE sample.collectionexercisejob RENAME COLUMN surveyref TO survey_ref;
ALTER TABLE sample.collectionexercisejob RENAME COLUMN exercisedatetime TO exercise_date_time;
ALTER TABLE sample.collectionexercisejob RENAME COLUMN createddatetime TO created_date_time;
ALTER TABLE sample.collectionexercisejob RENAME COLUMN samplesummaryid TO sample_summary_id;
ALTER TABLE sample.collectionexercisejob RENAME COLUMN jobcomplete TO job_complete;
ALTER TABLE sample.collectionexercisejob RENAME TO collection_exercise_job;