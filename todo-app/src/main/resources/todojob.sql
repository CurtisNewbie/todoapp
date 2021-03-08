CREATE TABLE IF NOT EXISTS todojob (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- "Primary key"
    name VARCHAR(255) NOT NULL,
    is_done TINYINT NOT NULL, -- "1-true, 0-false"
    start_date DATE NOT NULL
)