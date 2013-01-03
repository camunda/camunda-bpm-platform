drop index ACT_RU_EXECUTION.ACT_IDX_EXEC_BUSKEY;
drop index ACT_RU_TASK.ACT_IDX_TASK_CREATE;
drop index ACT_RU_IDENTITYLINK.ACT_IDX_IDENT_LNK_USER;
drop index ACT_RU_IDENTITYLINK.ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_RU_VARIABLE.ACT_IDX_VARIABLE_TASK_ID;
drop index ACT_RU_EVENT_SUBSCR.ACT_IDX_EVENT_SUBSCR_CONFIG_;

alter table ACT_GE_BYTEARRAY 
    drop constraint ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION 
    drop constraint ACT_FK_EXE_PROCDEF;
	
alter table ACT_RU_EXECUTION 
    drop constraint ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop constraint ACT_FK_EXE_SUPER;  
	
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

drop index ACT_RU_IDENTITYLINK.ACT_IDX_ATHRZ_PROCEDEF;
    
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_GE_PROPERTY') drop table ACT_GE_PROPERTY;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_GE_BYTEARRAY') drop table ACT_GE_BYTEARRAY;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RE_PROCDEF') drop table ACT_RE_PROCDEF;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RE_DEPLOYMENT') drop table ACT_RE_DEPLOYMENT;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_IDENTITYLINK') drop table ACT_RU_IDENTITYLINK;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_TASK') drop table ACT_RU_TASK;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_VARIABLE') drop table ACT_RU_VARIABLE;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_EXECUTION') drop table ACT_RU_EXECUTION;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_EVENT_SUBSCR') drop table ACT_RU_EVENT_SUBSCR;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_JOB') drop table ACT_RU_JOB;