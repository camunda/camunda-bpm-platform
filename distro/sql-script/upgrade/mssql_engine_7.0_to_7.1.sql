-- add new column to historic activity instance table --
alter table ACT_HI_ACTINST
    add ACT_INST_STATE_ tinyint;

-- add follow-up date to tasks --
alter table ACT_RU_TASK
    add FOLLOW_UP_DATE_ datetime2;
alter table ACT_HI_TASKINST
    add FOLLOW_UP_DATE_ datetime2;

-- add JOBDEF table --
create table ACT_RU_JOBDEF (
    ID_ nvarchar(64) NOT NULL,
    REV_ integer,
    PROC_DEF_ID_ nvarchar(64) NOT NULL,
    PROC_DEF_KEY_ nvarchar(255) NOT NULL,
    ACT_ID_ nvarchar(255) NOT NULL,
    JOB_TYPE_ nvarchar(255) NOT NULL,
    JOB_CONFIGURATION_ nvarchar(255),
    SUSPENSION_STATE_ tinyint,
    primary key (ID_)
);

-- add new columns to job table --
alter table ACT_RU_JOB
    add PROCESS_DEF_ID_ nvarchar(64);

alter table ACT_RU_JOB
    add PROCESS_DEF_KEY_ nvarchar(64);

alter table ACT_RU_JOB
    add SUSPENSION_STATE_ tinyint;

alter table ACT_RU_JOB
    add JOB_DEF_ID_ nvarchar(64);

-- update job table with values from execution table --

UPDATE
    ACT_RU_JOB
SET
    PROCESS_DEF_ID_  = (
        SELECT
            PI.PROC_DEF_ID_
        FROM
            ACT_RU_EXECUTION PI
        WHERE
            PI.ID_ = PROCESS_INSTANCE_ID_
    ),
    SUSPENSION_STATE_  = (
        SELECT
            PI.SUSPENSION_STATE_
        FROM
            ACT_RU_EXECUTION PI
        WHERE
            PI.ID_ = PROCESS_INSTANCE_ID_
    );

UPDATE
    ACT_RU_JOB
SET
    PROCESS_DEF_KEY_  = (
        SELECT
            PD.KEY_
        FROM
            ACT_RE_PROCDEF PD
        WHERE
            PD.ID_ = PROCESS_DEF_ID_
    );

-- add Hist OP Log table --

create table ACT_HI_OP_LOG (
    ID_ nvarchar(64) not null,
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    USER_ID_ nvarchar(255),
    TIMESTAMP_ datetime2 not null,
    OPERATION_TYPE_ nvarchar(64),
    OPERATION_ID_ nvarchar(64),
    ENTITY_TYPE_ nvarchar(30),
    PROPERTY_ nvarchar(64),
    ORG_VALUE_ nvarchar(4000),
    NEW_VALUE_ nvarchar(4000),
    primary key (ID_)
);

-- add new column to ACT_HI_VARINST --

alter table ACT_HI_VARINST
    add ACT_INST_ID_ nvarchar(64);

alter table ACT_HI_DETAIL
    add VAR_INST_ID_ nvarchar(64);

alter table ACT_HI_TASKINST
    add ACT_INST_ID_ nvarchar(64);

-- set cached entity state to 63 on all executions --

UPDATE
    ACT_RU_EXECUTION
SET
    CACHED_ENT_STATE_ = 63;

-- add new table ACT_HI_INCIDENT --

create table ACT_HI_INCIDENT (
  ID_ nvarchar(64) not null,
  PROC_DEF_ID_ nvarchar(64),
  PROC_INST_ID_ nvarchar(64),
  EXECUTION_ID_ nvarchar(64),
  CREATE_TIME_ datetime2 not null,
  END_TIME_ datetime2,
  INCIDENT_MSG_ nvarchar(4000),
  INCIDENT_TYPE_ nvarchar(255) not null,
  ACTIVITY_ID_ nvarchar(255),
  CAUSE_INCIDENT_ID_ nvarchar(64),
  ROOT_CAUSE_INCIDENT_ID_ nvarchar(64),
  CONFIGURATION_ nvarchar(255),
  INCIDENT_STATE_ integer,
  primary key (ID_)
);

-- update ACT_RU_VARIABLE table --

-- add new column --

ALTER TABLE ACT_RU_VARIABLE
    add VAR_SCOPE_ nvarchar(64);

-- migrate execution variables --

UPDATE
  ACT_RU_VARIABLE

SET
  VAR_SCOPE_ = EXECUTION_ID_

WHERE
  EXECUTION_ID_ is not null AND
  TASK_ID_ is null;

-- migrate task variables --

UPDATE
  ACT_RU_VARIABLE

SET
  VAR_SCOPE_ = TASK_ID_

WHERE
  TASK_ID_ is not null;

-- set VAR_SCOPE_ not null--

ALTER TABLE ACT_RU_VARIABLE
    ALTER COLUMN VAR_SCOPE_ nvarchar(64) NOT NULL;

-- add unique constraint --

CREATE UNIQUE INDEX ACT_UNIQ_VARIABLE ON ACT_RU_VARIABLE(VAR_SCOPE_, NAME_);

-- indexes for concurrency problems - https://app.camunda.com/jira/browse/CAM-1646 --
create index ACT_IDX_EXECUTION_PROC on ACT_RU_EXECUTION(PROC_DEF_ID_);
create index ACT_IDX_EXECUTION_PARENT on ACT_RU_EXECUTION(PARENT_ID_);
create index ACT_IDX_EXECUTION_SUPER on ACT_RU_EXECUTION(SUPER_EXEC_);
create index ACT_IDX_EVENT_SUBSCR_EXEC on ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
create index ACT_IDX_BA_DEPLOYMENT on ACT_GE_BYTEARRAY(DEPLOYMENT_ID_);
create index ACT_IDX_IDENT_LNK_TASK on ACT_RU_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_INCIDENT_EXEC on ACT_RU_INCIDENT(EXECUTION_ID_);
create index ACT_IDX_INCIDENT_PROCINST on ACT_RU_INCIDENT(PROC_INST_ID_);
create index ACT_IDX_INCIDENT_PROC_DEF_ID on ACT_RU_INCIDENT(PROC_DEF_ID_);
create index ACT_IDX_INCIDENT_CAUSE on ACT_RU_INCIDENT(CAUSE_INCIDENT_ID_);
create index ACT_IDX_INCIDENT_ROOT_CAUSE on ACT_RU_INCIDENT(ROOT_CAUSE_INCIDENT_ID_);
create index ACT_IDX_JOB_EXCEPTION_STACK on ACT_RU_JOB(EXCEPTION_STACK_ID_);
create index ACT_IDX_VARIABLE_BA on ACT_RU_VARIABLE(BYTEARRAY_ID_);
create index ACT_IDX_VARIABLE_EXEC on ACT_RU_VARIABLE(EXECUTION_ID_);
create index ACT_IDX_VARIABLE_PROCINST on ACT_RU_VARIABLE(PROC_INST_ID_);
create index ACT_IDX_TASK_EXEC on ACT_RU_TASK(EXECUTION_ID_);
create index ACT_IDX_TASK_PROCINST on ACT_RU_TASK(PROC_INST_ID_);
create index ACT_IDX_TASK_PROC_DEF_ID on ACT_RU_TASK(PROC_DEF_ID_);