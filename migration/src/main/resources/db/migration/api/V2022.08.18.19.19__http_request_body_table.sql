CREATE TABLE http_request_body (
    id VARCHAR(63),
    created_at TIMESTAMP NOT NULL,
    http_endpoint_id VARCHAR(63) UNIQUE NOT NULL,
    content_type VARCHAR(63) NOT NULL,
    body TEXT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_http_request_body_http_endpoint_id FOREIGN KEY (http_endpoint_id) REFERENCES http_endpoint(id)
);