DROP TABLE IF EXISTS users;
CREATE TABLE users (
    username VARCHAR(255) UNIQUE NOT NULL,
    hashed_password CHAR(60) NOT NULL,
    user_id SERIAL,
    PRIMARY KEY (user_id)
);
