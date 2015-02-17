-- case management --

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD SUPER_EXEC_ nvarchar(64);

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD REQUIRED_ tinyint;

-- history --

ALTER TABLE ACT_HI_ACTINST
  ADD CALL_CASE_INST_ID_ nvarchar(64);

ALTER TABLE ACT_HI_PROCINST
  ADD SUPER_CASE_INSTANCE_ID_ nvarchar(64);

ALTER TABLE ACT_HI_CASEINST
  ADD SUPER_PROCESS_INSTANCE_ID_ nvarchar(64);

ALTER TABLE ACT_HI_CASEACTINST
  ADD REQUIRED_ tinyint;

create table ACT_HI_JOB_LOG (
    ID_ varchar(64) not null,
    TIMESTAMP_ datetime2 not null,
    JOB_ID_ nvarchar(64) not null,
    JOB_DEF_ID_ nvarchar(64),
    ACT_ID_ nvarchar(64),
    TYPE_ nvarchar(255) not null,
    HANDLER_TYPE_ nvarchar(255),
    DUEDATE_ datetime2,
    RETRIES_ integer,
    EXCEPTION_MSG_ nvarchar(4000),
    EXCEPTION_STACK_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    PROCESS_INSTANCE_ID_ nvarchar(64),
    PROCESS_DEF_ID_ nvarchar(64),
    PROCESS_DEF_KEY_ nvarchar(64),
    DEPLOYMENT_ID_ nvarchar(64),
    JOB_STATE_ integer,
    primary key (ID_)
);
