CREATE SEQUENCE contacts_seq START WITH 1;

CREATE TABLE contacts (
    id BIGINT NOT NULL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    organization_id BIGINT,
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_contacts_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
);