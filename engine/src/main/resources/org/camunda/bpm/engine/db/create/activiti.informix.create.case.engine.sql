-- create case definition table --

create table if not exists ACT_RE_CASE_DEF (
    ID_ varchar(64) not null,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) not null,
    VERSION_ integer not null,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ lvarchar(4000),
    DGRM_RESOURCE_NAME_ lvarchar(4000),
    primary key (ID_)
);

-- create case execution table --

create table if not exists ACT_RU_CASE_EXECUTION (
    ID_ varchar(64) not null,
    REV_ integer,
    CASE_INST_ID_ varchar(64),
    SUPER_CASE_EXEC_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    CASE_DEF_ID_ varchar(64),
    ACT_ID_ varchar(255),
    PREV_STATE_ integer,
    CURRENT_STATE_ integer,
    primary key (ID_)
);

-- create case sentry part table --

create table ACT_RU_CASE_SENTRY_PART (
    ID_ varchar(64) not null,
    REV_ integer,
    CASE_INST_ID_ varchar(64),
    CASE_EXEC_ID_ varchar(64),
    SENTRY_ID_ varchar(255),
    TYPE_ varchar(255),
    SOURCE_CASE_EXEC_ID_ varchar(64),
    STANDARD_EVENT_ varchar(255),
    SATISFIED_ boolean,
    primary key (ID_)
);

-- create unique constraint on ACT_RE_CASE_DEF --
alter table ACT_RE_CASE_DEF
    add constraint unique (KEY_,VERSION_)
	constraint ACT_UNIQ_CASE_DEF;

-- create index on business key --
create index if not exists ACT_IDX_CASE_EXEC_BUSKEY on ACT_RU_CASE_EXECUTION(BUSINESS_KEY_);

-- create foreign key constraints on ACT_RU_CASE_EXECUTION --
alter table ACT_RU_CASE_EXECUTION
    add constraint foreign key (CASE_INST_ID_)
    references ACT_RU_CASE_EXECUTION(ID_)
	constraint ACT_FK_CASE_EXE_CASE_INST;

alter table ACT_RU_CASE_EXECUTION
    add constraint foreign key (PARENT_ID_)
    references ACT_RU_CASE_EXECUTION(ID_)
	constraint ACT_FK_CASE_EXE_PARENT;

alter table ACT_RU_CASE_EXECUTION
    add constraint foreign key (CASE_DEF_ID_)
    references ACT_RE_CASE_DEF(ID_)
	constraint ACT_FK_CASE_EXE_CASE_DEF;

alter table ACT_RU_VARIABLE
    add constraint foreign key (CASE_EXECUTION_ID_)
    references ACT_RU_CASE_EXECUTION(ID_)
	constraint ACT_FK_VAR_CASE_EXE;

alter table ACT_RU_VARIABLE
    add constraint foreign key (CASE_INST_ID_)
    references ACT_RU_CASE_EXECUTION(ID_)
	constraint ACT_FK_VAR_CASE_INST;

-- create foreign key constraints on ACT_RU_TASK --
alter table ACT_RU_TASK
    add constraint foreign key (CASE_EXECUTION_ID_)
    references ACT_RU_CASE_EXECUTION(ID_)
	constraint ACT_FK_TASK_CASE_EXE;

alter table ACT_RU_TASK
  add constraint foreign key (CASE_DEF_ID_)
  references ACT_RE_CASE_DEF(ID_)
  constraint ACT_FK_TASK_CASE_DEF;

-- create foreign key constraints on ACT_RU_CASE_SENTRY_PART --
alter table ACT_RU_CASE_SENTRY_PART
    add constraint foreign key (CASE_INST_ID_)
    references ACT_RU_CASE_EXECUTION(ID_)
	constraint ACT_FK_CASE_SENTRY_CASE_INST;

alter table ACT_RU_CASE_SENTRY_PART
    add constraint foreign key (CASE_EXEC_ID_)
    references ACT_RU_CASE_EXECUTION(ID_)
    constraint ACT_FK_CASE_SENTRY_CASE_EXEC;
