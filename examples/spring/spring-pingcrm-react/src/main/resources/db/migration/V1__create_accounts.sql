CREATE SEQUENCE accounts_seq START WITH 1;

CREATE TABLE accounts (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
