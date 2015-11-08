create table ACT_HI_PROCINST (
    ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64) not null,
    BUSINESS_KEY_ nvarchar(255),
    PROC_DEF_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64) not null,
    START_TIME_ datetime2 not null,
    END_TIME_ datetime2,
    DURATION_ numeric(19,0),
    START_USER_ID_ nvarchar(255),
    START_ACT_ID_ nvarchar(255),
    END_ACT_ID_ nvarchar(255),
    SUPER_PROCESS_INSTANCE_ID_ nvarchar(64),
    SUPER_CASE_INSTANCE_ID_ nvarchar(64),
    CASE_INST_ID_ nvarchar(64),
    DELETE_REASON_ nvarchar(4000),
    primary key (ID_),
    unique (PROC_INST_ID_)
);

create table ACT_HI_ACTINST (
    ID_ nvarchar(64) not null,
    PARENT_ACT_INST_ID_ nvarchar(64),
    PROC_DEF_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64) not null,
    EXECUTION_ID_ nvarchar(64) not null,
    ACT_ID_ nvarchar(255) not null,
    TASK_ID_ nvarchar(64),
    CALL_PROC_INST_ID_ nvarchar(64),
    CALL_CASE_INST_ID_ nvarchar(64),
    ACT_NAME_ nvarchar(255),
    ACT_TYPE_ nvarchar(255) not null,
    ASSIGNEE_ nvarchar(64),
    START_TIME_ datetime2 not null,
    END_TIME_ datetime2,
    DURATION_ numeric(19,0),
    ACT_INST_STATE_ tinyint,
    SEQUENCE_COUNTER_ numeric(19,0),
    primary key (ID_)
);

create table ACT_HI_TASKINST (
    ID_ nvarchar(64) not null,
    TASK_DEF_KEY_ nvarchar(255),
    PROC_DEF_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    CASE_DEF_KEY_ nvarchar(255),
    CASE_DEF_ID_ nvarchar(64),
    CASE_INST_ID_ nvarchar(64),
    CASE_EXECUTION_ID_ nvarchar(64),
    ACT_INST_ID_ nvarchar(64),
    NAME_ nvarchar(255),
    PARENT_TASK_ID_ nvarchar(64),
    DESCRIPTION_ nvarchar(4000),
    OWNER_ nvarchar(255),
    ASSIGNEE_ nvarchar(255),
    START_TIME_ datetime2 not null,
    END_TIME_ datetime2,
    DURATION_ numeric(19,0),
    DELETE_REASON_ nvarchar(4000),
    PRIORITY_ int,
    DUE_DATE_ datetime2,
    FOLLOW_UP_DATE_ datetime2,
    primary key (ID_)
);

create table ACT_HI_VARINST (
    ID_ nvarchar(64) not null,
    PROC_DEF_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    CASE_DEF_KEY_ nvarchar(255),
    CASE_DEF_ID_ nvarchar(64),
    CASE_INST_ID_ nvarchar(64),
    CASE_EXECUTION_ID_ nvarchar(64),
    ACT_INST_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(100),
    REV_ int,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

create table ACT_HI_DETAIL (
    ID_ nvarchar(64) not null,
    TYPE_ nvarchar(255) not null,
    PROC_DEF_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    CASE_DEF_KEY_ nvarchar(255),
    CASE_DEF_ID_ nvarchar(64),
    CASE_INST_ID_ nvarchar(64),
    CASE_EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    ACT_INST_ID_ nvarchar(64),
    VAR_INST_ID_ nvarchar(64),
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(255),
    REV_ int,
    TIME_ datetime2 not null,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    SEQUENCE_COUNTER_ numeric(19,0),
    primary key (ID_)
);

create table ACT_HI_COMMENT (
    ID_ nvarchar(64) not null,
    TYPE_ nvarchar(255),
    TIME_ datetime2 not null,
    USER_ID_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    ACTION_ nvarchar(255),
    MESSAGE_ nvarchar(4000),
    FULL_MSG_ image,
    primary key (ID_)
);

create table ACT_HI_ATTACHMENT (
    ID_ nvarchar(64) not null,
    REV_ integer,
    USER_ID_ nvarchar(255),
    NAME_ nvarchar(255),
    DESCRIPTION_ nvarchar(4000),
    TYPE_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    URL_ nvarchar(4000),
    CONTENT_ID_ nvarchar(64),
    primary key (ID_)
);

create table ACT_HI_OP_LOG (
    ID_ nvarchar(64) not null,
    DEPLOYMENT_ID_ nvarchar(64),
    PROC_DEF_ID_ nvarchar(64),
    PROC_DEF_KEY_ nvarchar(255),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    CASE_DEF_ID_ nvarchar(64),
    CASE_INST_ID_ nvarchar(64),
    CASE_EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    JOB_ID_ nvarchar(64),
    JOB_DEF_ID_ nvarchar(64),
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

create table ACT_HI_INCIDENT (
  ID_ nvarchar(64) not null,
  PROC_DEF_KEY_ nvarchar(255),
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

create table ACT_HI_JOB_LOG (
    ID_ nvarchar(64) not null,
    TIMESTAMP_ datetime2 not null,
    JOB_ID_ nvarchar(64) not null,
    JOB_DUEDATE_ datetime2,
    JOB_RETRIES_ integer,
    JOB_PRIORITY_ numeric(19,0) NOT NULL DEFAULT 0,
    JOB_EXCEPTION_MSG_ nvarchar(4000),
    JOB_EXCEPTION_STACK_ID_ nvarchar(64),
    JOB_STATE_ integer,
    JOB_DEF_ID_ nvarchar(64),
    JOB_DEF_TYPE_ nvarchar(255),
    JOB_DEF_CONFIGURATION_ nvarchar(255),
    ACT_ID_ nvarchar(255),
    EXECUTION_ID_ nvarchar(64),
    PROCESS_INSTANCE_ID_ nvarchar(64),
    PROCESS_DEF_ID_ nvarchar(64),
    PROCESS_DEF_KEY_ nvarchar(255),
    DEPLOYMENT_ID_ nvarchar(64),
    SEQUENCE_COUNTER_ numeric(19,0),
    primary key (ID_)
);

create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_ACT_INST_COMP on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_, END_TIME_, ID_);
create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_CASE_INST on ACT_HI_DETAIL(CASE_INST_ID_);
create index ACT_IDX_HI_DETAIL_CASE_EXEC on ACT_HI_DETAIL(CASE_EXECUTION_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_);
create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_CASEVAR_CASE_INST on ACT_HI_VARINST(CASE_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);
create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_HI_JOB_LOG_PROCINST on ACT_HI_JOB_LOG(PROCESS_INSTANCE_ID_);
create index ACT_IDX_HI_JOB_LOG_PROCDEF on ACT_HI_JOB_LOG(PROCESS_DEF_ID_);
