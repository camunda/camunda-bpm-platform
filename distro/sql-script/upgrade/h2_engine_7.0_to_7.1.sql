-- add new column to historic activity instance table --
alter table ACT_HI_ACTINST
    add ACT_INST_STATE_ integer;

-- add follow-up date to tasks --
alter table ACT_RU_TASK
    add FOLLOW_UP_DATE_ timestamp;
alter table ACT_HI_TASKINST
    add FOLLOW_UP_DATE_ timestamp;

-- add JOBDEF table --
create table ACT_RU_JOBDEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    PROC_DEF_ID_ varchar(64) NOT NULL,
    PROC_DEF_KEY_ varchar(255) NOT NULL,
    ACT_ID_ varchar(255) NOT NULL,
    JOB_TYPE_ varchar(255) NOT NULL,
    JOB_CONFIGURATION_ varchar(255),
    SUSPENSION_STATE_ integer,
    primary key (ID_)
);

-- add new columns to job table --
alter table ACT_RU_JOB
    add PROCESS_DEF_ID_ varchar(64);

alter table ACT_RU_JOB
    add PROCESS_DEF_KEY_ varchar(64);

alter table ACT_RU_JOB
    add SUSPENSION_STATE_ integer;

alter table ACT_RU_JOB
    add JOB_DEF_ID_ varchar(64);

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
    ),
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

-- add Hist OP Log table --

create table ACT_HI_OP_LOG (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    USER_ID_ varchar(255),
    TIMESTAMP_ timestamp not null,
    OPERATION_TYPE_ varchar(64),
    OPERATION_ID_ varchar(64),
    ENTITY_TYPE_ varchar(30),
    PROPERTY_ varchar(64),
    ORG_VALUE_ varchar(4000),
    NEW_VALUE_ varchar(4000),
    primary key (ID_)
);

-- add new column to ACT_HI_VARINST --

alter table ACT_HI_VARINST
    add ACT_INST_ID_ varchar(64);

alter table ACT_HI_DETAIL
    add VAR_INST_ID_ varchar(64);

alter table ACT_HI_TASKINST
    add ACT_INST_ID_ varchar(64);

-- set cached entity state to 63 on all executions --

UPDATE
    ACT_RU_EXECUTION
SET
    CACHED_ENT_STATE_ = 63;

-- add new table ACT_HI_INCIDENT --

create table ACT_HI_INCIDENT (
  ID_ varchar(64) not null,
  PROC_DEF_ID_ varchar(64),
  PROC_INST_ID_ varchar(64),
  EXECUTION_ID_ varchar(64),
  CREATE_TIME_ timestamp not null,
  END_TIME_ timestamp,
  INCIDENT_MSG_ varchar(4000),
  INCIDENT_TYPE_ varchar(255) not null,
  ACTIVITY_ID_ varchar(255),
  CAUSE_INCIDENT_ID_ varchar(64),
  ROOT_CAUSE_INCIDENT_ID_ varchar(64),
  CONFIGURATION_ varchar(255),
  INCIDENT_STATE_ integer,
  primary key (ID_)
);

-- update ACT_RU_VARIABLE table --

-- add new column --

ALTER TABLE ACT_RU_VARIABLE
    add VAR_SCOPE_ varchar(64);

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

-- set VAR_SCOPE_ not null --

ALTER TABLE ACT_RU_VARIABLE
    ALTER COLUMN VAR_SCOPE_ SET NOT NULL;

-- add unique constraint --
ALTER TABLE ACT_RU_VARIABLE
    ADD CONSTRAINT ACT_UNIQ_VARIABLE
    UNIQUE (VAR_SCOPE_, NAME_);
