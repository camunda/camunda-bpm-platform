-- metrics --

ALTER TABLE ACT_RU_METER_LOG
  ADD REPORTER_ nvarchar(255);

-- job prioritization --

ALTER TABLE ACT_RU_JOB
  ADD PRIORITY_ numeric(19,0) NOT NULL
  DEFAULT 0;

ALTER TABLE ACT_RU_JOBDEF
  ADD JOB_PRIORITY_ numeric(19,0);

ALTER TABLE ACT_HI_JOB_LOG
  ADD JOB_PRIORITY_ numeric(19,0) NOT NULL
  DEFAULT 0;

-- create decision definition table --
create table ACT_RE_DECISION_DEF (
    ID_ nvarchar(64) not null,
    REV_ int,
    CATEGORY_ nvarchar(255),
    NAME_ nvarchar(255),
    KEY_ nvarchar(255) not null,
    VERSION_ int not null,
    DEPLOYMENT_ID_ nvarchar(64),
    RESOURCE_NAME_ nvarchar(4000),
    DGRM_RESOURCE_NAME_ nvarchar(4000),
    primary key (ID_)
);

-- create unique constraint on ACT_RE_DECISION_DEF --
alter table ACT_RE_DECISION_DEF
    add constraint ACT_UNIQ_DECISION_DEF
    unique (KEY_,VERSION_);

-- case sentry part source --

ALTER TABLE ACT_RU_CASE_SENTRY_PART
  ADD SOURCE_ nvarchar(255);

-- create history decision instance table --
create table ACT_HI_DECINST (
    ID_ nvarchar(64) NOT NULL,
    DEC_DEF_ID_ nvarchar(64) NOT NULL,
    DEC_DEF_KEY_ nvarchar(255) NOT NULL,
    DEC_DEF_NAME_ nvarchar(255),
    PROC_DEF_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    CASE_DEF_KEY_ nvarchar(255),
    CASE_DEF_ID_ nvarchar(64),
    CASE_INST_ID_ nvarchar(64),
    ACT_INST_ID_ nvarchar(64),
    ACT_ID_ nvarchar(255),
    EVAL_TIME_ datetime2 not null,
    COLLECT_VALUE_ double precision,
    primary key (ID_)
);

-- create history decision input table --
create table ACT_HI_DEC_IN (
    ID_ nvarchar(64) NOT NULL,
    DEC_INST_ID_ nvarchar(64) NOT NULL,
    CLAUSE_ID_ nvarchar(64) NOT NULL,
    CLAUSE_NAME_ nvarchar(255),
    VAR_TYPE_ nvarchar(100),
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

-- create history decision output table --
create table ACT_HI_DEC_OUT (
    ID_ nvarchar(64) NOT NULL,
    DEC_INST_ID_ nvarchar(64) NOT NULL,
    CLAUSE_ID_ nvarchar(64) NOT NULL,
    CLAUSE_NAME_ nvarchar(255),
    RULE_ID_ nvarchar(64) NOT NULL,
    RULE_ORDER_ int,
    VAR_NAME_ nvarchar(255),
    VAR_TYPE_ nvarchar(100),
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

-- create indexes for historic decision tables
create index ACT_IDX_HI_DEC_INST_ID on ACT_HI_DECINST(DEC_DEF_ID_);
create index ACT_IDX_HI_DEC_INST_KEY on ACT_HI_DECINST(DEC_DEF_KEY_);
create index ACT_IDX_HI_DEC_INST_PI on ACT_HI_DECINST(PROC_INST_ID_);
create index ACT_IDX_HI_DEC_INST_CI on ACT_HI_DECINST(CASE_INST_ID_);
create index ACT_IDX_HI_DEC_INST_ACT on ACT_HI_DECINST(ACT_ID_);
create index ACT_IDX_HI_DEC_INST_ACT_INST on ACT_HI_DECINST(ACT_INST_ID_);
create index ACT_IDX_HI_DEC_INST_TIME on ACT_HI_DECINST(EVAL_TIME_);

create index ACT_IDX_HI_DEC_IN_INST on ACT_HI_DEC_IN(DEC_INST_ID_);
create index ACT_IDX_HI_DEC_IN_CLAUSE on ACT_HI_DEC_IN(DEC_INST_ID_, CLAUSE_ID_);

create index ACT_IDX_HI_DEC_OUT_INST on ACT_HI_DEC_OUT(DEC_INST_ID_);
create index ACT_IDX_HI_DEC_OUT_RULE on ACT_HI_DEC_OUT(RULE_ORDER_, CLAUSE_ID_);

-- add grant authorization for group camunda-admin:
INSERT INTO
  ACT_RU_AUTHORIZATION (ID_, TYPE_, GROUP_ID_, RESOURCE_TYPE_, RESOURCE_ID_, PERMS_, REV_)
VALUES
  ('camunda-admin-grant-decision-definition', 1, 'camunda-admin', 10, '*', 2147483647, 1);

-- external tasks --

create table ACT_RU_EXT_TASK (
  ID_ nvarchar(64) not null,
  REV_ integer not null,
  WORKER_ID_ nvarchar(255),
  TOPIC_NAME_ nvarchar(255),
  RETRIES_ int,
  ERROR_MSG_ nvarchar(4000),
  LOCK_EXP_TIME_ datetime2,
  EXECUTION_ID_ nvarchar(64),
  SUSPENSION_STATE_ tinyint,
  PROC_INST_ID_ nvarchar(64),
  PROC_DEF_ID_ nvarchar(64),
  PROC_DEF_KEY_ nvarchar(255),
  ACT_ID_ nvarchar(255),
  ACT_INST_ID_ nvarchar(64),
  primary key (ID_)
);

alter table ACT_RU_EXT_TASK
    add constraint ACT_FK_EXT_TASK_EXE
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXT_TASK_TOPIC on ACT_RU_EXT_TASK(TOPIC_NAME_);

-- deployment --

ALTER TABLE ACT_RE_DEPLOYMENT
  ADD SOURCE_ nvarchar(255);

ALTER TABLE ACT_HI_OP_LOG
  ADD DEPLOYMENT_ID_ nvarchar(64);

-- job suspension state

ALTER TABLE ACT_RU_JOB
  ADD DEFAULT 1
  FOR SUSPENSION_STATE_;

  -- relevant for jobs created in Camunda 7.0
UPDATE ACT_RU_JOB
  SET SUSPENSION_STATE_ = 1
  WHERE SUSPENSION_STATE_ IS NULL;

ALTER TABLE ACT_RU_JOB
  ALTER COLUMN SUSPENSION_STATE_ tinyint
  NOT NULL;
