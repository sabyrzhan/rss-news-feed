create table users
(
    id       serial
        constraint users_pk
            primary key,
    username varchar not null,
    password varchar not null
);