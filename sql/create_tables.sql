create table  projects (
    id numeric primary key,
    name varchar(50),
    create_time timestamp
);

insert into projects values (0, 'TestProject0', current_timestamp);
insert into projects values (1, 'TestProject1', current_timestamp)
