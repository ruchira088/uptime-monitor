CREATE TABLE http_header (
    id VARCHAR(63),
    created_at TIMESTAMP NOT NULL,
    http_endpoint_id VARCHAR(63) NOT NULL,
    header_name VARCHAR(255) NOT NULL,
    header_value VARCHAR(255),

    PRIMARY KEY (id),
    CONSTRAINT fk_http_header_http_endpoint_id FOREIGN KEY (http_endpoint_id) REFERENCES http_endpoint(id)
);