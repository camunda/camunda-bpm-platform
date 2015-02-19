-- case management --

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD SUPER_EXEC_ varchar(64);

ALTER TABLE ACT_RU_CASE_EXECUTION
  ADD REQUIRED_ smallint check(REQUIRED_ in (1,0));

-- history --

ALTER TABLE ACT_HI_ACTINST
  ADD CALL_CASE_INST_ID_ varchar(64);

ALTER TABLE ACT_HI_PROCINST
  ADD SUPER_CASE_INSTANCE_ID_ varchar(64);

ALTER TABLE ACT_HI_CASEINST
  ADD SUPER_PROCESS_INSTANCE_ID_ varchar(64);

ALTER TABLE ACT_HI_CASEACTINST
  ADD REQUIRED_ smallint check(REQUIRED_ in (1,0));

create table ACT_HI_JOB_LOG (
    ID_ varchar(64) not null,
    TIMESTAMP_ timestamp not null,
    JOB_ID_ varchar(64) not null,
    JOB_DEF_ID_ varchar(64),
    ACT_ID_ varchar(64),
    TYPE_ varchar(255) not null,
    HANDLER_TYPE_ varchar(255),
    DUEDATE_ timestamp,
    RETRIES_ integer,
    EXCEPTION_MSG_ varchar(4000),
    EXCEPTION_STACK_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROCESS_DEF_ID_ varchar(64),
    PROCESS_DEF_KEY_ varchar(64),
    DEPLOYMENT_ID_ varchar(64),
    JOB_STATE_ integer,
    primary key (ID_)
);

-- remove unused columns

ALTER TABLE ACT_HI_ACTINST
  DROP COLUMN OWNER_;
