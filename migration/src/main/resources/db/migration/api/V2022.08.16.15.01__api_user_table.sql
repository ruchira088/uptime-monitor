CREATE TABLE api_user (
    id VARCHAR(63),
    created_at TIMESTAMP NOT NULL,
    email VARCHAR(127) UNIQUE NOT NULL,
    first_name VARCHAR(63) NOT NULL,
    last_name VARCHAR(63) NOT NULL,

    PRIMARY KEY (id)
);