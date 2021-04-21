create table users (
    id numeric primary key,
    username varchar(50),
    name varchar(50),
    last_name varchar(50),
    date_of_birth date,
    create_time timestamp,
    global_role varchar(20),
    last_login timestamp null
);

insert into users values (0, 'xXRausAusDenSchulden69Xx', 'Peter', 'Zwegat', current_date, current_timestamp, 'SCHULDENBERATER', current_timestamp)
