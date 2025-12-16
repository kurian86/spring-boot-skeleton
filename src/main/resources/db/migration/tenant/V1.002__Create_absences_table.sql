CREATE TABLE absences
(
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date   DATE,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
)
