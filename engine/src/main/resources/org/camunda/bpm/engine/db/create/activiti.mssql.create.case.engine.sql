-- create case definition table --
create table ACT_RE_CASE_DEF (
    ID_ nvarchar(64) not null,
    REV_ int,
    CATEGORY_ nvarchar(255),
    NAME_ nvarchar(255),
    KEY_ nvarchar(255) not null,
    VERSION_ int not null,
    DEPLOYMENT_ID_ nvarchar(64),
    RESOURCE_NAME_ nvarchar(4000),
    DGRM_RESOURCE_NAME_ nvarchar(4000),
    TENANT_ID_ nvarchar(64),
    HISTORY_TTL_ int,
    primary key (ID_)
);

-- create case execution table --
create table ACT_RU_CASE_EXECUTION (
    ID_ nvarchar(64) NOT NULL,
    REV_ int,
    CASE_INST_ID_ nvarchar(64),
    SUPER_CASE_EXEC_ nvarchar(64),
    SUPER_EXEC_ nvarchar(64),
    BUSINESS_KEY_ nvarchar(255),
    PARENT_ID_ nvarchar(64),
    CASE_DEF_ID_ nvarchar(64),
    ACT_ID_ nvarchar(255),
    PREV_STATE_ int,
    CURRENT_STATE_ int,
    REQUIRED_ tinyint,
    TENANT_ID_ nvarchar(64),
    primary key (ID_)
);

-- create case sentry part table --

create table ACT_RU_CASE_SENTRY_PART (
    ID_ nvarchar(64) NOT NULL,
    REV_ int,
    CASE_INST_ID_ nvarchar(64),
    CASE_EXEC_ID_ nvarchar(64),
    SENTRY_ID_ nvarchar(255),
    TYPE_ nvarchar(255),
    SOURCE_CASE_EXEC_ID_ nvarchar(64),
    STANDARD_EVENT_ nvarchar(255),
    SOURCE_ nvarchar(255),
    VARIABLE_EVENT_ nvarchar(255),
    VARIABLE_NAME_ nvarchar(255),
    SATISFIED_ tinyint,
    TENANT_ID_ nvarchar(64),
    primary key (ID_)
);

-- create index on business key --
create index ACT_IDX_CASE_EXEC_BUSKEY on ACT_RU_CASE_EXECUTION(BUSINESS_KEY_);

-- create foreign key constraints on ACT_RU_CASE_EXECUTION --
alter table ACT_RU_CASE_EXECUTION
    add constraint ACT_FK_CASE_EXE_CASE_INST
    foreign key (CASE_INST_ID_)
    references ACT_RU_CASE_EXECUTION(ID_);

alter table ACT_RU_CASE_EXECUTION
    add constraint ACT_FK_CASE_EXE_PARENT
    foreign key (PARENT_ID_)
    references ACT_RU_CASE_EXECUTION(ID_);

alter table ACT_RU_CASE_EXECUTION
    add constraint ACT_FK_CASE_EXE_CASE_DEF
    foreign key (CASE_DEF_ID_)
    references ACT_RE_CASE_DEF(ID_);

-- create foreign key constraints on ACT_RU_VARIABLE --
alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_CASE_EXE
    foreign key (CASE_EXECUTION_ID_)
    references ACT_RU_CASE_EXECUTION(ID_);

alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_CASE_INST
    foreign key (CASE_INST_ID_)
    references ACT_RU_CASE_EXECUTION(ID_);

-- create foreign key constraints on ACT_RU_TASK --
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_CASE_EXE
    foreign key (CASE_EXECUTION_ID_)
    references ACT_RU_CASE_EXECUTION(ID_);

alter table ACT_RU_TASK
  add constraint ACT_FK_TASK_CASE_DEF
  foreign key (CASE_DEF_ID_)
  references ACT_RE_CASE_DEF(ID_);

-- create foreign key constraints on ACT_RU_CASE_SENTRY_PART --
alter table ACT_RU_CASE_SENTRY_PART
    add constraint ACT_FK_CASE_SENTRY_CASE_INST
    foreign key (CASE_INST_ID_)
    references ACT_RU_CASE_EXECUTION(ID_);

alter table ACT_RU_CASE_SENTRY_PART
    add constraint ACT_FK_CASE_SENTRY_CASE_EXEC
    foreign key (CASE_EXEC_ID_)
    references ACT_RU_CASE_EXECUTION(ID_);

-- indexes for concurrency problems - https://app.camunda.com/jira/browse/CAM-1646 --
create index ACT_IDX_CASE_EXEC_CASE on ACT_RU_CASE_EXECUTION(CASE_DEF_ID_);
create index ACT_IDX_CASE_EXEC_PARENT on ACT_RU_CASE_EXECUTION(PARENT_ID_);
create index ACT_IDX_VARIABLE_CASE_EXEC on ACT_RU_VARIABLE(CASE_EXECUTION_ID_);
create index ACT_IDX_VARIABLE_CASE_INST on ACT_RU_VARIABLE(CASE_INST_ID_);
create index ACT_IDX_TASK_CASE_EXEC on ACT_RU_TASK(CASE_EXECUTION_ID_);
create index ACT_IDX_TASK_CASE_DEF_ID on ACT_RU_TASK(CASE_DEF_ID_);

-- add indexes for ACT_RU_CASE_SENTRY_PART --
create index ACT_IDX_CASE_SENTRY_CASE_INST on ACT_RU_CASE_SENTRY_PART(CASE_INST_ID_);
create index ACT_IDX_CASE_SENTRY_CASE_EXEC on ACT_RU_CASE_SENTRY_PART(CASE_EXEC_ID_);

create index ACT_IDX_CASE_DEF_TENANT_ID on ACT_RE_CASE_DEF(TENANT_ID_);
create index ACT_IDX_CASE_EXEC_TENANT_ID on ACT_RU_CASE_EXECUTION(TENANT_ID_);
