-- migrate to V2.1 12/JULY/2021
ALTER TABLE todojob RENAME TO old_todojob;

CREATE TABLE IF NOT EXISTS todojob (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- "Primary key"
    name VARCHAR(255) NOT NULL, -- "Name or description of the todojob"
    is_done TINYINT NOT NULL, -- "1-true, 0-false"
    expected_end_date DATE NOT NULL, -- "the expected end date"
    actual_end_date DATE -- "the actual finish data"
);

CREATE INDEX IF NOT EXISTS sort_idx ON todojob (is_done ASC, start_date DESC);

INSERT INTO todojob (id, name, is_done, expected_end_date)
    SELECT id, name, is_done, start_date
    FROM old_todojob;

--DROP TABLE old_todojob;
