CREATE SEQUENCE notes_seq START WITH 1;

CREATE TABLE notes (
    id BIGINT NOT NULL PRIMARY KEY,
    body TEXT NOT NULL,
    contact_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_notes_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE,
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);