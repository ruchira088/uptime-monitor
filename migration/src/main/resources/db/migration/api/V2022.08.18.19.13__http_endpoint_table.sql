CREATE TABLE http_endpoint (
    id VARCHAR(63),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    http_method VARCHAR(8) NOT NULL,
    url VARCHAR(255) NOT NULL,

    PRIMARY KEY (id)
);