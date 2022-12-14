CREATE TABLE health_check (
    id VARCHAR(63),
    name VARCHAR(63) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    user_id VARCHAR(63) NOT NULL,
    http_endpoint_id VARCHAR(63) UNIQUE NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_health_check_user_id FOREIGN KEY (user_id) REFERENCES api_user(id),
    CONSTRAINT fk_health_check_http_endpoint_id FOREIGN KEY (http_endpoint_id) REFERENCES http_endpoint(id)
);