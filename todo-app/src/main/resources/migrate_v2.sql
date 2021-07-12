-- migrate to V2.0 12/JULY/2021
ALTER TABLE todojob MODIFY COLUMN start_date expected_end_date DATE NOT NULL;
ALTER TABLE todojob ADD COLUMN actual_end_date DATE;

UPDATE todojob
SET actual_end_date = expected_start_date;
