CREATE TABLE IF NOT EXISTS todojob (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- "Primary key"
    name VARCHAR(255) NOT NULL, -- "Name or description of the todojob"
    is_done TINYINT NOT NULL, -- "1-true, 0-false"
    expected_end_date DATE NOT NULL, -- "the expected end date"
    actual_end_date DATE -- "the actual finish data"
);

-- 07/AUG/2021 remove index
-- CREATE INDEX IF NOT EXISTS sort_idx ON todojob (is_done ASC, expected_end_date DESC);
-- DROP INDEX IF EXISTS sort_idx;
