ALTER TABLE sample.collectionexercisejob ADD COLUMN jobcomplete boolean;
UPDATE sample.collectionexercisejob SET jobcomplete = 'f';
ALTER TABLE sample.collectionexercisejob ALTER COLUMN jobcomplete SET NOT NULL;
ALTER TABLE sample.collectionexercisejob ALTER COLUMN jobcomplete SET DEFAULT FALSE;