CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table projects (
    id uuid primary key,
    name varchar(50),
    create_time timestamp
);

insert into projects values (uuid_generate_v4(), 'TestProject0', current_timestamp);
insert into projects values (uuid_generate_v4(), 'TestProject1', current_timestamp)
