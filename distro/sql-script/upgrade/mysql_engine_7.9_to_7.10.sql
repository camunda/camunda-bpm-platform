-- https://app.camunda.com/jira/browse/CAM-9084
ALTER TABLE ACT_RE_PROCDEF
  ADD STARTABLE_ BOOLEAN NOT NULL DEFAULT TRUE;

-- https://app.camunda.com/jira/browse/CAM-9153
ALTER TABLE ACT_HI_VARINST
  ADD CREATE_TIME_ TIMESTAMP;

-- https://app.camunda.com/jira/browse/CAM-9215
ALTER TABLE ACT_HI_ATTACHMENT
  ADD CREATE_TIME_ TIMESTAMP;

-- https://app.camunda.com/jira/browse/CAM-9216
ALTER TABLE ACT_HI_DEC_IN
  ADD CREATE_TIME_ TIMESTAMP;

-- https://app.camunda.com/jira/browse/CAM-9217
ALTER TABLE ACT_HI_DEC_OUT
  ADD CREATE_TIME_ TIMESTAMP;

-- https://app.camunda.com/jira/browse/CAM-9199
ALTER TABLE ACT_HI_PROCINST
  ADD ROOT_PROCESS_INSTANCE_ID_ varchar(64);
create index ACT_IDX_HI_PRO_INST_ROOT_PI on ACT_HI_PROCINST(ROOT_PROCESS_INSTANCE_ID_);

-- https://app.camunda.com/jira/browse/CAM-9200
ALTER TABLE ACT_HI_PROCINST
  ADD REMOVAL_TIME_ datetime;
create index ACT_IDX_HI_PRO_INST_RM_TIME on ACT_HI_PROCINST(REMOVAL_TIME_);

-- https://app.camunda.com/jira/browse/CAM-9230
ALTER TABLE ACT_HI_BATCH
  ADD CREATE_USER_ID_ varchar(255);
ALTER TABLE ACT_RU_BATCH
  ADD CREATE_USER_ID_ varchar(255);

-- https://app.camunda.com/jira/browse/CAM-9270
ALTER TABLE ACT_HI_DECINST
  ADD ROOT_PROC_INST_ID_ varchar(64);
create index ACT_IDX_HI_DEC_INST_ROOT_PI on ACT_HI_DECINST(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9270
ALTER TABLE ACT_HI_DECINST
  ADD REMOVAL_TIME_ datetime;
create index ACT_IDX_HI_DEC_INST_RM_TIME on ACT_HI_DECINST(REMOVAL_TIME_);

-- https://app.camunda.com/jira/browse/CAM-9322
ALTER TABLE ACT_GE_BYTEARRAY
  ADD TYPE_ integer;

ALTER TABLE ACT_GE_BYTEARRAY
  ADD CREATE_TIME_ timestamp;

-- https://app.camunda.com/jira/browse/CAM-9370
ALTER TABLE ACT_RU_EXECUTION
  ADD ROOT_PROC_INST_ID_ varchar(64);

CREATE index ACT_IDX_EXEC_ROOT_PI ON ACT_RU_EXECUTION(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9371
ALTER TABLE ACT_HI_ACTINST
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_ACTINST_ROOT_PI on ACT_HI_ACTINST(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9372
ALTER TABLE ACT_HI_TASKINST
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_TASKINST_ROOT_PI on ACT_HI_TASKINST(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9373
ALTER TABLE ACT_HI_VARINST
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_VARINST_ROOT_PI on ACT_HI_VARINST(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9374
ALTER TABLE ACT_HI_DETAIL
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_DETAIL_ROOT_PI on ACT_HI_DETAIL(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9377
ALTER TABLE ACT_HI_INCIDENT
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_INCIDENT_ROOT_PI on ACT_HI_INCIDENT(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9390
ALTER TABLE ACT_HI_EXT_TASK_LOG
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_HI_EXT_TASK_LOG_ROOT_PI on ACT_HI_EXT_TASK_LOG(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9391
ALTER TABLE ACT_HI_IDENTITYLINK
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_IDENT_LNK_ROOT_PI on ACT_HI_IDENTITYLINK(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9392
ALTER TABLE ACT_HI_JOB_LOG
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_JOB_LOG_ROOT_PI on ACT_HI_JOB_LOG(ROOT_PROC_INST_ID_);

-- https://app.camunda.com/jira/browse/CAM-9393
ALTER TABLE ACT_HI_OP_LOG
  ADD ROOT_PROC_INST_ID_ varchar(64);

create index ACT_IDX_HI_OP_LOG_ROOT_PI on ACT_HI_OP_LOG(ROOT_PROC_INST_ID_);