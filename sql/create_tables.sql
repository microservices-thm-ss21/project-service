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
    project_role varchar(20),
    CONSTRAINT fk_project
        FOREIGN KEY(project_id)
            REFERENCES projects(id)
);



insert into projects values ('e42e93d1-56dc-4687-9039-beb25dc1732a', 'TestProject0', '8d8fa2d7-b999-4e07-9739-c563ee9fb12b',current_timestamp);
insert into projects values ('e86c57cb-d703-4f39-9632-3782cb5500e8', 'TestProject1', '18525d18-76d9-4057-ace0-7a69c8cc0907',current_timestamp);
insert into members values ('e42e93d1-56dc-4687-9039-beb25dc1732a', '18525d18-76d9-4057-ace0-7a69c8cc0907', 'admin')

