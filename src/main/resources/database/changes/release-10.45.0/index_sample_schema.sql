-- SAMPLE SERVICE

-- samplesummary


-- Index: sample.samplesummary_statefk_index
-- DROP INDEX sample.samplesummary_statefk_index;

CREATE INDEX samplesummary_statefk_index ON sample.samplesummary USING btree (stateFK);


---------------------------------------------------------------------------
---------------------------------------------------------------------------

-- sampleunit


-- Index: sample.sampleunit_samplesummaryfk_index
-- DROP INDEX sample.sampleunit_samplesummaryfk_index;

CREATE INDEX sampleunit_samplesummaryfk_index ON sample.sampleunit USING btree (samplesummaryfk);



-- Index: sample.sampleunit_statefk_index
-- DROP INDEX sample.sampleunit_statefk_index;

CREATE INDEX sampleunit_statefk_index ON sample.sampleunit USING btree (stateFK);