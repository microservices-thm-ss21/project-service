create table  projects (
    id numeric primary key,
    name varchar(50),
    create_time timestamp
);

insert into projects values (0, 'TestProject', current_timestamp)
