ALTER TABLE sample.collectionexercisejob ADD COLUMN jobcomplete boolean;

-- Mark all the existing jobs as "not complete" so that anything that was in-flight when the upgrade
-- is run will be safely resumed and the service will eventually get back to a consistent state
UPDATE sample.collectionexercisejob SET jobcomplete = 'f';

ALTER TABLE sample.collectionexercisejob ALTER COLUMN jobcomplete SET NOT NULL;
ALTER TABLE sample.collectionexercisejob ALTER COLUMN jobcomplete SET DEFAULT FALSE;