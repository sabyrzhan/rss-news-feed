create table feeds
(
    id          SERIAL                          not null constraint feeds_pk primary key,
    user_id     int                             not null constraint feeds_users_id_fk references users (id),
    title       varchar(255)                    not null,
    url         varchar(255)                    not null,
    text        varchar(500)                    not null,
    create_date timestamp without time zone     not null
);