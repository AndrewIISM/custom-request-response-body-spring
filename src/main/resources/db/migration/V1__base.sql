create table first_entity
(
    id bigserial primary key,
    title varchar
);

insert into first_entity(title)
    values ('value1'), ('value2')