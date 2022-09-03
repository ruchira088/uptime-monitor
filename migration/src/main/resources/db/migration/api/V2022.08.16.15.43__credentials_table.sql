CREATE TABLE user_credentials (
    user_id VARCHAR(63),
    created_at TIMESTAMP NOT NULL,
    hashed_password VARCHAR(127) NOT NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT fk_crendetials_user_id FOREIGN KEY (user_id) REFERENCES api_user(id)
);