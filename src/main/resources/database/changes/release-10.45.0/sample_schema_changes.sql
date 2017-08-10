
-- samplesummarystate table (rename state to statePK)
ALTER TABLE sample.samplesummarystate  DROP CONSTRAINT samplesummarystate_pkey CASCADE;
ALTER TABLE sample.samplesummarystate  RENAME COLUMN state TO statePK;
ALTER TABLE sample.samplesummarystate  ADD CONSTRAINT samplesummarystate_pkey PRIMARY KEY(statePK);

-- samplesummary table (rename state to stateFK)
ALTER TABLE sample.samplesummary  RENAME COLUMN state TO stateFK;
ALTER TABLE sample.samplesummary  ADD CONSTRAINT statefk_fkey FOREIGN KEY (statefk) REFERENCES sample.samplesummarystate(statePK);



-- sampleunitstate table (rename state to statePK)
ALTER TABLE sample.sampleunitstate  DROP CONSTRAINT sampleunitstate_pkey CASCADE;
ALTER TABLE sample.sampleunitstate  RENAME COLUMN state TO statePK;
ALTER TABLE sample.sampleunitstate  ADD CONSTRAINT sampleunitstate_pkey PRIMARY KEY(statePK);

-- sampleunit table (rename state to stateFK)
ALTER TABLE sample.sampleunit  RENAME COLUMN state TO stateFK;
ALTER TABLE sample.sampleunit  ADD CONSTRAINT statefk_fkey FOREIGN KEY (statefk) REFERENCES sample.sampleunitstate(statePK);



