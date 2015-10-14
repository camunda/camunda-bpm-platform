drop index ACT_IDX_EXEC_BUSKEY;
drop index ACT_IDX_TASK_CREATE;
drop index ACT_IDX_TASK_ASSIGNEE;
drop index ACT_IDX_IDENT_LNK_USER;
drop index ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_IDX_VARIABLE_TASK_ID;
drop index ACT_IDX_INC_CONFIGURATION;
drop index ACT_IDX_JOB_PROCINST;

alter table ACT_GE_BYTEARRAY 
    drop constraint ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop constraint ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION 
    drop constraint ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop constraint ACT_FK_EXE_SUPER;
    
alter table ACT_RU_EXECUTION
    drop constraint ACT_FK_EXE_PROCDEF;

alter table ACT_RU_IDENTITYLINK
    drop constraint ACT_FK_TSKASS_TASK;

alter table ACT_RU_IDENTITYLINK
    drop constraint ACT_FK_ATHRZ_PROCEDEF;

alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_PROCDEF;
	
alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_PROCINST;
    
alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop constraint ACT_FK_JOB_EXCEPTION;
    
alter table ACT_RU_EVENT_SUBSCR
    drop constraint ACT_FK_EVENT_EXEC;

alter table ACT_RE_PROCDEF
    drop constraint ACT_UNIQ_PROCDEF;
    
alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_EXE; 
  
alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_PROCINST; 

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_PROCDEF;
    
alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_CAUSE; 

alter table ACT_RU_INCIDENT
    drop constraint ACT_FK_INC_RCAUSE; 

alter table ACT_RU_AUTHORIZATION
    drop constraint ACT_UNIQ_AUTH_GROUP; 
    
alter table ACT_RU_AUTHORIZATION
    drop constraint ACT_UNIQ_AUTH_USER;

alter table ACT_RU_VARIABLE
    drop constraint ACT_UNIQ_VARIABLE;
    
alter table ACT_RU_EXT_TASK
    drop constraint ACT_FK_EXT_TASK_EXE;

drop index ACT_IDX_EVENT_SUBSCR_CONFIG_;
drop index ACT_IDX_ATHRZ_PROCEDEF;

-- indexes for deadlock problems - https://app.camunda.com/jira/browse/CAM-2567
drop index ACT_IDX_INC_CAUSEINCID;
drop index ACT_IDX_INC_EXID;
drop index ACT_IDX_INC_PROCDEFID;
drop index ACT_IDX_INC_PROCINSTID;
drop index ACT_IDX_INC_ROOTCAUSEINCID;

drop index ACT_IDX_METER_LOG;
drop index ACT_IDX_AUTH_RESOURCE_ID;
drop index ACT_IDX_EXT_TASK_TOPIC;

drop index ACT_IDX_BYTEARRAY_NAME;
drop index ACT_IDX_DEPLOYMENT_NAME;
drop index ACT_IDX_JOBDEF_PROC_DEF_ID;
drop index ACT_IDX_JOB_HANDLER_TYPE;
drop index ACT_IDX_EVENT_SUBSCR_EVT_NAME;
drop index ACT_IDX_PROCDEF_DEPLOYMENT_ID;

drop table ACT_GE_PROPERTY if exists;
drop table ACT_GE_BYTEARRAY if exists;
drop table ACT_RE_DEPLOYMENT if exists;
drop table ACT_RU_EXECUTION if exists;
drop table ACT_RU_JOB if exists;
drop table ACT_RU_JOBDEF if exists;
drop table ACT_RE_PROCDEF if exists;
drop table ACT_RU_TASK if exists;
drop table ACT_RU_IDENTITYLINK if exists;
drop table ACT_RU_VARIABLE if exists;
drop table ACT_RU_EVENT_SUBSCR if exists;
drop table ACT_RU_INCIDENT if exists;
drop table ACT_RU_AUTHORIZATION if exists;
drop table ACT_RU_FILTER if exists;
drop table ACT_RU_METER_LOG if exists;
drop table ACT_RU_EXT_TASK if exists;

