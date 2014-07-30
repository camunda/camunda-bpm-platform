-- add deployment.lock row to property table --
INSERT INTO ACT_GE_PROPERTY
  VALUES ('deployment.lock', '0', 1);

-- add revision column to incident table --
ALTER TABLE ACT_RU_INCIDENT
  ADD REV_ INT;

-- set initial incident revision to 1 --
UPDATE
  ACT_RU_INCIDENT
SET
  REV_ = 1;

-- set incident revision column to not null --
ALTER TABLE ACT_RU_INCIDENT
  ALTER COLUMN REV_ INT NOT NULL;

-- case management

ALTER TABLE ACT_RU_VARIABLE
  ADD CASE_EXECUTION_ID_ nvarchar(64);
  
ALTER TABLE ACT_RU_VARIABLE
  ADD CASE_INST_ID_ nvarchar(64);

ALTER TABLE ACT_RU_TASK
  ADD CASE_EXECUTION_ID_ nvarchar(64);

ALTER TABLE ACT_RU_TASK
  ADD CASE_INST_ID_ nvarchar(64);

ALTER TABLE ACT_RU_TASK
  ADD CASE_DEF_ID_ nvarchar(64);

ALTER TABLE ACT_RU_EXECUTION
  ADD SUPER_CASE_EXEC_ nvarchar(64);

ALTER TABLE ACT_RU_EXECUTION
  ADD CASE_INST_ID_ nvarchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD CASE_EXECUTION_ID_ nvarchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD CASE_INST_ID_ nvarchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD CASE_DEF_ID_ nvarchar(64);

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
    primary key (ID_)
);

-- create case execution table --
create table ACT_RU_CASE_EXECUTION (
    ID_ nvarchar(64) NOT NULL,
    REV_ int,
    CASE_INST_ID_ nvarchar(64),
    SUPER_CASE_EXEC_ nvarchar(64),
    BUSINESS_KEY_ nvarchar(255),
    PARENT_ID_ nvarchar(64),
    CASE_DEF_ID_ nvarchar(64),
    ACT_ID_ nvarchar(255),
    PREV_STATE_ int,
    CURRENT_STATE_ int,
    primary key (ID_)
);

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

-- indexes for concurrency problems - https://app.camunda.com/jira/browse/CAM-1646 --
create index ACT_IDX_CASE_EXEC_CASE on ACT_RU_CASE_EXECUTION(CASE_DEF_ID_);
create index ACT_IDX_CASE_EXEC_PARENT on ACT_RU_CASE_EXECUTION(PARENT_ID_);
create index ACT_IDX_VARIABLE_CASE_EXEC on ACT_RU_VARIABLE(CASE_EXECUTION_ID_);
create index ACT_IDX_VARIABLE_CASE_INST on ACT_RU_VARIABLE(CASE_INST_ID_);
create index ACT_IDX_TASK_CASE_EXEC on ACT_RU_TASK(CASE_EXECUTION_ID_);
create index ACT_IDX_TASK_CASE_INST on ACT_RU_TASK(CASE_INST_ID_);
create index ACT_IDX_TASK_CASE_DEF_ID on ACT_RU_TASK(CASE_DEF_ID_);

-- add data format configuration fields
ALTER TABLE ACT_RU_VARIABLE
  ADD DATA_FORMAT_ID_ nvarchar(255);
  
ALTER TABLE ACT_RU_VARIABLE
  ADD CONFIGURATION_ nvarchar(255);
  
ALTER TABLE ACT_HI_VARINST
  ADD DATA_FORMAT_ID_ nvarchar(255);
  
ALTER TABLE ACT_HI_VARINST
  ADD CONFIGURATION_ nvarchar(255);
