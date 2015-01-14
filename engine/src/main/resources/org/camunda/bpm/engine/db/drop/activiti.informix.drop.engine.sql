drop index if exists ACT_IDX_EXEC_BUSKEY;
drop index if exists ACT_IDX_TASK_CREATE;
drop index if exists ACT_IDX_TASK_ASSIGNEE;
drop index if exists ACT_IDX_IDENT_LNK_USER;
drop index if exists ACT_IDX_IDENT_LNK_GROUP;
drop index if exists ACT_IDX_VARIABLE_TASK_ID;
drop index if exists ACT_IDX_INC_CONFIGURATION;
drop index if exists ACT_IDX_JOB_PROCINST;

alter table ACT_GE_BYTEARRAY drop constraint ACT_FK_BYTEARR_DEPL;
alter table ACT_RU_EXECUTION drop constraint ACT_FK_EXE_PROCINST;
alter table ACT_RU_EXECUTION drop constraint ACT_FK_EXE_PARENT;
alter table ACT_RU_EXECUTION drop constraint ACT_FK_EXE_SUPER;
alter table ACT_RU_EXECUTION drop constraint ACT_FK_EXE_PROCDEF;
alter table ACT_RU_IDENTITYLINK drop constraint ACT_FK_TSKASS_TASK;
alter table ACT_RU_IDENTITYLINK drop constraint ACT_FK_ATHRZ_PROCEDEF;
alter table ACT_RU_TASK drop constraint ACT_FK_TASK_EXE;
alter table ACT_RU_TASK drop constraint ACT_FK_TASK_PROCINST;
alter table ACT_RU_TASK drop constraint ACT_FK_TASK_PROCDEF;
alter table ACT_RU_VARIABLE drop constraint ACT_FK_VAR_EXE;
alter table ACT_RU_VARIABLE drop constraint ACT_FK_VAR_PROCINST;
alter table ACT_RU_VARIABLE drop constraint ACT_FK_VAR_BYTEARRAY;
alter table ACT_RU_JOB drop constraint ACT_FK_JOB_EXCEPTION;
alter table ACT_RU_EVENT_SUBSCR drop constraint ACT_FK_EVENT_EXEC;
alter table ACT_RE_PROCDEF drop constraint ACT_UNIQ_PROCDEF;
alter table ACT_RU_INCIDENT drop constraint ACT_FK_INC_EXE; 
alter table ACT_RU_INCIDENT drop constraint ACT_FK_INC_PROCINST; 
alter table ACT_RU_INCIDENT drop constraint ACT_FK_INC_PROCDEF;
alter table ACT_RU_INCIDENT drop constraint ACT_FK_INC_CAUSE; 
alter table ACT_RU_INCIDENT drop constraint ACT_FK_INC_RCAUSE; 

drop index if exists ACT_UNIQ_AUTH_GROUP;
drop index if exists ACT_UNIQ_AUTH_USER;
alter table ACT_RU_VARIABLE drop constraint ACT_UNIQ_VARIABLE;

drop function ACT_FCT_USER_ID_OR_ID_;
drop function ACT_FCT_GROUP_ID_OR_ID_;
drop function ACT_FCT_RESOURCE_ID_OR_ID_;

drop index if exists ACT_IDX_EVENT_SUBSCR_CONFIG_;
drop index if exists ACT_IDX_ATHRZ_PROCEDEF;

-- indexes for deadlock problems - https://app.camunda.com/jira/browse/CAM-2567
drop index if exists ACT_IDX_INC_CAUSEINCID;
drop index if exists ACT_IDX_INC_EXID;
drop index if exists ACT_IDX_INC_PROCDEFID;
drop index if exists ACT_IDX_INC_PROCINSTID;
drop index if exists ACT_IDX_INC_ROOTCAUSEINCID;

drop table if exists ACT_GE_PROPERTY;
drop table if exists ACT_GE_BYTEARRAY;
drop table if exists ACT_RE_DEPLOYMENT;
drop table if exists ACT_RE_PROCDEF;
drop table if exists ACT_RU_IDENTITYLINK;
drop table if exists ACT_RU_VARIABLE;
drop table if exists ACT_RU_TASK;
drop table if exists ACT_RU_EXECUTION;
drop table if exists ACT_RU_JOB;
drop table if exists ACT_RU_JOBDEF;
drop table if exists ACT_RU_EVENT_SUBSCR;
drop table if exists ACT_RU_INCIDENT;
drop table if exists ACT_RU_AUTHORIZATION;
drop table if exists ACT_RU_FILTER;
