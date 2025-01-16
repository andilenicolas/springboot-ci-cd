CREATE TABLE users (
    email character varying NOT NULL UNIQUE,
    id uuid NOT NULL UNIQUE,
    updated_at timestamp,
    deleted_at timestamp,
    created_at timestamp NOT NULL,
    is_deleted boolean NOT NULL
);