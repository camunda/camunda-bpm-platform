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
