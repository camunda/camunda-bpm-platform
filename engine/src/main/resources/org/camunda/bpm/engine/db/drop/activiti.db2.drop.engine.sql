drop index ACT_IDX_EXEC_BUSKEY;
drop index ACT_IDX_TASK_CREATE;
drop index ACT_IDX_TASK_ASSIGNEE;
drop index ACT_IDX_IDENT_LNK_USER;
drop index ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_IDX_VARIABLE_TASK_ID;
drop index ACT_IDX_INC_CONFIGURATION;
drop index ACT_IDX_JOB_PROCINST;
drop index ACT_UNIQ_AUTH_USER;
drop index ACT_UNIQ_AUTH_GROUP;
drop index ACT_UNIQ_VARIABLE;

alter table ACT_GE_BYTEARRAY
    drop foreign key ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop foreign key ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION
    drop foreign key ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION
    drop foreign key ACT_FK_EXE_SUPER;

alter table ACT_RU_EXECUTION 
    drop foreign key ACT_FK_EXE_PROCDEF;

alter table ACT_RU_IDENTITYLINK
    drop foreign key ACT_FK_TSKASS_TASK;

alter table ACT_RU_IDENTITYLINK
    drop foreign key ACT_FK_ATHRZ_PROCEDEF;

alter table ACT_RU_TASK
	drop foreign key ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop foreign key ACT_FK_TASK_PROCINST;

alter table ACT_RU_TASK
	drop foreign key ACT_FK_TASK_PROCDEF;

alter table ACT_RU_VARIABLE
    drop foreign key ACT_FK_VAR_EXE;

alter table ACT_RU_VARIABLE
	drop foreign key ACT_FK_VAR_PROCINST;

alter table ACT_RU_VARIABLE
    drop foreign key ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop foreign key ACT_FK_JOB_EXCEPTION;

alter table ACT_RU_EVENT_SUBSCR
    drop foreign key ACT_FK_EVENT_EXEC;

alter table ACT_RU_INCIDENT
    drop foreign key ACT_FK_INC_EXE;

alter table ACT_RU_INCIDENT
    drop foreign key ACT_FK_INC_PROCINST;

alter table ACT_RU_INCIDENT
    drop foreign key ACT_FK_INC_PROCDEF;

alter table ACT_RU_INCIDENT
    drop foreign key ACT_FK_INC_CAUSE;

alter table ACT_RU_INCIDENT
    drop foreign key ACT_FK_INC_RCAUSE;
    
alter table ACT_RU_EXT_TASK
    drop foreign key ACT_FK_EXT_TASK_EXE;

drop index ACT_IDX_EVENT_SUBSCR_CONFIG_;
drop index ACT_IDX_ATHRZ_PROCEDEF;

-- indexes for concurrency problems - https://app.camunda.com/jira/browse/CAM-1646 --
drop index ACT_IDX_EXECUTION_PROC;
drop index ACT_IDX_EXECUTION_PARENT;
drop index ACT_IDX_EXECUTION_SUPER;
drop index ACT_IDX_EXECUTION_PROCINST;
drop index ACT_IDX_EVENT_SUBSCR_EXEC;
drop index ACT_IDX_BA_DEPLOYMENT;
drop index ACT_IDX_IDENT_LNK_TASK;
drop index ACT_IDX_INCIDENT_EXEC;
drop index ACT_IDX_INCIDENT_PROCINST;
drop index ACT_IDX_INCIDENT_PROC_DEF_ID;
drop index ACT_IDX_INCIDENT_CAUSE;
drop index ACT_IDX_INCIDENT_ROOT_CAUSE;
drop index ACT_IDX_JOB_EXCEPTION_STACK;
drop index ACT_IDX_VARIABLE_BA;
drop index ACT_IDX_VARIABLE_EXEC;
drop index ACT_IDX_VARIABLE_PROCINST;
drop index ACT_IDX_TASK_EXEC;
drop index ACT_IDX_TASK_PROCINST;
drop index ACT_IDX_TASK_PROC_DEF_ID;
drop index ACT_IDX_METER_LOG;
drop index ACT_IDX_AUTH_RESOURCE_ID;
drop index ACT_IDX_EXT_TASK_TOPIC;

drop index ACT_IDX_BYTEARRAY_NAME;
drop index ACT_IDX_DEPLOYMENT_NAME;
drop index ACT_IDX_JOBDEF_PROC_DEF_ID;
drop index ACT_IDX_JOB_HANDLER_TYPE;
drop index ACT_IDX_EVENT_SUBSCR_EVT_NAME;
drop index ACT_IDX_PROCDEF_DEPLOYMENT_ID;

drop table ACT_GE_PROPERTY;
drop table ACT_GE_BYTEARRAY;
drop table ACT_RE_DEPLOYMENT;
drop table ACT_RE_PROCDEF;
drop table ACT_RU_VARIABLE;
drop table ACT_RU_IDENTITYLINK;
drop table ACT_RU_TASK;
drop table ACT_RU_EXECUTION;
drop table ACT_RU_JOB;
drop table ACT_RU_JOBDEF;
drop table ACT_RU_EVENT_SUBSCR;
drop table ACT_RU_INCIDENT;
drop table ACT_RU_AUTHORIZATION;
drop table ACT_RU_FILTER;
drop table ACT_RU_METER_LOG;
drop table ACT_RU_EXT_TASK;

