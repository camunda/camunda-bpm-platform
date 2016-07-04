-- add deployment.lock row to property table --
INSERT INTO ACT_GE_PROPERTY
  VALUES ('deployment.lock', '0', 1);

-- add revision column to incident table --
ALTER TABLE ACT_RU_INCIDENT
  ADD REV_ INTEGER;

-- set initial incident revision to 1 --
UPDATE
  ACT_RU_INCIDENT
SET
  REV_ = 1;

-- set incident revision column to not null --
ALTER TABLE ACT_RU_INCIDENT
  MODIFY REV_ INTEGER NOT NULL;

-- case management

ALTER TABLE ACT_RU_VARIABLE
  ADD CASE_EXECUTION_ID_ varchar(64);

ALTER TABLE ACT_RU_VARIABLE
  ADD CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_RU_TASK
  ADD CASE_EXECUTION_ID_ varchar(64);

ALTER TABLE ACT_RU_TASK
  ADD CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_RU_TASK
  ADD CASE_DEF_ID_ varchar(64);

ALTER TABLE ACT_RU_EXECUTION
  ADD SUPER_CASE_EXEC_ varchar(64);

ALTER TABLE ACT_RU_EXECUTION
  ADD CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD CASE_EXECUTION_ID_ varchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD CASE_DEF_ID_ varchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD PROC_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_PROCINST
  ADD CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_TASKINST
  ADD CASE_EXECUTION_ID_ varchar(64);

ALTER TABLE ACT_HI_TASKINST
  ADD CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_TASKINST
  ADD CASE_DEF_ID_ varchar(64);

-- create case definition table --
create table ACT_RE_CASE_DEF (
    ID_ varchar(64) not null,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) not null,
    VERSION_ integer not null,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

-- create case execution table --
create table ACT_RU_CASE_EXECUTION (
    ID_ varchar(64) NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

-- create case sentry part table --

create table ACT_RU_CASE_SENTRY_PART (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CASE_INST_ID_ varchar(64),
    CASE_EXEC_ID_ varchar(64),
    SENTRY_ID_ varchar(255),
    TYPE_ varchar(255),
    SOURCE_CASE_EXEC_ID_ varchar(64),
    STANDARD_EVENT_ varchar(255),
    SATISFIED_ boolean,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

-- create unique constraint on ACT_RE_CASE_DEF --
alter table ACT_RE_CASE_DEF
    add constraint ACT_UNIQ_CASE_DEF
    unique (KEY_,VERSION_);

-- create index on business key --
create index ACT_IDX_CASE_EXEC_BUSKEY on ACT_RU_CASE_EXECUTION(BUSINESS_KEY_);

-- create foreign key constraints on ACT_RU_CASE_EXECUTION --
alter table ACT_RU_CASE_EXECUTION
    add constraint ACT_FK_CASE_EXE_CASE_INST
    foreign key (CASE_INST_ID_)
    references ACT_RU_CASE_EXECUTION(ID_) on delete cascade on update cascade;

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

-- create filter table
create table ACT_RU_FILTER (
  ID_ varchar(64) not null,
  REV_ integer not null,
  RESOURCE_TYPE_ varchar(255) not null,
  NAME_ varchar(255) not null,
  OWNER_ varchar(255),
  QUERY_ LONGTEXT not null,
  PROPERTIES_ LONGTEXT,
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

-- add index to improve job executor performance
create index ACT_IDX_JOB_PROCINST on ACT_RU_JOB(PROCESS_INSTANCE_ID_);

-- create historic case instance/activity table and indexes --
create table ACT_HI_CASEINST (
    ID_ varchar(64) not null,
    CASE_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    CASE_DEF_ID_ varchar(64) not null,
    CREATE_TIME_ datetime not null,
    CLOSE_TIME_ datetime,
    DURATION_ bigint,
    STATE_ integer,
    CREATE_USER_ID_ varchar(255),
    SUPER_CASE_INSTANCE_ID_ varchar(64),
    primary key (ID_),
    unique (CASE_INST_ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_HI_CASEACTINST (
    ID_ varchar(64) not null,
    PARENT_ACT_INST_ID_ varchar(64),
    CASE_DEF_ID_ varchar(64) not null,
    CASE_INST_ID_ varchar(64) not null,
    CASE_ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    CALL_CASE_INST_ID_ varchar(64),
    CASE_ACT_NAME_ varchar(255),
    CASE_ACT_TYPE_ varchar(255),
    CREATE_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ bigint,
    STATE_ integer,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_HI_CAS_I_CLOSE on ACT_HI_CASEINST(CLOSE_TIME_);
create index ACT_IDX_HI_CAS_I_BUSKEY on ACT_HI_CASEINST(BUSINESS_KEY_);
create index ACT_IDX_HI_CAS_A_I_CREATE on ACT_HI_CASEACTINST(CREATE_TIME_);
create index ACT_IDX_HI_CAS_A_I_END on ACT_HI_CASEACTINST(END_TIME_);
create index ACT_IDX_HI_CAS_A_I_COMP on ACT_HI_CASEACTINST(CASE_ACT_ID_, END_TIME_, ID_);
create index ACT_IDX_HI_CAS_A_I_CASEINST on ACT_HI_CASEACTINST(CASE_INST_ID_, CASE_ACT_ID_);

create index ACT_IDX_TASK_ASSIGNEE on ACT_RU_TASK(ASSIGNEE_);

-- add case instance/execution to historic variable instance and detail --
alter table ACT_HI_VARINST
  add CASE_INST_ID_ varchar(64);

alter table ACT_HI_VARINST
  add CASE_EXECUTION_ID_ varchar(64);

alter table ACT_HI_DETAIL
  add CASE_INST_ID_ varchar(64);

alter table ACT_HI_DETAIL
  add CASE_EXECUTION_ID_ varchar(64);

create index ACT_IDX_HI_DETAIL_CASE_INST on ACT_HI_DETAIL(CASE_INST_ID_);
create index ACT_IDX_HI_DETAIL_CASE_EXEC on ACT_HI_DETAIL(CASE_EXECUTION_ID_);
create index ACT_IDX_HI_CASEVAR_CASE_INST on ACT_HI_VARINST(CASE_INST_ID_);
