CREATE SEQUENCE organizations_seq START WITH 1;

CREATE TABLE organizations (
    id BIGINT NOT NULL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(50),
    phone VARCHAR(50),
    address VARCHAR(150),
    city VARCHAR(50),
    region VARCHAR(50),
    country VARCHAR(2),
    postal_code VARCHAR(25),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_organizations_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);
