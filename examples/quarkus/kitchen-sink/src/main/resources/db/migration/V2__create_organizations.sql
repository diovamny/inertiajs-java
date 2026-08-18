CREATE SEQUENCE organizations_seq START WITH 1;

CREATE TABLE organizations (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);