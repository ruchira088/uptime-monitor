CREATE TABLE http_endpoint_body (
    id VARCHAR(48),
    created_at TIMESTAMP NOT NULL,
    http_endpoint_id VARCHAR(48) UNIQUE NOT NULL,
    body VARCHAR(2047) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_http_endpoint_body_http_endpoint_id FOREIGN KEY (http_endpoint_id) REFERENCES http_endpoint(id)
);