CREATE TABLE IF NOT EXISTS todojob (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- "Primary key"
    name VARCHAR(255) NOT NULL,
    is_done TINYINT NOT NULL, -- "1-true, 0-false"
    start_date DATE NOT NULL
)

CREATE INDEX IF NOT EXISTS sort_idx ON todojob (start_date DESC, is_done ASC);
