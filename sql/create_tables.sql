CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table projects (
    id uuid primary key DEFAULT uuid_generate_v4(),
    name varchar(50),
    creator_id uuid,
    create_time timestamp
);

create table members (
    project_id uuid,
    user_id uuid,
    project_role varchar(30),
    CONSTRAINT fk_project
        FOREIGN KEY(project_id)
            REFERENCES projects(id)
);



insert into projects values (uuid_generate_v4(), 'TestProject0', uuid_generate_v4(),current_timestamp);
insert into projects values (uuid_generate_v4(), 'TestProject1', uuid_generate_v4(),current_timestamp)
