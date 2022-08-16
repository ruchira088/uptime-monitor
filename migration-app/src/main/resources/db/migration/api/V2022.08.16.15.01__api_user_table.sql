CREATE TABLE api_user (
    id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    email VARCHAR(128) UNIQUE NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,

    PRIMARY KEY (id)
);