CREATE SEQUENCE users_seq START WITH 1;

CREATE TABLE users (
    id BIGINT NOT NULL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    first_name VARCHAR(25) NOT NULL,
    last_name VARCHAR(25) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    email_verified_at TIMESTAMP,
    password VARCHAR(255),
    owner BOOLEAN DEFAULT FALSE,
    photo_path VARCHAR(100),
    remember_token VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_users_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);
