-- add new column to historic activity instance table --
alter table ACT_HI_ACTINST
    add ACT_INST_STATE_ INTEGER;

-- add follow-up date to tasks --
alter table ACT_RU_TASK
    add FOLLOW_UP_DATE_ TIMESTAMP(6);
alter table ACT_HI_TASKINST
    add FOLLOW_UP_DATE_ TIMESTAMP(6);

-- add JOBDEF table --
create table ACT_RU_JOBDEF (
    ID_ NVARCHAR2(64) NOT NULL,
    REV_ INTEGER,
    PROC_DEF_ID_ NVARCHAR2(64) NOT NULL,
    PROC_DEF_KEY_ NVARCHAR2(255) NOT NULL,
    ACT_ID_ NVARCHAR2(255) NOT NULL,
    JOB_TYPE_ NVARCHAR2(255) NOT NULL,
    JOB_CONFIGURATION_ NVARCHAR2(255),
    SUSPENSION_STATE_ INTEGER,
    primary key (ID_)
);

-- add new columns to job table --
alter table ACT_RU_JOB
    add PROCESS_DEF_ID_ NVARCHAR2(64);

alter table ACT_RU_JOB
    add PROCESS_DEF_KEY_ NVARCHAR2(64);

alter table ACT_RU_JOB
    add SUSPENSION_STATE_ INTEGER;

alter table ACT_RU_JOB
    add JOB_DEF_ID_ NVARCHAR2(64);

-- update job table with values from execution table --

UPDATE
    ACT_RU_JOB J
SET
    PROCESS_DEF_ID_  = (
        SELECT
            PI.PROC_DEF_ID_
        FROM
            ACT_RU_EXECUTION PI
        WHERE
            PI.ID_ = J.PROCESS_INSTANCE_ID_
    );

UPDATE
    ACT_RU_JOB J
SET
    SUSPENSION_STATE_  = (
        SELECT
            PI.SUSPENSION_STATE_
        FROM
            ACT_RU_EXECUTION PI
        WHERE
            PI.ID_ = J.PROCESS_INSTANCE_ID_
    );

UPDATE
    ACT_RU_JOB J
SET
    PROCESS_DEF_KEY_  = (
        SELECT
            PD.KEY_
        FROM
            ACT_RE_PROCDEF PD
        WHERE
            PD.ID_ = J.PROCESS_DEF_ID_
    );

-- create HIST OP LOG table

create table ACT_HI_OP_LOG (
    ID_ NVARCHAR2(64) not null,
    PROC_DEF_ID_ NVARCHAR2(64),
    PROC_INST_ID_ NVARCHAR2(64),
    EXECUTION_ID_ NVARCHAR2(64),
    TASK_ID_ NVARCHAR2(64),
    USER_ID_ NVARCHAR2(255),
    TIMESTAMP_ TIMESTAMP(6) not null,
    OPERATION_TYPE_ NVARCHAR2(255),
    OPERATION_ID_ NVARCHAR2(64),
    ENTITY_TYPE_ NVARCHAR2(30),
    PROPERTY_ NVARCHAR2(64),
    ORG_VALUE_ NVARCHAR2(2000),
    NEW_VALUE_ NVARCHAR2(2000),
    primary key (ID_)
);

-- add new column to ACT_HI_VARINST --

alter table ACT_HI_VARINST
    add ACT_INST_ID_ NVARCHAR2(64);

alter table ACT_HI_DETAIL
    add VAR_INST_ID_ NVARCHAR2(64);

alter table ACT_HI_TASKINST
    add ACT_INST_ID_ NVARCHAR2(64);

-- set cached entity state to 63 on all executions --

UPDATE
    ACT_RU_EXECUTION
SET
    CACHED_ENT_STATE_ = 63;

-- align data types

alter table ACT_RE_PROCDEF
    modify (DGRM_RESOURCE_NAME_ NVARCHAR2(2000));

alter table ACT_RU_AUTHORIZATION
    modify (ID_ NVARCHAR2(64));

-- delete index on column GROUP_ID_ to modify the type --
drop index ACT_UNIQ_AUTH_GROUP;

-- delete index on column USER_ID_ to modify the type --
drop index ACT_UNIQ_AUTH_USER;

alter table ACT_RU_AUTHORIZATION
    modify (GROUP_ID_ NVARCHAR2(255));

alter table ACT_RU_AUTHORIZATION
    modify (USER_ID_ NVARCHAR2(255));

alter table ACT_RU_AUTHORIZATION
    modify (RESOURCE_ID_ NVARCHAR2(64));

-- add index on column GROUP_ID_ --
create unique index ACT_UNIQ_AUTH_GROUP on ACT_RU_AUTHORIZATION
   (case when GROUP_ID_ is null then null else TYPE_ end,
    case when GROUP_ID_ is null then null else RESOURCE_TYPE_ end,
    case when GROUP_ID_ is null then null else RESOURCE_ID_ end,
    case when GROUP_ID_ is null then null else GROUP_ID_ end);

-- add index on column USER_ID_ --
create unique index ACT_UNIQ_AUTH_USER on ACT_RU_AUTHORIZATION
   (case when USER_ID_ is null then null else TYPE_ end,
    case when USER_ID_ is null then null else RESOURCE_TYPE_ end,
    case when USER_ID_ is null then null else RESOURCE_ID_ end,
    case when USER_ID_ is null then null else USER_ID_ end);

-- add new table ACT_HI_INCIDENT --

create table ACT_HI_INCIDENT (
  ID_ NVARCHAR2(64) not null,
  PROC_DEF_ID_ NVARCHAR2(64),
  PROC_INST_ID_ NVARCHAR2(64),
  EXECUTION_ID_ NVARCHAR2(64),
  CREATE_TIME_ TIMESTAMP(6) not null,
  END_TIME_ TIMESTAMP(6),
  INCIDENT_MSG_ NVARCHAR2(2000),
  INCIDENT_TYPE_ NVARCHAR2(255) not null,
  ACTIVITY_ID_ NVARCHAR2(255),
  CAUSE_INCIDENT_ID_ NVARCHAR2(64),
  ROOT_CAUSE_INCIDENT_ID_ NVARCHAR2(64),
  CONFIGURATION_ NVARCHAR2(255),
  INCIDENT_STATE_ INTEGER,
  primary key (ID_)
);

-- update ACT_RU_VARIABLE table --

-- add new column --

ALTER TABLE ACT_RU_VARIABLE
    add VAR_SCOPE_ NVARCHAR2(64);

-- migrate execution variables --

UPDATE
  ACT_RU_VARIABLE V

SET
  VAR_SCOPE_ = V.EXECUTION_ID_

WHERE
  V.EXECUTION_ID_ is not null AND
  V.TASK_ID_ is null;

-- migrate task variables --

UPDATE
  ACT_RU_VARIABLE V

SET
  VAR_SCOPE_ = V.TASK_ID_

WHERE
  V.TASK_ID_ is not null;

-- set VAR_SCOPE_ not null--

ALTER TABLE ACT_RU_VARIABLE
    modify (VAR_SCOPE_ not null);

-- add unique constraint --

alter table ACT_RU_VARIABLE
    add constraint ACT_UNIQ_VARIABLE
    unique (VAR_SCOPE_, NAME_);
