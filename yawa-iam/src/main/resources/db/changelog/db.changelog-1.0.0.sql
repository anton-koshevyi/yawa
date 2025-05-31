--liquibase formatted sql

--changeset koshevyi:20250531135707
create table users (
       id uuid primary key default gen_random_uuid()
     , email varchar(255) unique
     , password_hash varchar(255) not null
);


--changeset koshevyi:20250531135721
create table email_requests (
       id uuid primary key default gen_random_uuid()
     , actual_email varchar(255)
     , requested_email varchar(255) not null
     , created_at timestamp not null
);


--changeset koshevyi:20250531135746
create table sessions (
       id uuid primary key default gen_random_uuid()
     , user_id uuid not null
     , accessed_at timestamp not null
     , constraint fk_session_user
       foreign key (user_id)
       references users (id)
);


--changeset koshevyi:20250531135804
create table session_refresh_tokens (
       session_id uuid primary key
     , content_cipher varchar(255) unique
     , created_at timestamp not null
     , constraint fk_session_refresh_token_session
       foreign key (session_id)
       references sessions (id)
);
