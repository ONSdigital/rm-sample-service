ALTER TABLE sample.samplesummary DROP COLUMN surveyref;

ALTER TABLE sample.samplesummary DROP COLUMN effectivestartdatetime;

ALTER TABLE sample.samplesummary DROP COLUMN effectiveenddatetime;

ALTER TABLE sample.samplesummary ADD COLUMN description character varying (250);