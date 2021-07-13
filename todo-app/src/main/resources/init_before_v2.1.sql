-- script before V2.1 (i.e., before 12/JULY/2021)
CREATE TABLE IF NOT EXISTS todojob (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- "Primary key"
    name VARCHAR(255) NOT NULL,
    is_done TINYINT NOT NULL, -- "1-true, 0-false"
    start_date DATE NOT NULL
)

CREATE INDEX IF NOT EXISTS sort_idx ON todojob (is_done ASC, start_date DESC);
