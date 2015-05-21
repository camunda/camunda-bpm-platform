-- case management --

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD SUPER_EXEC_ varchar(64);

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD REQUIRED_ boolean;

-- history --

ALTER TABLE ACT_HI_ACTINST
  ADD CALL_CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_PROCINST
  ADD SUPER_CASE_INSTANCE_ID_ varchar(64);

ALTER TABLE ACT_HI_CASEINST
  ADD SUPER_PROCESS_INSTANCE_ID_ varchar(64);

ALTER TABLE ACT_HI_CASEACTINST
  ADD REQUIRED_ boolean;

create table ACT_HI_JOB_LOG (
    ID_ varchar(64) not null,
    TIMESTAMP_ timestamp not null,
    JOB_ID_ varchar(64) not null,
    JOB_DUEDATE_ timestamp,
    JOB_RETRIES_ integer,
    JOB_EXCEPTION_MSG_ varchar(4000),
    JOB_EXCEPTION_STACK_ID_ varchar(64),
    JOB_STATE_ integer,
    JOB_DEF_ID_ varchar(64),
    JOB_DEF_TYPE_ varchar(255),
    JOB_DEF_CONFIGURATION_ varchar(255),
    ACT_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROCESS_DEF_ID_ varchar(64),
    PROCESS_DEF_KEY_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    SEQUENCE_COUNTER_ bigint,
    primary key (ID_)
);

create index ACT_IDX_HI_JOB_LOG_PROCINST on ACT_HI_JOB_LOG(PROCESS_INSTANCE_ID_);
create index ACT_IDX_HI_JOB_LOG_PROCDEF on ACT_HI_JOB_LOG(PROCESS_DEF_ID_);

create index ACT_IDX_HI_ACT_INST_COMP on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_, END_TIME_, ID_);
create index ACT_IDX_HI_CAS_A_I_COMP on ACT_HI_CASEACTINST(CASE_ACT_ID_, END_TIME_, ID_);

-- history: add columns PROC_DEF_KEY_, PROC_DEF_ID_, CASE_DEF_KEY_, CASE_DEF_ID_ --

ALTER TABLE ACT_HI_PROCINST
  ADD PROC_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_ACTINST
  ADD PROC_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_TASKINST
  ADD PROC_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_TASKINST
  ADD CASE_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_VARINST
  ADD PROC_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_VARINST
  ADD PROC_DEF_ID_ varchar(64);

ALTER TABLE ACT_HI_VARINST
  ADD CASE_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_VARINST
  ADD CASE_DEF_ID_ varchar(64);

ALTER TABLE ACT_HI_DETAIL
  ADD PROC_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_DETAIL
  ADD PROC_DEF_ID_ varchar(64);

ALTER TABLE ACT_HI_DETAIL
  ADD CASE_DEF_KEY_ varchar(255);

ALTER TABLE ACT_HI_DETAIL
  ADD CASE_DEF_ID_ varchar(64);

ALTER TABLE ACT_HI_INCIDENT
  ADD PROC_DEF_KEY_ varchar(255);

-- sequence counter

ALTER TABLE ACT_RU_EXECUTION
  ADD SEQUENCE_COUNTER_ bigint;

ALTER TABLE ACT_HI_ACTINST
  ADD SEQUENCE_COUNTER_ bigint;

ALTER TABLE ACT_RU_VARIABLE
  ADD SEQUENCE_COUNTER_ bigint;

ALTER TABLE ACT_HI_DETAIL
  ADD SEQUENCE_COUNTER_ bigint;

ALTER TABLE ACT_RU_JOB
  ADD SEQUENCE_COUNTER_ bigint;

ALTER TABLE ACT_HI_OP_LOG
  ADD JOB_ID_ varchar(64);

ALTER TABLE ACT_HI_OP_LOG
  ADD JOB_DEF_ID_ varchar(64);

-- AUTHORIZATION --

-- add grant authorizations for group camunda-admin:
INSERT INTO
  ACT_RU_AUTHORIZATION (ID_, TYPE_, GROUP_ID_, RESOURCE_TYPE_, RESOURCE_ID_, PERMS_, REV_)
VALUES
  ('camunda-admin-grant-process-definition', 1, 'camunda-admin', 6, '*', 2147483647, 1),
  ('camunda-admin-grant-task', 1, 'camunda-admin', 7, '*', 2147483647, 1),
  ('camunda-admin-grant-process-instance', 1, 'camunda-admin', 8, '*', 2147483647, 1),
  ('camunda-admin-grant-deployment', 1, 'camunda-admin', 9, '*', 2147483647, 1);

-- add global grant authorizations for new authorization resources:
-- DEPLOYMENT
-- PROCESS_DEFINITION
-- PROCESS_INSTANCE
-- TASK
-- with ALL permissions

INSERT INTO
  ACT_RU_AUTHORIZATION (ID_, TYPE_, USER_ID_, RESOURCE_TYPE_, RESOURCE_ID_, PERMS_, REV_)
VALUES
  ('global-grant-process-definition', 0, '*', 6, '*', 2147483647, 1),
  ('global-grant-task', 0, '*', 7, '*', 2147483647, 1),
  ('global-grant-process-instance', 0, '*', 8, '*', 2147483647, 1),
  ('global-grant-deployment', 0, '*', 9, '*', 2147483647, 1);

-- variables --

ALTER TABLE ACT_RU_VARIABLE
  ADD IS_CONCURRENT_LOCAL_ boolean;

-- metrics --

create table ACT_RU_METER_LOG (
  ID_ varchar(64) not null,
  NAME_ varchar(64) not null,
  VALUE_ bigint,
  TIMESTAMP_ timestamp not null,
  primary key (ID_)
);

create index ACT_IDX_METER_LOG on ACT_RU_METER_LOG(NAME_,TIMESTAMP_);
